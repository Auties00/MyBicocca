package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SearchedUser(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("profileimageurl") val profileImageUrl: String? = null,
    @SerializedName("profileimageurlsmall") val profileImageUrlSmall: String? = null
)