package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCalendarDay(
    @SerializedName("day")
    val date: String? = null,

    @SerializedName("events")
    val events: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseEvent> = emptyList(),

    @SerializedName("appeals")
    val appeals: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseAppeal> = emptyList()
)