package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceViewResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetFilteredCourseModulesRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentResponse
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * API for attendance-related operations (`mod_attendance`).
 *
 * The UniMiB Moodle instance does not expose the `mod_attendance_*` web
 * service functions to the mobile service. The summary and the sessions open
 * for marking are read from the module's `view.php` page; the take form and the
 * marking itself go through the site-plugins bridge (`tool_mobile_get_content`)
 * the official Moodle app uses. The plugin's mobile view is not used for reading,
 * because rendering it records the student's presence in open sessions.
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

        /** Path of the module page, which Moodle leaves when the browser session expired. */
        private const val VIEW_PAGE_PATH = "/mod/attendance/view.php"

        /** `view` value that shows every session (`ATT_VIEW_ALL`), and the full summary. */
        private const val VIEW_ALL_SESSIONS = "5"

        /** Selects the "latest" (Ionic 5+) Angular templates the modern app expects. */
        private const val APP_VERSION_CODE = "45000"

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

        /**
         * Plugin message key signaling a marking attempt did not record; see
         * [classifyMarkTemplate] for how these messages are rendered.
         */
        private const val MESSAGE_WRONG_PASSWORD = "incorrectpasswordshort"
        private const val MESSAGE_SUBNET = "subnetwrong"
        private const val MESSAGE_SHARED_IP = "preventsharederror"
        private const val MESSAGE_NO_STATUS = "attendance_no_status"
        private const val MESSAGE_INVALID_STATUS = "invalidstatus"
    }

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
     * Unlike the plugin's mobile view, which records the student's presence in every open
     * session that auto-assigns a status whenever it renders, `view.php` only reads: marking
     * happens on the page its "Submit attendance" links lead to. The page needs the
     * `MoodleSession` browser cookie installed through the [ElearningApi] constructor.
     *
     * @param moduleId The course module ID (cmid) of the attendance module
     * @return [ElearningAttendanceViewResponse.Success] with the parsed page, or
     *   [ElearningAttendanceViewResponse.RequiresReauth] if Moodle redirected away from it
     * @throws ElearningException If the page returns a non-OK HTTP status
     */
    suspend fun getAttendanceSummary(moduleId: Int): ElearningAttendanceViewResponse {
        val response = client.get("${BASE_URL}mod/attendance/view.php") {
            url.parameters.append("id", moduleId.toString())
            url.parameters.append("view", VIEW_ALL_SESSIONS)
        }
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(
                errorCode = null,
                errorMessage = "view.php returned ${response.status} for moduleId=$moduleId"
            )
        }
        if (response.request.url.encodedPath != VIEW_PAGE_PATH) {
            return ElearningAttendanceViewResponse.RequiresReauth
        }
        val document = Jsoup.parse(response.bodyAsText())
        return ElearningAttendanceViewResponse.Success(
            summary = parseSummaryTable(document),
            submittableSessionIds = parseSubmittableSessionIds(document)
        )
    }

    /**
     * Parses the `attlist` summary table. Its rows carry localized labels, but
     * `construct_user_data_stat` always emits them in the same order when all sessions are
     * shown, so the values are read by position:
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
     * @return The parsed summary, or null when the page has no summary table
     */
    private fun parseSummaryTable(document: Document): ElearningAttendanceSummary? {
        val values = document.select("table.attlist tr")
            .mapNotNull { row -> row.select("td").getOrNull(1)?.text()?.trim() }
        if (values.isEmpty()) {
            return null
        }

        return ElearningAttendanceSummary(
            attendedSessions = values.getOrNull(0)?.toIntOrNull(),
            pointsTakenLabel = values.getOrNull(1)?.takeIf { it.isNotBlank() },
            takenSessionsPercentage = values.getOrNull(2)?.parsePercentage(),
            totalSessions = values.getOrNull(3)?.toIntOrNull(),
            pointsAllSessionsLabel = values.getOrNull(4)?.takeIf { it.isNotBlank() },
            allSessionsPercentage = values.getOrNull(5)?.parsePercentage(),
            maxPossiblePointsLabel = values.getOrNull(6)?.takeIf { it.isNotBlank() },
            maxPossiblePercentage = values.getOrNull(7)?.parsePercentage()
        )
    }

    /**
     * Collects the sessions the session log offers to submit attendance for. Such a session's
     * status, points and remarks cells are merged into one cell holding either a link to
     * `attendance.php?sessid=…` or, for rotating QR sessions, a form with a `sessid` field.
     * Sessions that already have a status are left out, even when their status can still be
     * updated.
     */
    private fun parseSubmittableSessionIds(document: Document): List<String> =
        document.select("table.generaltable td[colspan=3]")
            .mapNotNull { cell ->
                cell.selectFirst("a[href*=attendance.php]")
                    ?.attr("href")
                    ?.let { SESSID_REGEX.find(it)?.groupValues?.get(1) }
                    ?: cell.selectFirst("input[name=sessid]")?.attr("value")?.takeIf { it.isNotBlank() }
            }
            .distinct()

    /**
     * Describes the sessions of a module the student may self-mark right now.
     *
     * Loads each candidate's take form from the plugin's mobile bridge (`mobile_user_form`,
     * which never records anything) to learn whether the session is still open, whether a
     * student password / QR pass is required and which statuses are selectable. A session the
     * form refuses (closed, wrong network, no status available) is left out; one that
     * auto-assigns its status without a password comes back with no statuses.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @param sessionIds The candidate sessions, from
     *   [ElearningAttendanceViewResponse.Success.submittableSessionIds]
     * @return The markable sessions, possibly empty
     * @throws IllegalArgumentException If the token is invalid
     */
    suspend fun getMarkableSessions(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        sessionIds: List<String>
    ): List<ElearningAttendanceMarkableSession> =
        sessionIds.mapNotNull { sessionId ->
            val formHtml = runCatching {
                requestMobileTemplate(
                    wsToken, MOBILE_USER_FORM_METHOD,
                    mapOf(
                        "cmid" to moduleId.toString(),
                        "courseid" to courseId.toString(),
                        "sessid" to sessionId,
                        // Below 44000 the plugin serves its "ionic5" form, which puts each
                        // status label beside its ion-radio instead of inside it.
                        "appversioncode" to APP_VERSION_CODE
                    )
                )
            }.getOrNull() ?: return@mapNotNull null

            val form = Jsoup.parse(formHtml)
            if (form.selectFirst("span.messages") != null) {
                return@mapNotNull null
            }
            ElearningAttendanceMarkableSession(
                sessionId = sessionId,
                requiresPassword = form.selectFirst("[name=studentpass]") != null,
                statuses = form.select("ion-radio").mapNotNull { radio ->
                    val id = radio.attr("value").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ElearningAttendanceStatusOption(id = id, description = radio.text().trim())
                }
            )
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
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @param sessionId The `attendance_sessions` id (`sessid`) to mark
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
        sessionId: String,
        statusId: String,
        studentPass: String? = null
    ): ElearningAttendanceMarkResult {
        val args = buildMap {
            put("cmid", moduleId.toString())
            put("courseid", courseId.toString())
            put("sessid", sessionId)
            put("status", statusId)
            put("studentpass", studentPass.orEmpty())
            put("appversioncode", APP_VERSION_CODE)
        }
        val html = requestMobileTemplate(wsToken, MOBILE_VIEW_METHOD, args)
            ?: return ElearningAttendanceMarkResult.Failed(reason = null)
        return classifyMarkTemplate(Jsoup.parse(html), sessionId)
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

    /**
     * Reads the marking outcome from the returned mobile view template, matching the plugin's
     * `mobile_view_page_latest.mustache` + `mobile.php` behavior.
     *
     * The plugin renders any blocking condition as a `plugin.mod_attendance.<key>` message
     * inside `<span class="messages">`; otherwise a successful take_from_student drops the
     * session's submit action (its `sessid` button) and surfaces the assigned status, so the
     * absence of both a message and a remaining mark action for this session is the success
     * signal. When no blocking message is present but the mark action remains, the session
     * was not matched (e.g. it closed between discovery and marking). The status assigned to
     * a marked session is the trailing `<h3>` of its session item, after the group name.
     */
    private fun classifyMarkTemplate(
        document: Document,
        sessionId: String
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

        val stillMarkable = SESSID_REGEX.findAll(document.html())
            .any { it.groupValues[1] == sessionId }
        if (stillMarkable) {
            return ElearningAttendanceMarkResult.NotOpen(reason = null)
        }

        val statusText = document.select("ion-item h3")
            .map { it.text().trim() }
            .lastOrNull { it.isNotBlank() }
        return ElearningAttendanceMarkResult.Marked(statusText)
    }

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
