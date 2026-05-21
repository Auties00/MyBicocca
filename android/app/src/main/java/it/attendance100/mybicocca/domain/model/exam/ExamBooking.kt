package it.attendance100.mybicocca.domain.model.exam

data class ExamBooking(
    val key: ExamCallKey,
    val applicationListId: Long?,
    val studentId: Long?,
    val activityChoiceId: Long?,
)
