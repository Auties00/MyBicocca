package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Per-account persistence of recent searches and of the result picked for each query —
 * the adaptive pick memory behind the unified search.
 */
interface SearchHistoryRepository {
    /** Streams the account's history, most recent first. */
    fun observeHistory(accountId: AccountId): Flow<List<SearchHistoryEntry>>

    /**
     * Records [query] at the head of the account's history. Re-submitting an existing
     * query (compared case-insensitively) moves it to the head; a null [pickedKey]
     * preserves the pick already learned for that query, a non-null one replaces it. The
     * history is bounded — the oldest entries fall off.
     */
    suspend fun add(accountId: AccountId, query: String, pickedKey: String? = null)

    /** Removes every entry whose query equals [query], compared case-insensitively. */
    suspend fun remove(accountId: AccountId, query: String)

    /** Removes the account's entire history. */
    suspend fun clear(accountId: AccountId)
}
