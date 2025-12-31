package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappPointOfInterestsMaps(
    @SerialName("filters")
    val filters: List<BicoccappMapFilter> = emptyList(),

    @SerialName("map_locations")
    val mapLocations: List<BicoccappMapLocation> = emptyList()
)

