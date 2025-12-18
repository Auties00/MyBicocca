package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName
data class BicoccappUserAppealsCareer(
    @SerializedName("courses")
    val courses: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamSession> = emptyList()
)