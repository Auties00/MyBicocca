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
    // Outcome of a past appello, carried inline by /prenotazioni. Only meaningful once
    // the teacher has published it: the same row reports superatoFlg=0 before publication,
    // which would read as "non superato", so the grade is interpreted ONLY when
    // outcomePublished is true (pubblId present) — otherwise it stays Unknown.
    val grade: ExamGrade = ExamGrade.Unknown,
    val outcomePublished: Boolean = false,
    val publishedNote: String? = null,
)
