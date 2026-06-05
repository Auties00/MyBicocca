package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationId
import it.attendance100.mybicocca.domain.repository.InternshipRepository
import javax.inject.Inject

class GetInternshipApplicationDetailUseCase @Inject constructor(
    private val repository: InternshipRepository,
) {
    suspend operator fun invoke(careerId: CareerId, applicationId: InternshipApplicationId): InternshipApplicationDetail =
        repository.getApplicationDetail(careerId, applicationId)
}
