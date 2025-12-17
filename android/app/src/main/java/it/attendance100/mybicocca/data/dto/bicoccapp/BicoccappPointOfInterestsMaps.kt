package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappPointOfInterestsMaps(
    @SerializedName("filters")
    val filters: List<BicoccappMapFilter> = emptyList(),

    @SerializedName("map_locations")
    val mapLocations: List<BicoccappMapLocation> = emptyList()
)

