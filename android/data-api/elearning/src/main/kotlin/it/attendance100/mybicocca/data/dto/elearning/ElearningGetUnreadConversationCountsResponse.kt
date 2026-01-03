package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetUnreadConversationCountsResponse(
    @SerialName("favourites")
    val favourites: Int = 0,
    @SerialName("types")
    val types: ElearningConversationTypeCounts? = null
) : ElearningResponse

@Serializable
data class ElearningConversationTypeCounts(
    @SerialName("1")
    val individual: Int = 0,
    @SerialName("2")
    val group: Int = 0,
    @SerialName("3")
    val self: Int = 0
)
