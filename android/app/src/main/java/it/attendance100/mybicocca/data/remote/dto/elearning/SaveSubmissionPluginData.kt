package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SaveSubmissionPluginData(
    @SerializedName("onlinetext_editor") val onlineTextEditor: SaveSubmissionOnlineTextEditor? = null,
    @SerializedName("files_filemanager") val filesFileManager: Int? = null
)
