package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.degreeaward.GraduationHub
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class GetGraduationHubUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(careerId: CareerId): GraduationHub = repository.getHub(careerId)
}
