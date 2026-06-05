package it.attendance100.mybicocca.domain.usecase.internship

import it.attendance100.mybicocca.domain.model.internship.SavedOpportunity
import it.attendance100.mybicocca.domain.repository.SavedOpportunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSavedOpportunitiesUseCase @Inject constructor(
    private val repository: SavedOpportunityRepository,
) {
    operator fun invoke(): Flow<List<SavedOpportunity>> = repository.observe()
}
