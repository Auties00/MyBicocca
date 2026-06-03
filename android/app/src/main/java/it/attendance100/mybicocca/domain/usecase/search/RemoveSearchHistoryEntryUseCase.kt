package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class RemoveSearchHistoryEntryUseCase @Inject constructor(
    private val repository: SearchHistoryRepository,
) {
    suspend operator fun invoke(accountId: AccountId, query: String) =
        repository.remove(accountId, query)
}
