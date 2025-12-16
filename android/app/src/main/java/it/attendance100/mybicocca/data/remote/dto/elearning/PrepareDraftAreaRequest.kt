package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class PrepareDraftAreaRequest(
    @SerializedName("postid") val postId: Int,
    @SerializedName("area") val area: String,
    @SerializedName("draftitemid") val draftItemId: Int? = null
)
