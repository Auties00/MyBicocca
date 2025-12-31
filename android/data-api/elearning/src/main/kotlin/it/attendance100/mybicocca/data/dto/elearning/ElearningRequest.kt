package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningRequest<REQUEST_ARGS : ElearningRequestArgs<*>>(
    @SerialName("index")
    val index: Int,
    @SerialName("methodname")
    val methodName: String,
    @SerialName("args")
    val args: REQUEST_ARGS
)