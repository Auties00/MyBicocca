package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserExamsCareer(
    @SerialName("notations")
    val notations: List<BicoccappUserExamsCareerNotation> = emptyList(),

    @SerialName("exams")
    val exams: List<BicoccappUserExamsCareerEntry> = emptyList(),

    @SerialName("remainings")
    val remainings: List<BicoccappUserExamsCareerEntry> = emptyList()
)

