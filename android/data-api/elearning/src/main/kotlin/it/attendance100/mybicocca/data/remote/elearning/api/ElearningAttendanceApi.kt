package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSubmittableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceViewResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetComponentStringsRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetComponentStringsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetFilteredCourseModulesRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentResponse
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.ConcurrentHashMap

/**
 * API for attendance-related operations (`mod_attendance`).
 *
 * The plugin's `mod_attendance_*` web service functions are only in its own "Attendance"
 * service, not in the mobile app's, so the mobile token can't call them (`accessexception`).
 * The summary and the sessions open for marking are read from the module's `view.php` page;
 * the take form and the marking itself go through the site-plugins bridge
 * (`tool_mobile_get_content`) the official Moodle app uses.
 *
 * Neither way of reading is free of side effects, so callers should read only when the user
 * asks to:
 * - Rendering the plugin's mobile view records the student's presence in every open session
 *   that auto-assigns a status. It is only used for marking.
 * - Every `view.php` load logs a `session_report_viewed` event in the course. When a teacher
 *   sets a session's automatic marking to "Yes", the plugin's cron marks present every user
 *   with any course log entry while the session runs, so reading the page then counts as
 *   attending, as opening the course in a browser or in the Moodle app would.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningAttendanceApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    companion object {
        private const val ATTENDANCE_MODULE_NAME = "attendance"
        private const val MOBILE_VIEW_METHOD = "mobile_view_activity"
        private const val MOBILE_USER_FORM_METHOD = "mobile_user_form"
        private const val MOBILE_COMPONENT = "mod_attendance"

        /** Path of the module page. */
        private const val VIEW_PAGE_PATH = "/mod/attendance/view.php"

        /** `view` value that shows every session (`ATT_VIEW_ALL`), and the full summary. */
        private const val VIEW_ALL_SESSIONS = "5"

        /** Selects the "latest" (Ionic 5+) Angular templates the modern app expects. */
        private const val APP_VERSION_CODE = "45000"

        /** Rows of the `attlist` summary when every session is shown (`construct_user_data_stat`). */
        private const val SUMMARY_ROW_COUNT = 8

        /** Id Moodle gives the guest user, whom an expired `MoodleSession` falls back to. */
        private const val GUEST_USER_ID = 1

        /** The string key of the link the session log shows for a session not open yet. */
        private const val FUTURE_LINK_STRING_ID = "submitattendancefuture"

        /** The page's `M.cfg` JavaScript config carries the id of the user it was rendered for. */
        private val PAGE_USER_ID_REGEX = Regex("\"userId\"\\s*:\\s*(\\d+)")

        /**
         * Matches the translate placeholder of a mobile template message, capturing the
         * string key (e.g. "incorrectpasswordshort").
         */
        private val TRANSLATE_KEY_REGEX = Regex("plugin\\.mod_attendance\\.(\\w+)")

        /**
         * Captures the `sessid` of an `attendance.php` link, or of a mobile view mark
         * action (`sessid: 123`).
         */
        private val SESSID_REGEX = Regex("sessid[=:]\\s*(\\d+)")

        private val WHITESPACE_REGEX = Regex("\\s+")

        /**
         * Plugin message key signaling a marking attempt did not record; see
         * [classifyMarkTemplate] for how these messages are rendered.
         */
        private const val MESSAGE_WRONG_PASSWORD = "incorrectpasswordshort"
        private const val MESSAGE_SUBNET = "subnetwrong"
        private const val MESSAGE_SHARED_IP = "preventsharederror"
        private const val MESSAGE_NO_STATUS = "attendance_no_status"
        private const val MESSAGE_INVALID_STATUS = "invalidstatus"

        /**
         * Tells apart, from the page Moodle finally served, a read `view.php`, an expired
         * session (Moodle serves the page, or the course's enrol page it redirects to, as the
         * guest user) and a logged-in user Moodle sent elsewhere.
         */
        internal fun classifyViewPage(
            path: String,
            document: Document,
            futureLinkText: String?
        ): ElearningAttendanceViewResponse {
            val userId = PAGE_USER_ID_REGEX.find(document.html())?.groupValues?.get(1)?.toIntOrNull()
            return when {
                userId == null || userId <= GUEST_USER_ID -> ElearningAttendanceViewResponse.RequiresReauth
                path != VIEW_PAGE_PATH -> ElearningAttendanceViewResponse.Unavailable
                else -> ElearningAttendanceViewResponse.Success(
                    summary = parseSummaryTable(document),
                    submittableSessions = parseSubmittableSessions(document, futureLinkText)
                )
            }
        }

        /**
         * Parses the `attlist` summary table. Its rows carry localized labels, but
         * `construct_user_data_stat` always emits the same eight rows in the same order when all
         * sessions are shown, so the values are read by position. Any other number of rows
         * means the markup changed, and the table is not read rather than mislabeled:
         *
         * ```
         * Sessioni frequentate:                        10
         * Punteggio sulle sessione già registrate:     10 / 10
         * Percentuale sulle sessioni già registrate:   100,0%
         * Numero totale delle sessioni:                82
         * Punteggio su tutte le sessioni:              10 / 82
         * Percentuale su tutte le sessioni:            12,2%
         * Punteggio massimo possibile:                 82 / 82
         * Percentuale massima possibile:               100,0%
         * ```
         *
         * @param document The `view.php` page
         * @return The parsed summary, or null when the page has no summary table of eight rows
         */
        internal fun parseSummaryTable(document: Document): ElearningAttendanceSummary? {
            val values = document.select("table.attlist tr")
                .mapNotNull { row -> row.select("td").getOrNull(1)?.text()?.trim() }
            if (values.size != SUMMARY_ROW_COUNT) {
                return null
            }

            return ElearningAttendanceSummary(
                takenSessions = values[0].toIntOrNull(),
                pointsTakenLabel = values[1].takeIf { it.isNotBlank() },
                takenSessionsPercentage = values[2].parsePercentage(),
                totalSessions = values[3].toIntOrNull(),
                pointsAllSessionsLabel = values[4].takeIf { it.isNotBlank() },
                allSessionsPercentage = values[5].parsePercentage(),
                maxPossiblePointsLabel = values[6].takeIf { it.isNotBlank() },
                maxPossiblePercentage = values[7].parsePercentage()
            )
        }

        /**
         * Collects the sessions the session log offers to submit attendance for right now. Such a
         * session's status, points and remarks cells are merged into one cell holding a link to
         * `attendance.php?sessid=…`. Left out are:
         * - sessions that already have a status, even when it can still be updated;
         * - sessions not open yet, whose link reads [futureLinkText] ("Report future absence")
         *   and leads to a form for reporting an absence in advance;
         * - rotating QR sessions, shown as a form instead of a link: the plugin's mobile handler
         *   only accepts the session's fixed password, which a rotating QR never carries;
         * - rows whose merged cell holds an enrolment message instead of a link.
         *
         * @param futureLinkText The localized text of the future-session link, or null if unknown,
         *   in which case such sessions are kept
         */
        internal fun parseSubmittableSessions(
            document: Document,
            futureLinkText: String?
        ): List<ElearningAttendanceSubmittableSession> =
            document.select("table.generaltable tr")
                .mapNotNull { row ->
                    val link = row.selectFirst("td[colspan=3] a[href*=attendance.php]") ?: return@mapNotNull null
                    if (futureLinkText != null && link.text().trim() == futureLinkText.trim()) {
                        return@mapNotNull null
                    }
                    val sessionId = SESSID_REGEX.find(link.attr("href"))?.groupValues?.get(1) ?: return@mapNotNull null
                    val time = row.selectFirst("td")?.text()?.normalizeSpace() ?: return@mapNotNull null
                    ElearningAttendanceSubmittableSession(sessionId = sessionId, time = time)
                }
                .distinctBy { it.sessionId }

        /**
         * Reads a session's `mobile_user_form` take form. The form shows a message instead of
         * the statuses when marking is refused right now, and the refusal is kept so that the
         * caller can tell the student why.
         */
        internal fun parseUserForm(
            session: ElearningAttendanceSubmittableSession,
            formHtml: String
        ): ElearningAttendanceMarkableSession {
            val form = Jsoup.parse(formHtml)
            val refusal = form.select("span.messages")
                .firstNotNullOfOrNull { TRANSLATE_KEY_REGEX.find(it.text())?.groupValues?.get(1) }
            return ElearningAttendanceMarkableSession(
                sessionId = session.sessionId,
                time = session.time,
                requiresPassword = refusal == null && form.selectFirst("[name=studentpass]") != null,
                statuses = if (refusal != null) emptyList() else form.select("ion-radio").mapNotNull { radio ->
                    val id = radio.attr("value").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ElearningAttendanceStatusOption(id = id, description = radio.text().trim())
                },
                refusal = refusal
            )
        }

        /**
         * Reads the marking outcome from the returned mobile view template, matching the plugin's
         * `mobile_view_page_latest.mustache` + `mobile.php` behavior.
         *
         * The plugin renders any blocking condition as a `plugin.mod_attendance.<key>` message
         * inside `<span class="messages">`. Otherwise the outcome is read from the session's own
         * item, found by its `<h2>` title, which is the same date and time text as the
         * `view.php` session log shows (`construct_session_full_date_time`):
         * - its submit action (`sessid` button) still there means it was not marked;
         * - a status in its trailing `<h3>` means it was;
         * - no item at all means the session is outside the view's time window (by default
         *   from 6 hours ago to 24 hours ahead), so nothing confirms the marking.
         */
        internal fun classifyMarkTemplate(
            document: Document,
            session: ElearningAttendanceSubmittableSession
        ): ElearningAttendanceMarkResult {
            val messageKeys = document.select("span.messages")
                .mapNotNull { TRANSLATE_KEY_REGEX.find(it.text())?.groupValues?.get(1) }

            when {
                messageKeys.any { it == MESSAGE_WRONG_PASSWORD } ->
                    return ElearningAttendanceMarkResult.WrongPassword
                messageKeys.any { it == MESSAGE_SHARED_IP } ->
                    return ElearningAttendanceMarkResult.NotOpen(MESSAGE_SHARED_IP)
                messageKeys.any { it == MESSAGE_SUBNET } ->
                    return ElearningAttendanceMarkResult.NotOpen(MESSAGE_SUBNET)
                messageKeys.any { it == MESSAGE_NO_STATUS || it == MESSAGE_INVALID_STATUS } ->
                    return ElearningAttendanceMarkResult.Failed(messageKeys.first())
            }

            val items = document.select("ion-item")
                .filter { it.selectFirst("h2")?.text()?.normalizeSpace() == session.time }
            if (items.isEmpty()) {
                return ElearningAttendanceMarkResult.Unconfirmed
            }
            if (items.any { item -> SESSID_REGEX.findAll(item.html()).any { it.groupValues[1] == session.sessionId } }) {
                return ElearningAttendanceMarkResult.NotOpen(reason = null)
            }

            val statusText = items.firstNotNullOfOrNull { item ->
                item.select("h3").lastOrNull()?.text()?.trim()?.takeIf { it.isNotBlank() }
            } ?: return ElearningAttendanceMarkResult.Failed(reason = null)
            return ElearningAttendanceMarkResult.Marked(statusText)
        }

        private fun String.normalizeSpace(): String = trim().replace(WHITESPACE_REGEX, " ")

        /**
         * Parses a localized percentage label (e.g. "100,0%") into its numeric value.
         *
         * @return The percentage in the 0.0 to 100.0 range, or null if not parseable
         */
        private fun String.parsePercentage(): Double? = this
            .removeSuffix("%")
            .replace(',', '.')
            .trim()
            .toDoubleOrNull()
    }

    /** The language the web services render in, which `view.php` is asked to match. */
    private val language: String
        get() = client.attributes.getOrNull(ElearningAttributes.Language) ?: DEFAULT_LANGUAGE

    /** Localized future-session link texts, by language. */
    private val futureLinkTexts = ConcurrentHashMap<String, String>()

    /**
     * Lists the attendance modules of a course.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course to inspect
     * @return The attendance modules of the course, possibly empty
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getAttendanceModules(
        wsToken: String,
        courseId: Int
    ): List<ElearningCourseModule> {
        val response = executeAuthenticatedRequest(
            wsToken,
            ElearningGetFilteredCourseModulesRequest(courseId, ATTENDANCE_MODULE_NAME)
        )
        return response.sections
            .flatMap { it.modules }
            .filter { it.moduleName == ATTENDANCE_MODULE_NAME }
    }

    /**
     * Reads the current user's attendance summary for an attendance module, together with the
     * sessions open for self-marking, from the module's `view.php` page.
     *
     * Unlike the plugin's mobile view, `view.php` never records a status itself: marking happens
     * on the page its "Submit attendance" links lead to. It does log a course event, which
     * matters for automatic marking, see [ElearningAttendanceApi]. The page needs the
     * `MoodleSession` browser cookie installed through the [ElearningApi] constructor, and is
     * asked for in the web services' language so that its session times match the mobile view's.
     *
     * @param wsToken The web service token (32 characters), to look up the future-session link text
     * @param moduleId The course module ID (cmid) of the attendance module
     * @return [ElearningAttendanceViewResponse.Success] with the parsed page,
     *   [ElearningAttendanceViewResponse.RequiresReauth] if the browser session expired, or
     *   [ElearningAttendanceViewResponse.Unavailable] if Moodle sent the user elsewhere
     * @throws ElearningException If the page returns a non-OK HTTP status, e.g. 404 for a deleted module
     */
    suspend fun getAttendanceSummary(wsToken: String, moduleId: Int): ElearningAttendanceViewResponse {
        val pageLanguage = language
        val response = client.get("${BASE_URL}mod/attendance/view.php") {
            url.parameters.append("id", moduleId.toString())
            url.parameters.append("view", VIEW_ALL_SESSIONS)
            url.parameters.append("lang", pageLanguage)
        }
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(
                errorCode = null,
                errorMessage = "view.php returned ${response.status} for moduleId=$moduleId"
            )
        }
        return classifyViewPage(
            path = response.request.url.encodedPath,
            document = Jsoup.parse(response.bodyAsText()),
            futureLinkText = runCatching { getFutureLinkText(wsToken, pageLanguage) }.getOrNull()
        )
    }

    /**
     * Looks up, through `core_get_component_strings` (a site-level read that logs nothing in a
     * course), the text of the link the session log shows for a session not open yet.
     */
    private suspend fun getFutureLinkText(wsToken: String, language: String): String? {
        futureLinkTexts[language]?.let { return it }
        val strings: ElearningGetComponentStringsResponse = executeAuthenticatedRequest(
            wsToken,
            ElearningGetComponentStringsRequest(component = MOBILE_COMPONENT, language = language)
        )
        val text = strings.items.firstOrNull { it.stringId == FUTURE_LINK_STRING_ID }?.string ?: return null
        futureLinkTexts[language] = text
        return text
    }

    /**
     * Describes the open sessions of a module, refused ones included.
     *
     * Loads each candidate's take form from the plugin's mobile bridge (`mobile_user_form`,
     * which never records anything and logs nothing in the course) to learn whether a student
     * password / QR pass is required and which statuses are selectable, or why the session
     * can't be marked right now ([ElearningAttendanceMarkableSession.refusal]). A session that
     * auto-assigns its status without a password comes back with no statuses.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @param sessions The candidate sessions, from
     *   [ElearningAttendanceViewResponse.Success.submittableSessions]
     * @return The described sessions, in the same order; one whose form can't be loaded is left out
     * @throws IllegalArgumentException If the token is invalid
     */
    suspend fun getMarkableSessions(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        sessions: List<ElearningAttendanceSubmittableSession>
    ): List<ElearningAttendanceMarkableSession> =
        sessions.mapNotNull { session ->
            val formHtml = runCatching {
                requestMobileTemplate(
                    wsToken, MOBILE_USER_FORM_METHOD,
                    mapOf(
                        "cmid" to moduleId.toString(),
                        "courseid" to courseId.toString(),
                        "sessid" to session.sessionId,
                        // Below 44000 the plugin serves its "ionic5" form, which puts each
                        // status label beside its ion-radio instead of inside it.
                        "appversioncode" to APP_VERSION_CODE
                    )
                )
            }.getOrNull() ?: return@mapNotNull null
            parseUserForm(session, formHtml)
        }

    /**
     * Records the current student's presence for an open attendance session
     * through the official mobile bridge — the same `tool_mobile_get_content` ->
     * `mobile_view_activity` call the Moodle app makes when a student taps "submit
     * attendance". Rendering that view also records the student's presence in every other
     * session of the module open right now that auto-assigns a status without a password,
     * as the Moodle app does.
     *
     * This covers typing the session password and scanning a static session QR, whose
     * pass is that same password; both go in [studentPass]. Rotating QR codes are not
     * supported: the plugin's mobile handler only compares against the session's fixed
     * password, not the rotating ones. Sessions that
     * auto-assign a status accept any non-empty [statusId]; otherwise pass one of
     * the ids from [getMarkableSessions].
     *
     * The view only lists sessions starting between 6 hours ago and 24 hours from now (the
     * site's defaults), and a session outside it is not marked; that comes back as
     * [ElearningAttendanceMarkResult.Unconfirmed], never as marked.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @param session The session to mark, as found on `view.php`
     * @param statusId The status to record; auto-assign sessions override it server-side
     * @param studentPass The session password / QR pass, or null when the session needs none
     * @return The classified outcome
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the bridge request fails
     */
    suspend fun markSession(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        session: ElearningAttendanceSubmittableSession,
        statusId: String,
        studentPass: String? = null
    ): ElearningAttendanceMarkResult {
        val args = buildMap {
            put("cmid", moduleId.toString())
            put("courseid", courseId.toString())
            put("sessid", session.sessionId)
            put("status", statusId)
            put("studentpass", studentPass.orEmpty())
            put("appversioncode", APP_VERSION_CODE)
        }
        val html = requestMobileTemplate(wsToken, MOBILE_VIEW_METHOD, args)
            ?: return ElearningAttendanceMarkResult.Failed(reason = null)
        return classifyMarkTemplate(Jsoup.parse(html), session)
    }

    private suspend fun requestMobileTemplate(
        wsToken: String,
        method: String,
        args: Map<String, String>
    ): String? {
        val response: ElearningGetMobileContentResponse = executeAuthenticatedRequest(
            wsToken,
            ElearningGetMobileContentRequest(
                component = MOBILE_COMPONENT,
                method = method,
                args = args
            )
        )
        return response.templates.firstOrNull()?.html
    }
}
