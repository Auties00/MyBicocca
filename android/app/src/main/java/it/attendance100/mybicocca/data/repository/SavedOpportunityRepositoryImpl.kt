package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.internship.SavedOpportunityDao
import it.attendance100.mybicocca.data.local.internship.SavedOpportunityEntity
import it.attendance100.mybicocca.domain.model.internship.SavedOpportunity
import it.attendance100.mybicocca.domain.repository.SavedOpportunityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SavedOpportunityRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dao: SavedOpportunityDao,
) : SavedOpportunityRepository {

    override fun observe(): Flow<List<SavedOpportunity>> =
        sessionManager.activeAccount.flatMapLatest { account ->
            if (account == null) {
                flowOf(emptyList())
            } else {
                dao.observe(account.id.value).map { rows -> rows.map { it.toDomain() } }
            }
        }

    override suspend fun save(opportunity: SavedOpportunity) {
        dao.upsert(
            SavedOpportunityEntity(
                accountId = requireAccountId(),
                opportunityId = opportunity.id,
                title = opportunity.title,
                company = opportunity.company,
                url = opportunity.url,
                savedAt = opportunity.savedAt.toEpochMilli(),
            ),
        )
    }

    override suspend fun remove(id: String) {
        dao.delete(requireAccountId(), id)
    }

    override suspend fun isSaved(id: String): Boolean =
        dao.isSaved(requireAccountId(), id)

    private fun requireAccountId(): String =
        sessionManager.activeAccount.value?.id?.value
            ?: error("No active account; cannot access saved opportunities.")

    private fun SavedOpportunityEntity.toDomain(): SavedOpportunity = SavedOpportunity(
        id = opportunityId,
        title = title,
        company = company,
        url = url,
        savedAt = Instant.ofEpochMilli(savedAt),
    )
}
