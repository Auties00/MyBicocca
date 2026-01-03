package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetPrivateFilesInfoResponse(
    @SerialName("filecount")
    val fileCount: Int,
    @SerialName("foldercount")
    val folderCount: Int,
    @SerialName("filesize")
    val fileSize: Long,
    @SerialName("filesizewithoutreferences")
    val fileSizeWithoutReferences: Long,
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
