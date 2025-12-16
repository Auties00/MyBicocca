package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ContentTemplate(
    @SerializedName("id") val id: String? = null,
    @SerializedName("html") val html: String? = null
)