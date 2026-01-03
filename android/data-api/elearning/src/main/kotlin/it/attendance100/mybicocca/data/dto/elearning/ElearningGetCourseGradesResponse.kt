package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetCourseGradesResponse(
    @SerialName("grades")
    val grades: List<ElearningCourseGrade> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse
