package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningQuestionSummary(
    @SerialName("slot")
    val slot: Int,
    @SerialName("type")
    val type: String? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("questionnumber")
    val questionNumber: String? = null,
    @SerialName("number")
    val number: Int? = null,
    @SerialName("flagged")
    val flagged: Boolean? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("blockedbyprevious")
    val blockedByPrevious: Boolean? = null,
    @SerialName("mark")
    val mark: String? = null,
    @SerialName("maxmark")
    val maxMark: Double? = null
)

@Serializable
data class ElearningGetAttemptSummaryResponse(
    @SerialName("questions")
    val questions: List<ElearningQuestionSummary>,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
