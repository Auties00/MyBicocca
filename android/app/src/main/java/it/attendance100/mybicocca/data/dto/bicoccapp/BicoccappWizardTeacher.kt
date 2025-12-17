package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappWizardTeacher(
    @SerializedName("teacher_surname")
    val teacherSurname: String? = null,

    @SerializedName("teacher_email")
    val teacherEmail: String? = null,

    @SerializedName("teacher_code")
    val teacherCode: String? = null,

    @SerializedName("teacher_name")
    val teacherName: String? = null,

    @SerializedName("teacher_id")
    val teacherId: Int? = null
)