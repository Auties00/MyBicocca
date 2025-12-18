package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserExamsCareer(
    @SerializedName("notations")
    val notations: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserExamsCareerNotation> = emptyList(),

    @SerializedName("exams")
    val exams: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserExamsCareerEntry> = emptyList(),

    @SerializedName("remainings")
    val remainings: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserExamsCareerEntry> = emptyList()
)

