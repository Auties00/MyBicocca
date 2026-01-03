package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetActionEventsByTimesortResponse(
    @SerialName("events")
    val events: List<ElearningCalendarEvent> = emptyList(),
    @SerialName("firstid")
    val firstId: Int? = null,
    @SerialName("lastid")
    val lastId: Int? = null
) : ElearningResponse
