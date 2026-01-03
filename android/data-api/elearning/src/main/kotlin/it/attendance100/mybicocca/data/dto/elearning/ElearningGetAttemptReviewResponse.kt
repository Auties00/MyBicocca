package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningAttemptGrade(
    @SerialName("value")
    val value: Double? = null,
    @SerialName("str")
    val str: String? = null
)

@Serializable
data class ElearningAdditionalData(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("content")
    val content: String? = null
)

@Serializable
data class ElearningGetAttemptReviewResponse(
    @SerialName("grade")
    val grade: String? = null,
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("additionaldata")
    val additionalData: List<ElearningAdditionalData>? = null,
    @SerialName("questions")
    val questions: List<ElearningQuizQuestion>,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
