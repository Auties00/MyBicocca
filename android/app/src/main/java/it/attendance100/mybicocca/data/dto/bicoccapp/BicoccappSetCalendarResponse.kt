package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappSetCalendarResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: Int? = null
)

