package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class QuizAccessInformation(
    @SerializedName("canattempt") val canAttempt: Boolean? = null,
    @SerializedName("canmanage") val canManage: Boolean? = null,
    @SerializedName("canpreview") val canPreview: Boolean? = null,
    @SerializedName("canreviewmyattempts") val canReviewMyAttempts: Boolean? = null,
    @SerializedName("canviewreports") val canViewReports: Boolean? = null,
    @SerializedName("accessrules") val accessRules: List<String>? = null,
    @SerializedName("activerulenames") val activeRuleNames: List<String>? = null,
    @SerializedName("preventaccessreasons") val preventAccessReasons: List<String>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
