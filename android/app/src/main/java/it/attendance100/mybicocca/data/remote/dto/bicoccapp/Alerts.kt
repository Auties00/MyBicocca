package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param alertsToRead
 * @param alerts
 */


data class Alerts(

    @SerializedName("alerts_to_read")
    val alertsToRead: Int? = null,

    @SerializedName("alerts")
    val alerts: List<Any>? = null

)

