package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourseAppeal(
    @SerializedName("cdsId")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("activityId")
    val activityId: Int,
    @SerializedName("activityAppealId")
    val activityAppealId: Int,
    @SerializedName("activityItemId")
    val activityItemId: Int,
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("appealDate")
    val appealDate: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("appealDescr")
    val appealDescription: String,
    @SerializedName("courseDescr")
    val courseDescription: String,
    @SerializedName("sessionDescr")
    val sessionDescription: String,
    @SerializedName("typeAppealCode")
    val typeAppealCode: String,
    @SerializedName("position")
    val position: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("session_booked")
    val sessionBooked: Boolean
)