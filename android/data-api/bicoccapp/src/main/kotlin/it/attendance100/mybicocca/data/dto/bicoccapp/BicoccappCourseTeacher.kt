package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourseTeacher(
    @SerializedName("teacher_key")
    val key: String? = null,

    @SerializedName("teacher_code")
    val code: String? = null,

    @SerializedName("teacher_fullname")
    val fullName: String? = null,

    @SerializedName("teacher_email")
    val email: String? = null
)