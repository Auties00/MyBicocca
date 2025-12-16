package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param success
 * @param uploadId
 * @param message
 */


data class ChunkUploadResponse(

    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("uploadId")
    val uploadId: String? = null,

    @SerializedName("message")
    val message: String? = null

)

