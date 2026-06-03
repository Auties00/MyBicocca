package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.search.SearchHistoryEntry
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSearchHistoryUseCase @Inject constructor(
    private val repository: SearchHistoryRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<List<SearchHistoryEntry>> =
        repository.observeHistory(accountId)
}
