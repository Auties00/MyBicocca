package it.attendance100.mybicocca.data.dto.esse3

import com.google.gson.annotations.SerializedName

data class Esse3AttachmentUploadResponse(
    @SerializedName("ok")
    val statusCode: Int? = null,

    @SerializedName("allegatoId")
    val attachmentId: Int? = null
)