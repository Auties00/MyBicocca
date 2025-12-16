package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class RemoveSubmissionRequest(
    @SerializedName("assignid") val assignId: Int,
    @SerializedName("userid") val userId: Int? = null
)
