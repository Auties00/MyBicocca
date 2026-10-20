package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Attendance statistics of the current user for a single attendance module,
 * parsed from the summary table of the module's `view.php` page.
 *
 * "Taken" refers to the sessions whose attendance has already been recorded
 * by the teacher; "all" includes sessions not yet recorded. Percentages are
 * in the 0.0 to 100.0 range. The points fields mirror the grade points the
 * module awards per attended session and are kept as their rendered "earned /
 * maximum" labels because the plugin formats them locale-dependently.
 *
 * @property attendedSessions The number of sessions the user attended.
 * @property takenSessionsPercentage The attendance percentage over the recorded sessions.
 * @property totalSessions The total number of sessions of the module.
 * @property allSessionsPercentage The attendance percentage over all sessions.
 * @property pointsTakenLabel The "earned / available" points over the recorded sessions (e.g. "10 / 10").
 * @property pointsAllSessionsLabel The "earned / available" points over every session (e.g. "10 / 82").
 * @property maxPossiblePointsLabel The "earned / maximum" points achievable over every session (e.g. "82 / 82").
 * @property maxPossiblePercentage The best attendance percentage still reachable if every remaining session is attended.
 */
@Serializable
data class ElearningAttendanceSummary(
    @SerialName("attendedSessions")
    val attendedSessions: Int?,

    @SerialName("takenSessionsPercentage")
    val takenSessionsPercentage: Double?,

    @SerialName("totalSessions")
    val totalSessions: Int?,

    @SerialName("allSessionsPercentage")
    val allSessionsPercentage: Double?,

    @SerialName("pointsTakenLabel")
    val pointsTakenLabel: String? = null,

    @SerialName("maxPossiblePointsLabel")
    val maxPossiblePointsLabel: String? = null,

    @SerialName("maxPossiblePercentage")
    val maxPossiblePercentage: Double? = null,

    @SerialName("pointsAllSessionsLabel")
    val pointsAllSessionsLabel: String? = null
)

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
        val summary: ElearningAttendanceSummary?,
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
 * A self-markable attendance status the student may pick for a session
 * (e.g. "Presente"), parsed from the mobile take form.
 *
 * @property id The status id (`stid`) passed back as the `status` argument when marking.
 * @property description The label the teacher configured, shown to the student.
 */
data class ElearningAttendanceStatusOption(
    val id: String,
    val description: String
)

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
    val statuses: List<ElearningAttendanceStatusOption>
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
