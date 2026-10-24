package it.attendance100.mybicocca.data.elearning

import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebAttendanceApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleNamedValue
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebScrapers
import org.jsoup.Jsoup

/**
 * Attendance (`mod_attendance`), whose web service functions the university's Moodle doesn't
 * expose to the mobile service.
 *
 * The summary and the sessions open for marking are read from the activity's `view.php` page;
 * the take form and the marking itself go through the site-plugins bridge
 * (`tool_mobile_get_content`) the official Moodle app uses. The plugin's mobile view is never
 * rendered just to read, because rendering it records the student's presence in open sessions.
 *
 * @param moodle The web service functions the plugin's views are reached through.
 * @param pages The plugin's pages, sent with the user's browser session.
 */
class ElearningAttendance(
    private val moodle: MoodleApi,
    private val pages: MoodleWebAttendanceApi,
) {
    /** The attendance activities of a course, possibly none. */
    suspend fun getAttendanceModules(wsToken: String, courseId: Int): List<MoodleCourseModule> {
        // Only attendance activities, and without their file listings, to keep the response small.
        val sections = moodle.course.getContents(
            courseId = courseId,
            options = listOf(MoodleOption("modname", MODULE_NAME), MoodleOption("excludecontents", "1")),
            auth = MoodleTokenAuth(wsToken),
        )
        return sections.flatMap { it.modules }.filter { it.modName == MODULE_NAME }
    }

    /**
     * Reads the user's statistics for an attendance activity, with the sessions open for
     * self-marking, from its `view.php` page, which only reads: marking happens on the pages its
     * links lead to.
     *
     * @return The page, or [ElearningAttendanceViewResponse.RequiresReauth] when Moodle showed
     *   another page, typically its login page because the session expired.
     */
    suspend fun getAttendanceSummary(moduleId: Int): ElearningAttendanceViewResponse {
        val view = pages.getAttendanceView(moduleId)
        if (!view.shown) return ElearningAttendanceViewResponse.RequiresReauth
        return ElearningAttendanceViewResponse.Success(
            summary = view.summary,
            submittableSessionIds = view.submittableSessions.map { it.id }.distinct(),
        )
    }

    /**
     * Describes the sessions of an activity the student may mark themselves present at now.
     *
     * Each candidate's take form comes from the plugin's mobile bridge (`mobile_user_form`, which
     * never records anything), telling whether the session is still open, whether it asks for a
     * password or QR code, and which statuses it offers. A session the form refuses (closed,
     * wrong network, no status available) is left out; one assigning its status by itself
     * without a password comes back with no statuses.
     *
     * @param sessionIds The candidates, from [ElearningAttendanceViewResponse.Success.submittableSessionIds].
     */
    suspend fun getMarkableSessions(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        sessionIds: List<String>,
    ): List<ElearningAttendanceMarkableSession> = sessionIds.mapNotNull { sessionId ->
        val html = runCatching {
            template(
                wsToken,
                USER_FORM_METHOD,
                mapOf(
                    "cmid" to moduleId.toString(),
                    "courseid" to courseId.toString(),
                    "sessid" to sessionId,
                    // Below 44000 the plugin serves its "ionic5" form, which puts each status
                    // label beside its ion-radio instead of inside it.
                    "appversioncode" to APP_VERSION_CODE,
                ),
            )
        }.getOrNull() ?: return@mapNotNull null
        val form = MoodleWebScrapers.attendanceSessionForm(Jsoup.parse(html))?.takeUnless { it.refused } ?: return@mapNotNull null
        ElearningAttendanceMarkableSession(sessionId, form.requiresPassword, form.statuses)
    }

    /**
     * Records the student's presence at an open session through the mobile bridge, as the Moodle
     * app does when a student taps "submit attendance". Rendering that view also records the
     * presence in every other open session of the activity assigning a status by itself without
     * a password, as the Moodle app does.
     *
     * The password typed or read from the session's static QR code goes in [studentPass]; the
     * plugin's mobile handler doesn't accept the rotating QR codes. Sessions assigning their
     * status by themselves accept any [statusId].
     */
    suspend fun markSession(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        sessionId: String,
        statusId: String,
        studentPass: String? = null,
    ): ElearningAttendanceMarkResult {
        val arguments = mapOf(
            "cmid" to moduleId.toString(),
            "courseid" to courseId.toString(),
            "sessid" to sessionId,
            "status" to statusId,
            "studentpass" to studentPass.orEmpty(),
            "appversioncode" to APP_VERSION_CODE,
        )
        val html = template(wsToken, VIEW_METHOD, arguments) ?: return ElearningAttendanceMarkResult.Failed(reason = null)
        return classify(html, sessionId)
    }

    private suspend fun template(wsToken: String, method: String, arguments: Map<String, String>): String? =
        moodle.mobile.getContent(
            component = COMPONENT,
            method = method,
            args = arguments.map { (name, value) -> MoodleNamedValue(name, value) },
            auth = MoodleTokenAuth(wsToken),
        ).templates.firstOrNull()?.html

    /**
     * Reads the outcome of marking from the template of `mobile_view_activity`. The plugin shows
     * every blocking condition as a message; otherwise marking drops the session's button and
     * shows its status. A session that still has its button and no message wasn't matched, as
     * when it closed in the meantime. A marked session's status is the last heading of its item,
     * after the group's.
     */
    private fun classify(html: String, sessionId: String): ElearningAttendanceMarkResult {
        val page = MoodleWebScrapers.attendanceMarkPage(Jsoup.parse(html)) ?: return ElearningAttendanceMarkResult.Failed(reason = null)
        val keys = page.messages.map { it.key }
        return when {
            MESSAGE_WRONG_PASSWORD in keys -> ElearningAttendanceMarkResult.WrongPassword
            MESSAGE_SHARED_IP in keys -> ElearningAttendanceMarkResult.NotOpen(MESSAGE_SHARED_IP)
            MESSAGE_SUBNET in keys -> ElearningAttendanceMarkResult.NotOpen(MESSAGE_SUBNET)
            keys.any { it == MESSAGE_NO_STATUS || it == MESSAGE_INVALID_STATUS } -> ElearningAttendanceMarkResult.Failed(keys.first())
            page.markActions.any { it.sessionId == sessionId } -> ElearningAttendanceMarkResult.NotOpen(reason = null)
            else -> ElearningAttendanceMarkResult.Marked(page.headings.lastOrNull())
        }
    }

    private companion object {
        const val MODULE_NAME = "attendance"
        const val COMPONENT = "mod_attendance"
        const val VIEW_METHOD = "mobile_view_activity"
        const val USER_FORM_METHOD = "mobile_user_form"

        /** Selects the "latest" (Ionic 5+) templates the modern app expects. */
        const val APP_VERSION_CODE = "45000"

        /** The keys of the plugin's messages for an attempt that recorded nothing. */
        const val MESSAGE_WRONG_PASSWORD = "incorrectpasswordshort"
        const val MESSAGE_SUBNET = "subnetwrong"
        const val MESSAGE_SHARED_IP = "preventsharederror"
        const val MESSAGE_NO_STATUS = "attendance_no_status"
        const val MESSAGE_INVALID_STATUS = "invalidstatus"
    }
}

/** What reading an attendance activity's `view.php` page found. */
sealed interface ElearningAttendanceViewResponse {
    /**
     * @property summary The user's statistics, or null if the page showed none.
     * @property submittableSessionIds The sessions the page offers to submit attendance for,
     *   for [ElearningAttendance.getMarkableSessions].
     */
    data class Success(
        val summary: MoodleWebAttendanceSummary?,
        val submittableSessionIds: List<String>,
    ) : ElearningAttendanceViewResponse

    /**
     * Moodle showed another page, typically because the `MoodleSession` cookie expired. The
     * caller should renew the session and retry; if the retry fails too, the user most likely
     * isn't enrolled in the course.
     */
    data object RequiresReauth : ElearningAttendanceViewResponse
}

/**
 * A session the student may mark themselves present at now. None is the normal state outside
 * lesson hours.
 *
 * @property sessionId The session, as the `sessid` marking it.
 * @property requiresPassword Whether the teacher protected the session with a password or QR code.
 * @property statuses The statuses to choose from; none when the session assigns one by itself.
 */
data class ElearningAttendanceMarkableSession(
    val sessionId: String,
    val requiresPassword: Boolean,
    val statuses: List<MoodleWebAttendanceStatusOption>,
)

/** The outcome of marking a presence, read from the template the plugin renders after it. */
sealed interface ElearningAttendanceMarkResult {
    /** @property statusDescription The status the session assigned (e.g. "Presente"), when shown. */
    data class Marked(val statusDescription: String?) : ElearningAttendanceMarkResult

    /** The student had already marked this session. */
    data object AlreadyMarked : ElearningAttendanceMarkResult

    /** The password or QR code didn't match the session's. */
    data object WrongPassword : ElearningAttendanceMarkResult

    /**
     * The session can't be marked now: closed, outside its window, refused by the classroom
     * network check, or already marked from this network.
     *
     * @property reason The plugin's message key when known (e.g. "subnetwrong"), for logging.
     */
    data class NotOpen(val reason: String?) : ElearningAttendanceMarkResult

    /**
     * The attempt failed for another reason, such as no status being available.
     *
     * @property reason The plugin's message key when known, for logging.
     */
    data class Failed(val reason: String?) : ElearningAttendanceMarkResult
}
