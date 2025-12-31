package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappAlert(
    @SerialName("event_id")
    val eventId: Long? = null,
    @SerialName("alert_id")
    val alertId: Long? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("read")
    val read: Boolean? = null,
    @SerialName("rows")
    val rows: List<String> = emptyList()
)

