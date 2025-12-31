package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappCourseTeacher(
    @SerialName("teacher_key")
    val key: String? = null,

    @SerialName("teacher_code")
    val code: String? = null,

    @SerialName("teacher_fullname")
    val fullName: String? = null,

    @SerialName("teacher_email")
    val email: String? = null
)