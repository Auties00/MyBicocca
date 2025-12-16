package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmissionPlugin(
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("fileareas") val fileAreas: List<FileArea>? = null,
    @SerializedName("editorfields") val editorFields: List<EditorField>? = null
)
