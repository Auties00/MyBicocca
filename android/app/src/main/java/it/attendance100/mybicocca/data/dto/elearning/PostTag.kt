package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostTag(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("tagid") val tagId: Int? = null,
    @SerializedName("isstandard") val isStandard: Boolean? = null,
    @SerializedName("displayname") val displayName: String? = null,
    @SerializedName("flag") val flag: Boolean? = null,
    @SerializedName("urls") val urls: PostTagUrls? = null
)
