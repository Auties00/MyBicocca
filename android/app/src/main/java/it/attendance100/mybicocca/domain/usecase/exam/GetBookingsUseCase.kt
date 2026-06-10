package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Loads the student's full exam booking history (upcoming and past) live from Esse3 for
 * the booked-exams modal in the registry tab, which splits it into "Attive" and
 * "Passate" sections. Bookings are deliberately not cached locally. Throws on failure.
 */
class GetBookingsUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<BookedExam> =
        repository.getBookings(careerId)
}
