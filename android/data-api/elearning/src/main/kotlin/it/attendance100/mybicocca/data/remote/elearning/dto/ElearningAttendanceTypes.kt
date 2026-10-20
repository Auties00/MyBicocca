package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One localized string, as `core_get_component_strings` lists them.
 *
 * The attendance API reads the text of the session log's future-session link this way. The
 * function is a site-level read: it logs nothing in any course. It is called by hand, since
 * the generated Moodle client doesn't include it.
 *
 * @property stringId The string key (e.g. "submitattendancefuture").
 * @property string The text in the requested language.
 */
@Serializable
data class ElearningComponentString(
    @SerialName("stringid")
    val stringId: String,

    @SerialName("string")
    val string: String
)

/**
 * Attendance statistics of the current user for a single attendance module,
 * parsed from the summary table of the module's `view.php` page.
 *
 * "Taken" refers to the sessions whose attendance has already been recorded,
 * whatever the status (an absence counts too); "all" includes sessions not yet
 * recorded. Percentages are in the 0.0 to 100.0 range. The points fields mirror
 * the grade points the module awards per status and are kept as their rendered
 * "earned / maximum" labels because the plugin formats them locale-dependently.
 *
 * @property takenSessions The number of sessions with a recorded status, absences included
 *   (the plugin's `numtakensessions`, labeled "Sessioni frequentate" in Italian).
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
    @SerialName("takenSessions")
    val takenSessions: Int?,

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
     * @property summary The user's statistics, or null if the page showed none (or not the
     *   eight rows it is read by).
     * @property submittableSessions The sessions open right now that the page offers to submit
     *   attendance for, to pass to
     *   [it.attendance100.mybicocca.data.remote.elearning.api.ElearningAttendanceApi.getMarkableSessions].
     */
    data class Success(
        val summary: ElearningAttendanceSummary?,
        val submittableSessions: List<ElearningAttendanceSubmittableSession>,
    ) : ElearningAttendanceViewResponse

    /**
     * Moodle served the page as the guest user: the `MoodleSession` cookie expired (Moodle then
     * redirects to the course's enrol page). The caller should mint a new browser session and
     * retry once.
     */
    data object RequiresReauth : ElearningAttendanceViewResponse

    /**
     * The user is logged in but Moodle sent them elsewhere: the page for teachers
     * (`manage.php`) or report viewers (`report.php`), or an enrolment, policy or access
     * restriction page. A new session would not change that, so the caller should not renew it.
     */
    data object Unavailable : ElearningAttendanceViewResponse
}

/**
 * A session the `view.php` session log offers to submit attendance for right now.
 *
 * @property sessionId The `attendance_sessions` id (`sessid`).
 * @property time The session's date and time as the plugin renders it in the user's language
 *   (e.g. "mer 18 nov 2026 2PM - 4PM"), whitespace collapsed. The mobile view titles each session
 *   with the same text, which is how a marking result is matched to its session.
 */
data class ElearningAttendanceSubmittableSession(
    val sessionId: String,
    val time: String,
)

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
 * A `mod_attendance` session open right now, as described by the plugin's mobile take form.
 *
 * @property sessionId The `attendance_sessions` id, used as the `sessid` when marking.
 * @property time The session's date and time, see [ElearningAttendanceSubmittableSession.time].
 * @property requiresPassword Whether the teacher protected the session with a student password / QR pass.
 * @property statuses The selectable statuses; empty when the session auto-assigns one server-side,
 *   or when it is refused.
 * @property refusal The plugin's message key when the form refuses marking right now:
 *   "subnetwrong" (not on the classroom network), "preventsharederror" (this network already
 *   recorded a presence), "closed" or "attendance_no_status". Null when the session can be marked.
 */
data class ElearningAttendanceMarkableSession(
    val sessionId: String,
    val time: String,
    val requiresPassword: Boolean,
    val statuses: List<ElearningAttendanceStatusOption>,
    val refusal: String? = null,
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
     * No error came back, but the returned view doesn't list the session, so the marking can't
     * be confirmed. The mobile view only lists sessions starting between 6 hours ago and 24
     * hours from now (the site's defaults), so a longer-running session falls outside it.
     */
    data object Unconfirmed : ElearningAttendanceMarkResult

    /**
     * The attempt failed for another reason (no available status, expired status,
     * unexpected template).
     *
     * @property reason The plugin's message key when known, for logging.
     */
    data class Failed(val reason: String?) : ElearningAttendanceMarkResult
}
