package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.SearchHistoryStore
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SearchHistoryRepository] backed by the DataStore-based [SearchHistoryStore]; all
 * semantics (ordering, dedup, cap, pick preservation) live in the store.
 */
@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val store: SearchHistoryStore,
) : SearchHistoryRepository {

    override fun observeHistory(accountId: AccountId): Flow<List<SearchHistoryEntry>> =
        store.history(accountId)

    override suspend fun add(accountId: AccountId, query: String, pickedKey: String?) =
        store.add(accountId, query, pickedKey)

    override suspend fun remove(accountId: AccountId, query: String) = store.remove(accountId, query)

    override suspend fun clear(accountId: AccountId) = store.clear(accountId)
}
