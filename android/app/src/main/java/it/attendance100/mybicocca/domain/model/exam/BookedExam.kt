package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDateTime

data class BookedExam(
    val key: ExamCallKey,
    val applicationListId: Long?,
    // The booking's `stuId` (career-of-the-student id) — required to cancel via
    // DELETE /appelli/{cds}/{ad}/{app}/iscritti/{studentId}. NOT the same as the
    // student's matId; cancelling with matId 404s.
    val studentId: Long?,
    val activityChoiceId: Long?,
    val activityDescription: String?,
    val examCallDescription: String?,
    val examDateTime: LocalDateTime?,
    val classroomDescription: String?,
    val buildingDescription: String?,
    val position: Int?,
    val bookingDate: LocalDateTime?,
    val studentNote: String?,
)
