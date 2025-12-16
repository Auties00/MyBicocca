package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class DraftFile(
    @SerializedName("contextid") val contextId: Int,
    @SerializedName("component") val component: String,
    @SerializedName("filearea") val fileArea: String,
    @SerializedName("itemid") val itemId: Int,
    @SerializedName("filepath") val filePath: String,
    @SerializedName("filename") val fileName: String,
    @SerializedName("isdir") val isDir: Boolean,
    @SerializedName("isimage") val isImage: Boolean,
    @SerializedName("timemodified") val timeModified: Int,
    @SerializedName("timecreated") val timeCreated: Int,
    @SerializedName("filesize") val fileSize: Int,
    @SerializedName("author") val author: String,
    @SerializedName("license") val license: String,
    @SerializedName("filenameshort") val fileNameShort: String,
    @SerializedName("filesizeformatted") val fileSizeFormatted: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("timecreatedformatted") val timeCreatedFormatted: String,
    @SerializedName("timemodifiedformatted") val timeModifiedFormatted: String,
    @SerializedName("url") val url: URI,
    @SerializedName("urls") val urls: StoredFileUrls? = null,
    @SerializedName("html") val html: StoredFileHtml? = null
)
