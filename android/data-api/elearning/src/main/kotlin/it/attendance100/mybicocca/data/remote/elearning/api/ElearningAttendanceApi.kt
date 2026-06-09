package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetFilteredCourseModulesRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentResponse
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * API for attendance-related operations (`mod_attendance`).
 *
 * The UniMiB Moodle instance does not expose the `mod_attendance_*` web
 * service functions to the mobile service, so this API reads attendance data
 * the same way the official Moodle app does: through the site-plugins bridge
 * (`tool_mobile_get_content`), which renders the plugin's mobile view
 * server-side. The rendered template is then parsed into a structured
 * [ElearningAttendanceSummary].
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

        // Selects the "latest" (Ionic 5+) Angular templates the modern app expects.
        private const val APP_VERSION_CODE = "45000"

        /**
         * Matches the translate placeholder of a summary row, capturing the
         * string key (e.g. "sessionscompleted").
         */
        private val TRANSLATE_KEY_REGEX = Regex("plugin\\.mod_attendance\\.(\\w+)")

        // Captures every `sessid` a mobile view button exposes as a mark action;
        // the plugin renders one only for sessions open for student self-marking.
        private val SESSID_REGEX = Regex("sessid:\\s*(\\d+)")

        // Plugin message keys (rendered inside <span class="messages"> as
        // `plugin.mod_attendance.<key>`) that signal a marking attempt did not record.
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
     * Gets the current user's attendance summary for an attendance module.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @return The parsed summary, or null if the plugin view exposed no statistics
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the request fails
     */
    suspend fun getAttendanceSummary(
        wsToken: String,
        courseId: Int,
        moduleId: Int
    ): ElearningAttendanceSummary? {
        val response = executeAuthenticatedRequest(
            wsToken,
            ElearningGetMobileContentRequest(
                component = MOBILE_COMPONENT,
                method = MOBILE_VIEW_METHOD,
                args = mapOf(
                    "cmid" to moduleId.toString(),
                    "courseid" to courseId.toString()
                )
            )
        )
        val html = response.templates.firstOrNull()?.html ?: return null
        return parseSummaryTemplate(html)
    }

    /**
     * Parses the rendered mobile view template into a summary.
     *
     * The template lays the statistics out as `ion-row` elements whose first
     * column holds a `{{ 'plugin.mod_attendance.<key>' | translate }}`
     * placeholder and whose second column holds the value, for example:
     *
     * ```
     * <ion-row>
     *   <ion-col>{{ 'plugin.mod_attendance.sessionscompleted' | translate }}</ion-col>
     *   <ion-col>9</ion-col>
     * </ion-row>
     * ```
     *
     * @param html The template markup
     * @return The parsed summary, or null when no known statistic was found
     */
    private fun parseSummaryTemplate(html: String): ElearningAttendanceSummary? {
        val document = Jsoup.parse(html)
        val values = mutableMapOf<String, String>()
        for (row in document.select("ion-row")) {
            val columns = row.select("ion-col")
            if (columns.size < 2) {
                continue
            }

            val key = TRANSLATE_KEY_REGEX.find(columns[0].text())?.groupValues?.get(1) ?: continue
            values[key] = columns[1].text().trim()
        }
        if (values.isEmpty()) {
            return null
        }

        return ElearningAttendanceSummary(
            attendedSessions = values["sessionscompleted"]?.toIntOrNull(),
            takenSessionsPercentage = values["percentagesessionscompleted"]?.parsePercentage(),
            totalSessions = values["sessionstotal"]?.toIntOrNull(),
            allSessionsPercentage = values["percentageallsessions"]?.parsePercentage(),
            pointsTakenLabel = values["pointssessionscompleted"]?.takeIf { it.isNotBlank() },
            maxPossiblePointsLabel = values["maxpossiblepoints"]?.takeIf { it.isNotBlank() },
            maxPossiblePercentage = values["maxpossiblepercentage"]?.parsePercentage()
        )
    }

    /**
     * Lists the `mod_attendance` sessions of a module the student may self-mark
     * right now.
     *
     * Reads the plugin's mobile view through the site-plugins bridge and collects
     * the sessions for which it rendered a mark action, then loads each one's take
     * form to learn whether a student password / QR pass is required and which
     * statuses are selectable. An empty result means no session is open for marking
     * at the moment, which is the normal state outside lesson hours.
     *
     * @param wsToken The web service token (32 characters)
     * @param courseId The course the module belongs to
     * @param moduleId The course module ID (cmid) of the attendance module
     * @return The markable sessions, possibly empty
     * @throws IllegalArgumentException If the token is invalid
     * @throws ElearningException If the bridge request fails
     */
    suspend fun getMarkableSessions(
        wsToken: String,
        courseId: Int,
        moduleId: Int
    ): List<ElearningAttendanceMarkableSession> {
        val viewHtml = requestMobileTemplate(
            wsToken, MOBILE_VIEW_METHOD,
            mapOf("cmid" to moduleId.toString(), "courseid" to courseId.toString())
        ) ?: return emptyList()

        val sessionIds = SESSID_REGEX.findAll(viewHtml).map { it.groupValues[1] }.distinct().toList()
        return sessionIds.mapNotNull { sessionId ->
            val formHtml = runCatching {
                requestMobileTemplate(
                    wsToken, MOBILE_USER_FORM_METHOD,
                    mapOf(
                        "cmid" to moduleId.toString(),
                        "courseid" to courseId.toString(),
                        "sessid" to sessionId
                    )
                )
            }.getOrNull() ?: return@mapNotNull null

            val form = Jsoup.parse(formHtml)
            ElearningAttendanceMarkableSession(
                sessionId = sessionId,
                requiresPassword = form.selectFirst("[name=studentpass]") != null,
                statuses = form.select("ion-radio").mapNotNull { radio ->
                    val id = radio.attr("value").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ElearningAttendanceStatusOption(id = id, description = radio.text().trim())
                }
            )
        }
    }

    /**
     * Records the current student's presence for an open attendance session
     * through the official mobile bridge — the same `tool_mobile_get_content` ->
     * `mobile_view_activity` call the Moodle app makes when a student taps "submit
     * attendance". No browser session or web scraping is involved.
     *
     * This covers both ways a student marks: scanning the session QR (whose pass
     * is passed as [studentPass]) and typing the session password. Sessions that
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

    // Reads the marking outcome from the returned mobile view template. The plugin
    // renders any blocking condition as a `plugin.mod_attendance.<key>` message;
    // otherwise a successful take_from_student drops the session's submit action
    // (its `sessid` button) and surfaces the assigned status, so the absence of
    // both a message and a remaining mark action for this session is the success
    // signal (verified against mobile_view_page_latest.mustache + mobile.php).
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
            // No blocking message yet the mark action is still present: the session
            // wasn't matched (e.g. it closed between discovery and marking).
            return ElearningAttendanceMarkResult.NotOpen(reason = null)
        }

        // Marked: pick up the assigned status from a marked session row, when present.
        // currentstatus is the trailing <h3> of a session item (after the group name).
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
