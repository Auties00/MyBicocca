package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCalendar(
    @SerializedName("day")
    val day: String? = null,

    @SerializedName("events")
    val events: List<BicoccappCourseEvent> = emptyList(),

    @SerializedName("appeals")
    val appeals: List<BicoccappCourseAppeal> = emptyList()
)