package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappAlertsResponse(
    @SerializedName("alerts_to_read")
    val alertsToRead: Int? = null,

    @SerializedName("alerts")
    val alerts: List<BicoccappAlert> = emptyList()
)

