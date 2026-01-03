package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetQuizAccessInfoResponse(
    @SerialName("canattempt")
    val canAttempt: Boolean,
    @SerialName("canmanage")
    val canManage: Boolean,
    @SerialName("canpreview")
    val canPreview: Boolean,
    @SerialName("canreviewmyattempts")
    val canReviewMyAttempts: Boolean,
    @SerialName("canviewreports")
    val canViewReports: Boolean,
    @SerialName("accessrules")
    val accessRules: List<String>? = null,
    @SerialName("activerulenames")
    val activeRuleNames: List<String>? = null,
    @SerialName("preventaccessreasons")
    val preventAccessReasons: List<String>? = null,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
