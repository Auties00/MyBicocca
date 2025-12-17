package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostHtml(
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("taglist") val tagList: String? = null,
    @SerializedName("authorsubheading") val authorSubheading: String? = null
)
