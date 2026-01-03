package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningQuizAttempt(
    @SerialName("id")
    val id: Int,
    @SerialName("quiz")
    val quiz: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("attempt")
    val attempt: Int? = null,
    @SerialName("uniqueid")
    val uniqueId: Int? = null,
    @SerialName("layout")
    val layout: String? = null,
    @SerialName("currentpage")
    val currentPage: Int? = null,
    @SerialName("preview")
    val preview: Int? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("timestart")
    val timeStart: Long? = null,
    @SerialName("timefinish")
    val timeFinish: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("timemodifiedoffline")
    val timeModifiedOffline: Long? = null,
    @SerialName("timecheckstate")
    val timeCheckState: Long? = null,
    @SerialName("sumgrades")
    val sumGrades: Double? = null,
    @SerialName("gradednotificationsenttime")
    val gradedNotificationSentTime: Long? = null
) {
    companion object {
        const val STATE_IN_PROGRESS = "inprogress"
        const val STATE_OVERDUE = "overdue"
        const val STATE_FINISHED = "finished"
        const val STATE_ABANDONED = "abandoned"
    }

    val isFinished: Boolean
        get() = state == STATE_FINISHED

    val isOverdue: Boolean
        get() = state == STATE_OVERDUE

    val isInProgress: Boolean
        get() = state == STATE_IN_PROGRESS

    val isAbandoned: Boolean
        get() = state == STATE_ABANDONED
}

@Serializable
data class ElearningGetUserAttemptsResponse(
    @SerialName("attempts")
    val attempts: List<ElearningQuizAttempt>,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
