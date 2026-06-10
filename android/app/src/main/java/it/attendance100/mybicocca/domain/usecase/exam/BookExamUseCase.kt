package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookExamRequest
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Books a seat on an exam call (appello), triggered by the confirm action of the
 * booking sheet in the registry tab. Hits Esse3 directly (bookings are never cached)
 * and, as a side effect, invalidates the calendar's exam source so the new exam shows
 * up there immediately. Returns a locally built confirmation; throws on failure.
 */
class BookExamUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        key: ExamCallKey,
        request: BookExamRequest,
    ): ExamBooking = repository.bookExam(careerId, key, request)
}
