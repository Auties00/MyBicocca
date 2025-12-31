package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCalendarDay(
    @SerialName("day")
    val date: String? = null,

    @SerialName("events")
    val events: List<BicoccappCourseEvent> = emptyList(),

    @SerialName("appeals")
    val appeals: List<BicoccappCourseAppeal> = emptyList()
)