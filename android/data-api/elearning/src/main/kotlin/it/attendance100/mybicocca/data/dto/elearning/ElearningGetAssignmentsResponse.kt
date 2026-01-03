package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetAssignmentsResponse(
    @SerialName("courses")
    val courses: List<ElearningCourseAssignments> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse {
    val allAssignments: List<ElearningAssignment>
        get() = courses.flatMap { it.assignments }
}

@Serializable
data class ElearningCourseAssignments(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("assignments")
    val assignments: List<ElearningAssignment> = emptyList()
)
