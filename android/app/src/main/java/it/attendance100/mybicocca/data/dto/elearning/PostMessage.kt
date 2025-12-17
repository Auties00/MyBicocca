package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostMessage(
    @SerializedName("type") val type: String? = null,
    @SerializedName("message") val message: String? = null
)
