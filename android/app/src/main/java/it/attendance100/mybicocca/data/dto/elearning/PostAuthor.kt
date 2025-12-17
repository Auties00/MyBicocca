package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostAuthor(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("isdeleted") val isDeleted: Boolean? = null,
    @SerializedName("groups") val groups: List<Any>? = null, // Type Any as it is an empty object in spec
    @SerializedName("urls") val urls: PostAuthorUrls? = null
)
