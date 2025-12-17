package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName
data class BicoccappCalendarCoursesResponse(
    @SerializedName("courses")
    val courses: List<BicoccappCourse> = emptyList()
)

