package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ErrorResponse(
    @SerialName("statusCode")
    val statusCode: Int? = null,

    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("retErrMsg")
    val errorMessage: String = "",

    @SerialName("errDetails")
    val errorDetails: List<Esse3ErrorDetail> = emptyList()
)

@Serializable
data class Esse3ErrorDetail(
    @SerialName("errorType")
    val errorType: String,

    @SerialName("value")
    val value: String? = null,

    @SerialName("rawValue")
    val rawValue: String? = null
)