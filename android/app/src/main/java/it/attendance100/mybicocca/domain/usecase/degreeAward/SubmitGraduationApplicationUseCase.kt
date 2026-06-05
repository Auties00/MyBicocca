package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationCallId
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class SubmitGraduationApplicationUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(careerId: CareerId, callId: GraduationCallId) =
        repository.submitApplication(careerId, callId)
}
