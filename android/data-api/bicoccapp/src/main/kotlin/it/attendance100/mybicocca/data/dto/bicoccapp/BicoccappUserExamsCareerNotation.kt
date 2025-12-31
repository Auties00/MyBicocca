package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserExamsCareerNotation(
    @SerialName("year")
    val year: Int? = null,

    @SerialName("dateExam")
    val dateExam: String? = null,

    @SerialName("laudFlag")
    val laudFlag: Int? = null,

    @SerialName("grade")
    val grade: Float? = null
)