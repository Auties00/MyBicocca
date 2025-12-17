package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PreviousAttempt(
    @SerializedName("attemptnumber") val attemptNumber: Int? = null,
    @SerializedName("submission") val submission: Submission? = null,
    @SerializedName("grade") val grade: SubmissionGrade? = null
)
