package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCourseTeacher(
    @SerializedName("teacher_key")
    val teacherKey: String? = null,

    @SerializedName("teacher_code")
    val teacherCode: String? = null,

    @SerializedName("teacher_fullname")
    val teacherFullName: String? = null,

    @SerializedName("teacher_email")
    val teacherEmail: String? = null
)