package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostAuthorUrls(
    @SerializedName("profile") val profile: String? = null,
    @SerializedName("profileimage") val profileImage: String? = null
)
