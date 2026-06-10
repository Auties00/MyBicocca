package it.attendance100.mybicocca.domain.usecase.enrollment

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import javax.inject.Inject

/**
 * Loads the career's annual enrollment timeline plus the derived renewal state live from
 * Esse3 for the enrollments sub-screen in the registry tab. Enrollment history is
 * deliberately not cached locally. Throws on failure.
 */
class GetEnrollmentHistoryUseCase @Inject constructor(
    private val repository: EnrollmentRepository,
) {
    suspend operator fun invoke(careerId: CareerId): EnrollmentHistory =
        repository.getHistory(careerId)
}
