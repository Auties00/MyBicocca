package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class PostAttachment(
    @SerializedName("filename") val fileName: String? = null,
    @SerializedName("filepath") val filePath: String? = null,
    @SerializedName("filesize") val fileSize: Int? = null,
    @SerializedName("fileurl") val fileUrl: String? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("mimetype") val mimeType: String? = null,
    @SerializedName("isexternalfile") val isExternalFile: Boolean? = null,
    @SerializedName("repositorytype") val repositoryType: String? = null,
    @SerializedName("icon") val icon: String? = null
)