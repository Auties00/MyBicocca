package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappPointOfInterestsResponse(
    @SerializedName("maps")
    val maps: BicoccappPointOfInterestsMaps? = null
)

