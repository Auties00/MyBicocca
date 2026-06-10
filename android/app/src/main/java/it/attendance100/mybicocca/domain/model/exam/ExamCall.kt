package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalTime

/**
 * An exam call (appello) the student can book, sourced live from Esse3's bookable-calls
 * list for the libretto. Backs the bookable-exams list and the booking sheet in the
 * registry tab; the list response already includes the booking-sheet fields (notes,
 * president, booking mode), so no per-appello detail call is made.
 *
 * @property key Identity of the call within Esse3 (course of study, activity, call).
 * @property examCallId Esse3 internal id of the call, distinct from the positional
 *   call id inside [key].
 * @property activityChoiceId Esse3 `adsceId` of the libretto row the call belongs to;
 *   the value the booking POST requires.
 * @property activityCode University activity code, e.g. "E3101Q123".
 * @property activityDescription Name of the teaching activity.
 * @property courseOfStudyDescription Name of the course of study offering the call.
 * @property callDescription Name the teacher gave the call; often duplicates the
 *   activity description.
 * @property callDate Day the exam takes place.
 * @property callTime Start time of the exam.
 * @property enrollmentWindow Period during which the call accepts bookings.
 * @property enrolledNumber How many students are currently booked.
 * @property state Raw Esse3 state code of the call.
 * @property stateDescription Human description of the call state.
 * @property callType Whether the call is a final or partial test.
 * @property examType Exam mode (written/oral/joint/separate) declared on the call.
 * @property isReserved True when the call is reserved to a subset of students.
 * @property matId The student's enrollment id the call was resolved for.
 * @property notes Free-text notes the teacher attached to the call.
 * @property president The commission president, when Esse3 publishes one.
 * @property bookingTypeDescription Human description of the booking management mode.
 */
data class ExamCall(
    val key: ExamCallKey,
    val examCallId: Long?,
    val activityChoiceId: Long?,
    val activityCode: String?,
    val activityDescription: String?,
    val courseOfStudyDescription: String?,
    val callDescription: String?,
    val callDate: LocalDate?,
    val callTime: LocalTime?,
    val enrollmentWindow: ExamEnrollmentWindow,
    val enrolledNumber: Int?,
    val state: String?,
    val stateDescription: String?,
    val callType: ExamCallType,
    val examType: ExamType,
    val isReserved: Boolean,
    val matId: Long?,
    val notes: String?,
    val president: ExamExaminer?,
    val bookingTypeDescription: String?,
)

/**
 * A member of the exam commission as published on the call, typically the president.
 *
 * @property id Esse3 id of the examiner.
 * @property name Given name; Esse3 may publish only part of the identity.
 * @property surname Family name; Esse3 may publish only part of the identity.
 */
data class ExamExaminer(
    val id: Long?,
    val name: String?,
    val surname: String?,
)

/**
 * The period during which an exam call accepts bookings. Esse3 also allows cancelling an
 * existing booking up to [closesAt].
 *
 * @property opensAt First day bookings are accepted.
 * @property closesAt Last day bookings (and cancellations) are accepted.
 */
data class ExamEnrollmentWindow(
    val opensAt: LocalDate?,
    val closesAt: LocalDate?,
)

/**
 * Whether the call is a final exam or a mid-course partial test. Maps Esse3 `tipoAppCod`
 * ("PF" final, "PP" partial); [Other] covers any unrecognised code.
 */
enum class ExamCallType { Final, Partial, Other }
