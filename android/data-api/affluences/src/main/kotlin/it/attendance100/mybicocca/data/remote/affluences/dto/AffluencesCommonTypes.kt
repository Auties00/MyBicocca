package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic envelope used by the Affluences app API (`api.affluences.com`).
 *
 * Every successful response from the app API wraps its payload in a `data` field.
 * The reservation API (`reservation.affluences.com`) returns bare payloads instead.
 *
 * @property data The wrapped payload.
 */
@Serializable
class AffluencesDataEnvelope<T>(
    @SerialName("data")
    val data: T
)

/**
 * Crowd level estimate used when a site reports estimated (rather than exact) occupancy.
 */
@Serializable
enum class AffluencesCrowdLevel {
    /**
     * Low crowd, no significant waiting.
     */
    @SerialName("FLUID")
    FLUID,

    /**
     * Moderate crowd.
     */
    @SerialName("MODERATE")
    MODERATE,

    /**
     * High crowd, expect waiting.
     */
    @SerialName("DENSE")
    DENSE
}

/**
 * Trend of an occupancy, flow, or waiting time value compared to the previous forecast slot.
 */
@Serializable
enum class AffluencesEvolution {
    /**
     * The value is increasing compared to the previous slot.
     */
    @SerialName("INCREASE")
    INCREASE,

    /**
     * The value is decreasing compared to the previous slot.
     */
    @SerialName("DECREASE")
    DECREASE
}

/**
 * Geographic coordinates of a site.
 *
 * @property latitude The latitude in decimal degrees.
 * @property longitude The longitude in decimal degrees.
 */
@Serializable
data class AffluencesCoordinates(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double
)
