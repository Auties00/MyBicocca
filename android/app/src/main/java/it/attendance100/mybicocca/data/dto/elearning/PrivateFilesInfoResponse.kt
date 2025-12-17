package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PrivateFilesInfoResponse(
    @SerializedName("filecount") val fileCount: Int? = null,
    @SerializedName("foldercount") val folderCount: Int? = null,
    @SerializedName("filesize") val fileSize: Int? = null,
    @SerializedName("filesizewithoutreferences") val fileSizeWithoutReferences: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)