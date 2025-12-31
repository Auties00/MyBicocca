package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappAlertsResponse(
    @SerialName("alerts_to_read")
    val alertsToRead: Int? = null,

    @SerialName("alerts")
    val alerts: List<BicoccappAlert> = emptyList()
)

