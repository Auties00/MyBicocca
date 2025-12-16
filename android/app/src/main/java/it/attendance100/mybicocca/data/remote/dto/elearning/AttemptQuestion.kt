package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AttemptQuestion(
    @SerializedName("slot") val slot: Int,
    @SerializedName("type") val type: String,
    @SerializedName("page") val page: Int,
    @SerializedName("html") val html: String,
    @SerializedName("questionnumber") val questionNumber: String? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("responsefileareas") val responseFileAreas: List<QuestionFileArea>? = null,
    @SerializedName("sequencecheck") val sequenceCheck: Int? = null,
    @SerializedName("lastactiontime") val lastActionTime: Int? = null,
    @SerializedName("hasautosavedstep") val hasAutoSavedStep: Boolean? = null,
    @SerializedName("flagged") val flagged: Boolean? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("stateclass") val stateClass: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("blockedbyprevious") val blockedByPrevious: Boolean? = null,
    @SerializedName("mark") val mark: String? = null,
    @SerializedName("maxmark") val maxMark: BigDecimal? = null,
    @SerializedName("settings") val settings: String? = null
)
