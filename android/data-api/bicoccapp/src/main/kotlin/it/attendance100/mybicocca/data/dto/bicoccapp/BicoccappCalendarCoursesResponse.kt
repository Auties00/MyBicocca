package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCalendarCoursesResponse(
    @SerialName("courses")
    val courses: List<BicoccappCourse> = emptyList()
)

