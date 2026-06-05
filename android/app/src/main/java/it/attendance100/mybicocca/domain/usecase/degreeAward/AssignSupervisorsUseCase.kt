package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorAssignment
import it.attendance100.mybicocca.domain.model.degreeaward.ThesisId
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class AssignSupervisorsUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(thesisId: ThesisId, assignments: List<SupervisorAssignment>) =
        repository.assignSupervisors(thesisId, assignments)
}
