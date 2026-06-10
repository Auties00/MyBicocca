package it.attendance100.mybicocca.domain.usecase.enrollment

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import javax.inject.Inject

/**
 * Resolves the Esse3 web URL of the official annual re-enrollment flow ("iscrizione
 * anni successivi"), which the enrollments sub-screen opens in the browser when the
 * student taps the renewal action — no student REST submission path exists, so the web
 * flow is the only way to complete the renewal.
 */
class GetRenewalWebUrlUseCase @Inject constructor(
    private val repository: EnrollmentRepository,
) {
    operator fun invoke(careerId: CareerId): String =
        repository.renewalWebUrl(careerId)
}
