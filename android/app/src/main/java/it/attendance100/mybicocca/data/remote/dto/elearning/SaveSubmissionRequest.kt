package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SaveSubmissionRequest(
    @SerializedName("assignmentid") val assignmentId: Int,
    @SerializedName("plugindata") val pluginData: SaveSubmissionPluginData
)

data class SaveSubmissionPluginData(
    @SerializedName("onlinetext_editor") val onlineTextEditor: SaveSubmissionOnlineTextEditor? = null,
    @SerializedName("files_filemanager") val filesFileManager: Int? = null
)

data class SaveSubmissionOnlineTextEditor(
    @SerializedName("text") val text: String,
    @SerializedName("format") val format: Int? = null,
    @SerializedName("itemid") val itemId: Int? = null
)
