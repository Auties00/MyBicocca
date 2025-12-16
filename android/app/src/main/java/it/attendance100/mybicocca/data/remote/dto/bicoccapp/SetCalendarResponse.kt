package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param message
 * @param status
 */


data class SetCalendarResponse(

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: Int? = null

)

