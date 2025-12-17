package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SaveSubmissionRequest(
    @SerializedName("assignmentid") val assignmentId: Int,
    @SerializedName("plugindata") val pluginData: SaveSubmissionPluginData
)
