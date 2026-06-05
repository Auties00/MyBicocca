package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationApplicationId
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisDraft
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class SubmitThesisUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        applicationId: GraduationApplicationId,
        committeeRegulationId: Long?,
        draft: ThesisDraft,
    ) = repository.submitThesis(careerId, applicationId, committeeRegulationId, draft)
}
