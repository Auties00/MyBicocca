package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningStartAttemptResponse(
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
