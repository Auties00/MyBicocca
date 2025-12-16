package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SaveSubmissionOnlineTextEditor(
    @SerializedName("text") val text: String,
    @SerializedName("format") val format: Int? = null,
    @SerializedName("itemid") val itemId: Int? = null
)
