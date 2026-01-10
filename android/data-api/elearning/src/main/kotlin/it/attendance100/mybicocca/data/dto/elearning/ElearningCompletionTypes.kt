package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve the completion status of activities in a course.
 *
 * @property courseId The course identifier to get activity completion statuses for.
 * @property userId Optional user identifier. If not provided, returns completion status for the current user.
 */
@Serializable
class ElearningGetActivitiesCompletionStatusRequest(
    private val courseId: Int,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetActivitiesCompletionStatusResponse> {
    override val functionName = "core_completion_get_activities_completion_status"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        userId?.let { formData.append("userid", it.toString()) }
    }
}

/**
 * Response containing the completion statuses of activities in a course.
 *
 * @property statuses The list of activity completion statuses.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningGetActivitiesCompletionStatusResponse(
    @SerialName("statuses")
    val statuses: List<ElearningActivityCompletionStatus> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to manually update the completion status of an activity.
 *
 * @property courseModuleId The course module identifier of the activity to update.
 * @property completed Whether to mark the activity as completed.
 */
@Serializable
class ElearningUpdateActivityCompletionRequest(
    private val courseModuleId: Int,
    private val completed: Boolean
) : ElearningRequest<ElearningUpdateActivityCompletionResponse> {
    override val functionName = "core_completion_update_activity_completion_status_manually"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("cmid", courseModuleId.toString())
        formData.append("completed", if (completed) "1" else "0")
    }
}

/**
 * Response from updating an activity's completion status.
 *
 * @property status Whether the update was successful.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningUpdateActivityCompletionResponse(
    @SerialName("status")
    val status: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents the completion status of an activity.
 *
 * @property courseModuleId The course module identifier of the activity.
 * @property moduleName The name of the module type (e.g., "quiz", "assign", "forum").
 * @property instance The instance identifier of the module.
 * @property state The completion state (0=not tracked, 1=incomplete, 2=complete, 3=complete pass, 4=complete fail).
 * @property timeCompleted The Unix timestamp when the activity was completed.
 * @property tracking The tracking mode (0=none, 1=manual, 2=automatic).
 * @property overrideBy The user identifier who overrode this completion status, if any.
 * @property valueUsed Whether this completion value is used in course completion criteria.
 */
@Serializable
data class ElearningActivityCompletionStatus(
    @SerialName("cmid")
    val courseModuleId: Int,
    @SerialName("modname")
    val moduleName: String? = null,
    @SerialName("instance")
    val instance: Int? = null,
    @SerialName("state")
    val state: Int,
    @SerialName("timecompleted")
    val timeCompleted: Long? = null,
    @SerialName("tracking")
    val tracking: Int? = null,
    @SerialName("overrideby")
    val overrideBy: Int? = null,
    @SerialName("valueused")
    val valueUsed: Boolean? = null
) {
    /**
     * Whether completion tracking is disabled for this activity.
     */
    val isNotTracked: Boolean get() = state == 0

    /**
     * Whether the activity is incomplete.
     */
    val isIncomplete: Boolean get() = state == 1

    /**
     * Whether the activity is complete.
     */
    val isComplete: Boolean get() = state == 2

    /**
     * Whether the activity is complete with a passing grade.
     */
    val isCompletePass: Boolean get() = state == 3

    /**
     * Whether the activity is complete with a failing grade.
     */
    val isCompleteFail: Boolean get() = state == 4
}
