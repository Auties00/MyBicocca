package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappEventCoordinates(
    @SerializedName("latitude")
    val latitude: String? = null,

    @SerializedName("longitude")
    val longitude: String? = null
)