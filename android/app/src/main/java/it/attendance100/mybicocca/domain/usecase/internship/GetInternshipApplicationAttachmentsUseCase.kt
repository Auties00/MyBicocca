package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationId
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import it.attendance100.mybicocca.domain.repository.InternshipRepository
import javax.inject.Inject

class GetInternshipApplicationAttachmentsUseCase @Inject constructor(
    private val repository: InternshipRepository,
) {
    suspend operator fun invoke(careerId: CareerId, applicationId: InternshipApplicationId): List<InternshipAttachment> =
        repository.getApplicationAttachments(careerId, applicationId)
}
