package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AssignmentGrades(
    @SerializedName("assignmentid") val assignmentId: Int? = null,
    @SerializedName("grades") val grades: List<SubmissionGrade>? = null
)
