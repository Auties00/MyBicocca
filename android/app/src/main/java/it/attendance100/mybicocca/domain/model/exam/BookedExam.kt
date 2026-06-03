package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
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
    val examType: ExamType,
    val callType: ExamCallType,
    val examDateTime: LocalDateTime?,
    val classroomDescription: String?,
    val buildingDescription: String?,
    val credits: Float?,
    val examModeDescription: String?,
    val position: Int?,
    val bookingDate: LocalDateTime?,
    // End of the call's enrollment window (dataFineIscr) — Esse3 also allows
    // cancelling the booking up to this date.
    val cancellableUntil: LocalDate?,
    val studentNote: String?,
)
