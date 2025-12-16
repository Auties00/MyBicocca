package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GradingSummary(
    @SerializedName("participantcount") val participantCount: Int? = null,
    @SerializedName("submissiondraftscount") val submissionDraftsCount: Int? = null,
    @SerializedName("submissionsenabled") val submissionsEnabled: Boolean? = null,
    @SerializedName("submissionssubmittedcount") val submissionsSubmittedCount: Int? = null,
    @SerializedName("submissionsneedgradingcount") val submissionsNeedGradingCount: Int? = null,
    @SerializedName("warnofungroupedusers") val warnOfUngroupedUsers: String? = null
)
