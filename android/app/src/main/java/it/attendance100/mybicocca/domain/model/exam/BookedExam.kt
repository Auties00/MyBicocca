package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * One row of the student's exam booking history (prenotazione), upcoming or past, as
 * returned by Esse3's `/prenotazioni` list. Rendered by the booked-exams modal in the
 * registry tab, split into upcoming ("Attive") and already-sat ("Passate") sections.
 *
 * The list response already carries every field the booking detail modal renders, so no
 * per-appello detail call is needed.
 *
 * @property key Identity of the exam call the booking belongs to.
 * @property applicationListId Esse3 prenotazione id, used to acknowledge a published
 *   outcome; null when the server omits it.
 * @property studentId The booking's `stuId` (career-of-the-student id), required to
 *   cancel via DELETE `/appelli/{cds}/{ad}/{app}/iscritti/{studentId}`. Distinct from the
 *   student's `matId`: cancelling with `matId` returns 404.
 * @property activityChoiceId Esse3 `adsceId` of the libretto row the booking refers to.
 * @property activityDescription Name of the teaching activity as it appears in the
 *   student's libretto.
 * @property examCallDescription Name of the call itself; Esse3 often duplicates the
 *   activity description here.
 * @property examType Exam mode (written/oral/joint/separate) declared on the call.
 * @property callType Whether the call is a final or partial test.
 * @property examDateTime Date and time of the student's shift (turno).
 * @property classroomDescription Room assigned to the shift, when published.
 * @property buildingDescription Building of the assigned room, when published.
 * @property credits CFU weight of the booked activity.
 * @property examModeDescription Human description of the exam mode, e.g. "Scritto".
 * @property position The student's position in the enrollment list.
 * @property bookingDate When the booking was placed.
 * @property cancellableUntil End of the call's enrollment window (`dataFineIscr`) —
 *   Esse3 also accepts booking cancellations up to this date.
 * @property studentNote Note the student typed when placing the booking.
 * @property grade Outcome of a past appello, carried inline by `/prenotazioni`. Only
 *   meaningful once the teacher has published it: the same row reports `superatoFlg=0`
 *   before publication, which would read as "non superato", so the grade is interpreted
 *   only when [outcomePublished] is true and stays [ExamGrade.Unknown] otherwise.
 * @property outcomePublished True when the outcome has been published (`pubblId` present).
 * @property publishedNote Public note the teacher attached to the published outcome.
 */
data class BookedExam(
    val key: ExamCallKey,
    val applicationListId: Long?,
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
    val cancellableUntil: LocalDate?,
    val studentNote: String?,
    val grade: ExamGrade = ExamGrade.Unknown,
    val outcomePublished: Boolean = false,
    val publishedNote: String? = null,
)
