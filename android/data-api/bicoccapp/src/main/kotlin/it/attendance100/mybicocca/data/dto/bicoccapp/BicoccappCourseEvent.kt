package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCourseEvent(
    @SerialName("eventId")
    val eventId: String? = null,

    @SerialName("courseName")
    val courseName: String? = null,

    @SerialName("courseCode")
    val courseCode: String? = null,

    @SerialName("partition")
    val partition: String? = null,

    @SerialName("date")
    val date: String? = null,

    @SerialName("time")
    val time: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("day")
    val dayOfWeek: String? = null,

    @SerialName("roomCode")
    val roomCode: String? = null,

    @SerialName("room")
    val room: String? = null,

    @SerialName("canceled")
    val canceled: String? = null,

    @SerialName("maps")
    val maps: String? = null,

    @SerialName("coordinates")
    val coordinates: BicoccappEventCoordinates? = null,

    @SerialName("teachers")
    val teachers: List<BicoccappCourseTeacher> = emptyList(),

    @SerialName("activityCode")
    val activityCode: String? = null,

    @SerialName("session_booked")
    val sessionBooked: Boolean? = null
)