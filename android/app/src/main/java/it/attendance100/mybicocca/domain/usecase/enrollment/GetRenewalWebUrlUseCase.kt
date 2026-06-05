package it.attendance100.mybicocca.domain.usecase.enrollment

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import javax.inject.Inject

class GetRenewalWebUrlUseCase @Inject constructor(
    private val repository: EnrollmentRepository,
) {
    operator fun invoke(careerId: CareerId): String =
        repository.renewalWebUrl(careerId)
}
