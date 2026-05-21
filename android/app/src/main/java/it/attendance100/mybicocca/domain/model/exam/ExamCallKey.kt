package it.attendance100.mybicocca.domain.model.exam

data class ExamCallKey(
    val courseOfStudyId: Long,
    val activityId: Long,
    val callId: Int,
)
