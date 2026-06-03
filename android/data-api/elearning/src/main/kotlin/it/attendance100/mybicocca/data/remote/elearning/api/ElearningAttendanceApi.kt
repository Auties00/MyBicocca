package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetFilteredCourseModulesRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetMobileContentRequest
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

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
        private const val MOBILE_COMPONENT = "mod_attendance"

        /**
         * Matches the translate placeholder of a summary row, capturing the
         * string key (e.g. "sessionscompleted").
         */
        private val TRANSLATE_KEY_REGEX = Regex("plugin\\.mod_attendance\\.(\\w+)")
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
            allSessionsPercentage = values["percentageallsessions"]?.parsePercentage()
        )
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
