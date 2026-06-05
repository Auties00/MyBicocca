package it.attendance100.mybicocca.domain.usecase.enrollment

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import javax.inject.Inject

class GetEnrollmentHistoryUseCase @Inject constructor(
    private val repository: EnrollmentRepository,
) {
    suspend operator fun invoke(careerId: CareerId): EnrollmentHistory =
        repository.getHistory(careerId)
}
