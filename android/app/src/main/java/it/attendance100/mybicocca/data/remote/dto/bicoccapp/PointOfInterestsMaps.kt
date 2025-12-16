package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param filters
 * @param mapLocations
 */


data class PointOfInterestsMaps(

    @SerializedName("filters")
    val filters: List<Any>? = null,

    @SerializedName("map_locations")
    val mapLocations: List<Any>? = null

)

