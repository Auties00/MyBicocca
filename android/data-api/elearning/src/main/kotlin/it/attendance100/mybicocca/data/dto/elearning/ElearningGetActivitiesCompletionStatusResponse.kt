package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetActivitiesCompletionStatusResponse(
    @SerialName("statuses")
    val statuses: List<ElearningActivityCompletionStatus> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse

@Serializable
data class ElearningActivityCompletionStatus(
    @SerialName("cmid")
    val cmId: Int,
    @SerialName("modname")
    val modName: String? = null,
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
    val isNotTracked: Boolean get() = state == 0
    val isIncomplete: Boolean get() = state == 1
    val isComplete: Boolean get() = state == 2
    val isCompletePass: Boolean get() = state == 3
    val isCompleteFail: Boolean get() = state == 4
}
