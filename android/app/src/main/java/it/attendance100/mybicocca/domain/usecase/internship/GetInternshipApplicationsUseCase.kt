package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.repository.InternshipRepository
import javax.inject.Inject

class GetInternshipApplicationsUseCase @Inject constructor(
    private val repository: InternshipRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<InternshipApplication> =
        repository.getApplications(careerId)
}
