package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun observeHistory(accountId: AccountId): Flow<List<SearchHistoryEntry>>
    suspend fun add(accountId: AccountId, query: String)
    suspend fun remove(accountId: AccountId, query: String)
    suspend fun clear(accountId: AccountId)
}
