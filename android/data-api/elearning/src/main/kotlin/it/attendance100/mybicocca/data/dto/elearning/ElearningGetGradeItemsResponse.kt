package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetGradeItemsResponse(
    @SerialName("usergrades")
    val userGrades: List<ElearningUserGrades> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse

@Serializable
data class ElearningUserGrades(
    @SerialName("courseid")
    val courseId: Int,
    @SerialName("courseidnumber")
    val courseIdNumber: String? = null,
    @SerialName("userid")
    val userId: Int,
    @SerialName("userfullname")
    val userFullName: String? = null,
    @SerialName("useridnumber")
    val userIdNumber: String? = null,
    @SerialName("maxdepth")
    val maxDepth: Int? = null,
    @SerialName("gradeitems")
    val gradeItems: List<ElearningGradeItem> = emptyList()
)
