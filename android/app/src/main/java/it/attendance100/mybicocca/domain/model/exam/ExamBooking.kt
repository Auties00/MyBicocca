package it.attendance100.mybicocca.domain.model.exam

/**
 * Confirmation of a freshly placed exam booking, shown by the booking sheet right after
 * the enrollment POST succeeds. Esse3's booking POST returns no body, so this is built
 * from values already known client-side.
 *
 * @property key Identity of the exam call that was booked.
 * @property applicationListId Esse3 prenotazione id; null on a fresh booking because the
 *   POST response carries none.
 * @property studentId The booking's `stuId` (career-of-the-student id), the id later
 *   needed to cancel the booking.
 * @property activityChoiceId Esse3 `adsceId` of the libretto row that was booked.
 */
data class ExamBooking(
    val key: ExamCallKey,
    val applicationListId: Long?,
    val studentId: Long?,
    val activityChoiceId: Long?,
)
