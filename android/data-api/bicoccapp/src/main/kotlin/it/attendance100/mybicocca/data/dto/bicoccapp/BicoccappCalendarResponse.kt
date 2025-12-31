package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCalendarResponse(
    @SerialName("calendar")
    val days: List<BicoccappCalendarDay> = emptyList()
)

