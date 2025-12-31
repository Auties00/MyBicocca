package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardTeacher(
    @SerialName("teacher_surname")
    val teacherSurname: String? = null,

    @SerialName("teacher_email")
    val teacherEmail: String? = null,

    @SerialName("teacher_code")
    val teacherCode: String? = null,

    @SerialName("teacher_name")
    val teacherName: String? = null,

    @SerialName("teacher_id")
    val teacherId: Int? = null
)