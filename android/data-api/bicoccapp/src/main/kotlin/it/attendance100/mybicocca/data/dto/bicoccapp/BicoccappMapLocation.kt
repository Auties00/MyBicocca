package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappMapLocation(
    @SerialName("name")
    val name: String? = null,
    @SerialName("latitude")
    val latitude: String? = null,
    @SerialName("longitude")
    val longitude: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("type")
    val type: String? = null
)