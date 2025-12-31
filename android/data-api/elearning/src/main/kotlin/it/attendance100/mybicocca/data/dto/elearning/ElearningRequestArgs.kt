package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
sealed interface ElearningRequestArgs<RESPONSE_DATA : ElearningResponseData> {
    val methodName: String
}