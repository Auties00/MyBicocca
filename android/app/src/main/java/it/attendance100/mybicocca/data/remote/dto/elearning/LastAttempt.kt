package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class LastAttempt(
    @SerializedName("submission") val submission: Submission? = null,
    @SerializedName("teamsubmission") val teamSubmission: Submission? = null,
    @SerializedName("submissiongroup") val submissionGroup: Int? = null,
    @SerializedName("submissiongroupmemberswhoneedtosubmit") val submissionGroupMembersWhoNeedToSubmit: List<Int>? = null,
    @SerializedName("submissionsenabled") val submissionsEnabled: Boolean? = null,
    @SerializedName("locked") val locked: Boolean? = null,
    @SerializedName("graded") val graded: Boolean? = null,
    @SerializedName("canedit") val canEdit: Boolean? = null,
    @SerializedName("caneditowner") val canEditOwner: Boolean? = null,
    @SerializedName("cansubmit") val canSubmit: Boolean? = null,
    @SerializedName("extensionduedate") val extensionDueDate: Int? = null,
    @SerializedName("blindmarking") val blindMarking: Boolean? = null,
    @SerializedName("gradingstatus") val gradingStatus: String? = null,
    @SerializedName("usergroups") val userGroups: List<Int>? = null
)
