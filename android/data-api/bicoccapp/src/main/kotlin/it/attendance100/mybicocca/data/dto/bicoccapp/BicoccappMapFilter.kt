package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappMapFilter(
    @SerialName("type")
    val type: String? = null,
    @SerialName("color")
    val color: String? = null
)