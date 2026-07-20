package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Fetches the total number of students booked on a call (Esse3's `numIscritti`) from the
 * per-appello detail endpoint. Deliberately not part of any list read — the endpoint costs
 * 0.3–5 s per call — it backs the lazy fetch when a single booking's detail page opens.
 * Returns null when Esse3 omits the count; throws on network failure.
 */
class GetExamCallTotalBookingsUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, key: ExamCallKey): Int? =
        repository.getCallTotalBookings(careerId, key)
}
