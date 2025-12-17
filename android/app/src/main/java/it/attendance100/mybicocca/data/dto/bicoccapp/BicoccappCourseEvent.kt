package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourseEvent(
    @SerializedName("eventId")
    val eventId: String? = null,

    @SerializedName("courseName")
    val courseName: String? = null,

    @SerializedName("courseCode")
    val courseCode: String? = null,

    @SerializedName("partition")
    val partition: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("time")
    val time: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("day")
    val dayOfWeek: String? = null,

    @SerializedName("roomCode")
    val roomCode: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("canceled")
    val canceled: String? = null,

    @SerializedName("maps")
    val maps: String? = null,

    @SerializedName("coordinates")
    val coordinates: BicoccappEventCoordinates? = null,

    @SerializedName("teachers")
    val teachers: List<BicoccappCourseTeacher> = emptyList(),

    @SerializedName("activityCode")
    val activityCode: String? = null,

    @SerializedName("session_booked")
    val sessionBooked: Boolean? = null
)