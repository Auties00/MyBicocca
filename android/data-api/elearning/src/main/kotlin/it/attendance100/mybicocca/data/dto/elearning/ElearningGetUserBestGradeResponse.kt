package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetUserBestGradeResponse(
    @SerialName("hasgrade")
    val hasGrade: Boolean,
    @SerialName("grade")
    val grade: Double? = null,
    @SerialName("gradetopass")
    val gradeToPass: Double? = null,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
