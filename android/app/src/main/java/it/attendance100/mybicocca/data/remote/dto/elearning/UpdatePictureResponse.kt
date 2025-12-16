package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class UpdatePictureResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("profileimageurl") val profileImageUrl: URI? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)