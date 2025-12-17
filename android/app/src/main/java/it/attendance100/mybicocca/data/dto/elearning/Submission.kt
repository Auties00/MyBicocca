package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class Submission(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("attemptnumber") val attemptNumber: Int? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("timestarted") val timeStarted: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("assignment") val assignment: Int? = null,
    @SerializedName("latest") val latest: Int? = null,
    @SerializedName("plugins") val plugins: List<SubmissionPlugin>? = null
)
