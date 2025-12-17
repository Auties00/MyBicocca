package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserExamsCareer(
    @SerializedName("notations")
    val notations: List<BicoccappUserExamsCareerNotation> = emptyList(),

    @SerializedName("exams")
    val exams: List<BicoccappUserExamsCareerEntry> = emptyList(),

    @SerializedName("remainings")
    val remainings: List<BicoccappUserExamsCareerEntry> = emptyList()
)

