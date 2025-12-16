package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param courses
 */


data class WizardCourses(

    @SerializedName("courses")
    val courses: List<Any>? = null

)

