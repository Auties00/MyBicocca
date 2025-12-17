package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmissionGrade(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("assignment") val assignment: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("attemptnumber") val attemptNumber: Int? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("grader") val grader: Int? = null,
    @SerializedName("grade") val grade: String? = null
)
