package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningQuizQuestion(
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
    @SerialName("html")
    val html: String? = null,
    @SerialName("responsefileareas")
    val responseFileAreas: List<ElearningResponseFileArea>? = null,
    @SerialName("sequencecheck")
    val sequenceCheck: Int? = null,
    @SerialName("lastactiontime")
    val lastActionTime: Long? = null,
    @SerialName("hasautosavedstep")
    val hasAutosavedStep: Boolean? = null,
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
    val maxMark: Double? = null,
    @SerialName("settings")
    val settings: String? = null
)

@Serializable
data class ElearningResponseFileArea(
    @SerialName("area")
    val area: String? = null,
    @SerialName("files")
    val files: List<ElearningFile>? = null
)

@Serializable
data class ElearningGetAttemptDataResponse(
    @SerialName("attempt")
    val attempt: ElearningQuizAttempt,
    @SerialName("messages")
    val messages: List<String>? = null,
    @SerialName("nextpage")
    val nextPage: Int? = null,
    @SerialName("questions")
    val questions: List<ElearningQuizQuestion>,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
