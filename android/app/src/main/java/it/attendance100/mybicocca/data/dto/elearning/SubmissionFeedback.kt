package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmissionFeedback(
    @SerializedName("grade") val grade: SubmissionGrade? = null,
    @SerializedName("gradefordisplay") val gradeForDisplay: String? = null,
    @SerializedName("gradeddate") val gradedDate: Int? = null
)
