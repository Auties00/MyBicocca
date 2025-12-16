package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmitGradingFormRequest(
    @SerializedName("assignmentid") val assignmentId: Int,
    @SerializedName("userid") val userId: Int,
    @SerializedName("jsonformdata") val jsonFormData: String
)
