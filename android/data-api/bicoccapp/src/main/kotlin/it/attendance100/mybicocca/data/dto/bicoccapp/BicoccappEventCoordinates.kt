package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappEventCoordinates(
    @SerialName("latitude")
    val latitude: String? = null,

    @SerialName("longitude")
    val longitude: String? = null
)