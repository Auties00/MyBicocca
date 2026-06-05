package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.model.internship.SavedOpportunity
import it.attendance100.mybicocca.domain.repository.SavedOpportunityRepository
import javax.inject.Inject

// Used by the opportunity detail once the (currently admin-gated) opportunity catalog
// is reachable; the bookmark store itself is fully functional today.
class SaveOpportunityUseCase @Inject constructor(
    private val repository: SavedOpportunityRepository,
) {
    suspend operator fun invoke(opportunity: SavedOpportunity) = repository.save(opportunity)
}
