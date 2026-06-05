package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.repository.SavedOpportunityRepository
import javax.inject.Inject

class RemoveSavedOpportunityUseCase @Inject constructor(
    private val repository: SavedOpportunityRepository,
) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
