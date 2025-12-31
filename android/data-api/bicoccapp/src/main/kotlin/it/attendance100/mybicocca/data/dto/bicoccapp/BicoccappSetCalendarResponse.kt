package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappSetCalendarResponse(
    @SerialName("message")
    val message: String? = null,

    @SerialName("status")
    val status: Int? = null
)

