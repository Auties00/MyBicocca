package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to list the modules of a course filtered by module type.
 *
 * Uses the same `core_course_get_contents` web service function as
 * [ElearningGetCourseContentsRequest] but applies the `modname` option so only
 * modules of the requested type are returned, and `excludecontents` so file
 * listings are omitted. This keeps the response small when the caller only
 * needs module identities (e.g. to discover attendance modules).
 *
 * @property courseId The course ID to inspect.
 * @property moduleName The module type to filter by (e.g. "attendance").
 */
class ElearningGetFilteredCourseModulesRequest(
    private val courseId: Int,
    private val moduleName: String
) : ElearningRequest<ElearningGetCourseContentsResponse> {
    override val functionName: String
        get() = "core_course_get_contents"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        formData.append("options[0][name]", "modname")
        formData.append("options[0][value]", moduleName)
        formData.append("options[1][name]", "excludecontents")
        formData.append("options[1][value]", "1")
    }
}

/**
 * Request to render a mobile view of an activity through the Moodle mobile
 * site-plugins bridge (`tool_mobile_get_content`).
 *
 * This is how the official Moodle app displays activities whose module type
 * has no dedicated web service functions exposed (e.g. `mod_attendance` on
 * the UniMiB instance). The server executes the plugin's mobile handler and
 * returns the rendered Ionic templates.
 *
 * @property component The plugin component (e.g. "mod_attendance").
 * @property method The mobile handler method (e.g. "mobile_view_activity").
 * @property args Name-value arguments forwarded to the handler (e.g. cmid, courseid).
 */
class ElearningGetMobileContentRequest(
    private val component: String,
    private val method: String,
    private val args: Map<String, String>
) : ElearningRequest<ElearningGetMobileContentResponse> {
    override val functionName: String
        get() = "tool_mobile_get_content"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("component", component)
        formData.append("method", method)
        args.entries.forEachIndexed { index, (name, value) ->
            formData.append("args[$index][name]", name)
            formData.append("args[$index][value]", value)
        }
    }
}

/**
 * Response of [ElearningGetMobileContentRequest], mirroring the structure
 * returned by `tool_mobile_get_content`.
 *
 * @property templates The rendered Ionic templates, keyed by template ID.
 * @property javascript JavaScript code to inject in the mobile app (unused here).
 * @property otherData Additional key-value data exposed to the template.
 * @property disabled Whether the plugin handler reported itself as disabled.
 */
@Serializable
data class ElearningGetMobileContentResponse(
    @SerialName("templates")
    val templates: List<ElearningMobileContentTemplate> = emptyList(),

    @SerialName("javascript")
    val javascript: String = "",

    @SerialName("otherdata")
    val otherData: List<ElearningMobileContentOtherData> = emptyList(),

    @SerialName("disabled")
    val disabled: Boolean = false
) : ElearningResponse

/**
 * A single rendered mobile template.
 *
 * @property id The template identifier (usually "main").
 * @property html The rendered Ionic/Angular HTML markup.
 */
@Serializable
data class ElearningMobileContentTemplate(
    @SerialName("id")
    val id: String,

    @SerialName("html")
    val html: String
)

/**
 * A key-value entry from the `otherdata` section of a mobile content response.
 *
 * @property name The entry name.
 * @property value The entry value.
 */
@Serializable
data class ElearningMobileContentOtherData(
    @SerialName("name")
    val name: String,

    @SerialName("value")
    val value: String
)

/**
 * Attendance statistics of the current user for a single attendance module,
 * parsed from the `mod_attendance` mobile view template.
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
    val maxPossiblePercentage: Double? = null
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
 * A `mod_attendance` session the current student is currently allowed to
 * self-mark, discovered through the mobile view bridge.
 *
 * Only sessions that are open for student marking right now appear here: the
 * plugin renders a mark action (carrying the [sessionId]) exclusively for
 * those. An empty list therefore means "no session to register at the moment",
 * which is the normal state outside lesson hours.
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
