package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Chunked file upload request
 *
 * @param filename
 * @param chunkNumber
 * @param totalChunks
 * @param chunkSize
 * @param totalSize
 * @param `data`
 */


data class ChunkUploadRequest(

    @SerializedName("filename")
    val filename: String? = null,

    @SerializedName("chunkNumber")
    val chunkNumber: Int? = null,

    @SerializedName("totalChunks")
    val totalChunks: Int? = null,

    @SerializedName("chunkSize")
    val chunkSize: Int? = null,

    @SerializedName("totalSize")
    val totalSize: Int? = null,

    @SerializedName("data")
    val `data`: java.io.File? = null

)

