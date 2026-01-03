package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetSubmissionStatusResponse(
    @SerialName("gradingsummary")
    val gradingSummary: ElearningGradingSummary? = null,
    @SerialName("lastattempt")
    val lastAttempt: ElearningLastAttempt? = null,
    @SerialName("feedback")
    val feedback: ElearningAssignFeedback? = null,
    @SerialName("previousattempts")
    val previousAttempts: List<ElearningPreviousAttempt>? = null,
    @SerialName("assignmentdata")
    val assignmentData: ElearningAssignmentData? = null,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
