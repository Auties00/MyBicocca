package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ProcessAttemptResponse(
    @SerializedName("state") val state: ProcessAttemptResponse.ProcessAttemptState? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
) {
    enum class ProcessAttemptState(val value: String) {
        @SerializedName(value = "inprogress") INPROGRESS("inprogress"),
        @SerializedName(value = "finished") FINISHED("finished"),
        @SerializedName(value = "overdue") OVERDUE("overdue"),
        @SerializedName(value = "abandoned") ABANDONED("abandoned")
    }
}
