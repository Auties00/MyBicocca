package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.internship.SavedOpportunity
import kotlinx.coroutines.flow.Flow

// Local-only bookmarks of internship opportunities (Room SSOT, scoped to the active account).
interface SavedOpportunityRepository {
    fun observe(): Flow<List<SavedOpportunity>>
    suspend fun save(opportunity: SavedOpportunity)
    suspend fun remove(id: String)
    suspend fun isSaved(id: String): Boolean
}
