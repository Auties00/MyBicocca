package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddUserPrivateFilesRequest(
    @SerializedName("draftid") val draftId: Int
)