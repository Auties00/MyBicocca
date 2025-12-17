package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappAlert(
    @SerializedName("event_id")
    val eventId: Long? = null,
    @SerializedName("alert_id")
    val alertId: Long? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("read")
    val read: Boolean? = null,
    @SerializedName("rows")
    val rows: List<String> = emptyList()
)

