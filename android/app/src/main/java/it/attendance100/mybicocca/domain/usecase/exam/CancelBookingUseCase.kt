package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Cancels an existing exam booking, triggered from the booked-exams modal in the
 * registry tab; Esse3 accepts the cancellation up to the call's enrollment deadline.
 * The studentId is the booking's `stuId`, not the user's `matId`. As a side effect the
 * calendar's exam source is invalidated so the event disappears. Throws on failure.
 */
class CancelBookingUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, key: ExamCallKey, studentId: Long) =
        repository.cancelBooking(careerId, key, studentId)
}
