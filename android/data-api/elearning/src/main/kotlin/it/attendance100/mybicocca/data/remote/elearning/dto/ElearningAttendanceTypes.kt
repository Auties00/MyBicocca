package it.attendance100.mybicocca.data.remote.elearning.dto

/**
 * Result of reading an attendance module's `view.php` page.
 */
sealed interface ElearningAttendanceViewResponse {
    /**
     * The page was read.
     *
     * @property summary The user's statistics, or null if the page showed none.
     * @property submittableSessionIds The sessions the page offers to submit attendance for,
     *   to pass to
     *   [it.attendance100.mybicocca.data.remote.elearning.api.ElearningAttendanceApi.getMarkableSessions].
     */
    data class Success(
        val summary: MoodleWebAttendanceSummary?,
        val submittableSessionIds: List<String>,
    ) : ElearningAttendanceViewResponse

    /**
     * Moodle redirected away from `view.php`, typically because the `MoodleSession` cookie
     * expired. The caller should mint a new browser session and retry; if the retry fails too,
     * the user most likely isn't enrolled in the course.
     */
    data object RequiresReauth : ElearningAttendanceViewResponse
}

/**
 * A `mod_attendance` session the current student is currently allowed to
 * self-mark, as reported by the plugin's mobile take form.
 *
 * Only sessions open for student marking right now appear here. An empty list
 * therefore means "no session to register at the moment", which is the normal
 * state outside lesson hours.
 *
 * @property sessionId The `attendance_sessions` id, used as the `sessid` when marking.
 * @property requiresPassword Whether the teacher protected the session with a student password / QR pass.
 * @property statuses The selectable statuses; empty when the session auto-assigns one server-side.
 */
data class ElearningAttendanceMarkableSession(
    val sessionId: String,
    val requiresPassword: Boolean,
    val statuses: List<MoodleWebAttendanceStatusOption>
)

/**
 * Outcome of a `mod_attendance` self-marking attempt made through the official
 * mobile bridge (`tool_mobile_get_content` -> `mobile_view_activity`), classified
 * from the returned template: a marked session loses its submit action and gains
 * a status, while every failure path renders a known message key.
 */
sealed interface ElearningAttendanceMarkResult {

    /**
     * The presence was recorded.
     *
     * @property statusDescription The status the session assigned (e.g. "Presente"), when exposed.
     */
    data class Marked(val statusDescription: String?) : ElearningAttendanceMarkResult

    /** The student had already marked this session (it now shows a status, not a mark action). */
    data object AlreadyMarked : ElearningAttendanceMarkResult

    /** The supplied password / QR pass did not match the session. */
    data object WrongPassword : ElearningAttendanceMarkResult

    /**
     * The session cannot be marked right now: closed, out of its window, blocked by
     * the classroom-network (subnet) check, or already marked from this network.
     *
     * @property reason The plugin's message key when known (e.g. "subnetwrong"), for logging.
     */
    data class NotOpen(val reason: String?) : ElearningAttendanceMarkResult

    /**
     * The attempt failed for another reason (no available status, expired status,
     * unexpected template).
     *
     * @property reason The plugin's message key when known, for logging.
     */
    data class Failed(val reason: String?) : ElearningAttendanceMarkResult
}
