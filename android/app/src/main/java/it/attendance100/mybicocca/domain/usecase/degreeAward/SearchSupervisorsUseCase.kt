package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.degreeaward.SupervisorCandidate
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class SearchSupervisorsUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(surname: String, includeExternal: Boolean = false): List<SupervisorCandidate> =
        repository.searchSupervisors(surname, includeExternal)
}
