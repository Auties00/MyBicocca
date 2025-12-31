package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCourseAppeal(
    @SerialName("cdsId")
    val id: Int,
    @SerialName("type")
    val type: String,
    @SerialName("activityId")
    val activityId: Int,
    @SerialName("activityAppealId")
    val activityAppealId: Int,
    @SerialName("activityItemId")
    val activityItemId: Int,
    @SerialName("studentId")
    val studentId: Int,
    @SerialName("appealDate")
    val appealDate: String,
    @SerialName("date")
    val date: String,
    @SerialName("time")
    val time: String,
    @SerialName("appealDescr")
    val appealDescription: String,
    @SerialName("courseDescr")
    val courseDescription: String,
    @SerialName("sessionDescr")
    val sessionDescription: String,
    @SerialName("typeAppealCode")
    val typeAppealCode: String,
    @SerialName("position")
    val position: Int,
    @SerialName("status")
    val status: String,
    @SerialName("session_booked")
    val sessionBooked: Boolean
)