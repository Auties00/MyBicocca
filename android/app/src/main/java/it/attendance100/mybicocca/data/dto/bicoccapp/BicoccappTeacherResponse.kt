package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappTeacherResponse(
    @SerializedName("teacher")
    val teacher: BicoccappTeacher? = null
)

