package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName


data class BicoccappErrorResponse(
    @SerializedName("error")
    val error: String? = null
)

