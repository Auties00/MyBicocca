package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class StartSubmissionRequest(
    @SerializedName("assignid") val assignId: Int
)
