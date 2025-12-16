package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param courses
 */


data class CalendarCourses(

    @SerializedName("courses")
    val courses: List<Any>? = null

)

