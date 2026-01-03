package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningProcessAttemptResponse(
    @SerialName("state")
    val state: String,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse {
    val isFinished: Boolean
        get() = state == ElearningQuizAttempt.STATE_FINISHED
}
