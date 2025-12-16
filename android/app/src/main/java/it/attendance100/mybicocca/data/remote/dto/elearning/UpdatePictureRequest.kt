package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UpdatePictureRequest(
    @SerializedName("draftitemid") val draftItemId: Int,
    @SerializedName("delete") val delete: Boolean? = null,
    @SerializedName("userid") val userId: Int? = 0
)