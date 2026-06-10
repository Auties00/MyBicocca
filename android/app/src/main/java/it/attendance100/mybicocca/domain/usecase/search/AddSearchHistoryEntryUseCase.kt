package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.SearchHistoryRepository
import javax.inject.Inject

/**
 * Records a submitted query in the account's search history. When the search ended in a
 * tap, the picked result key records which result was opened, feeding the adaptive pick
 * boost.
 */
class AddSearchHistoryEntryUseCase @Inject constructor(
    private val repository: SearchHistoryRepository,
) {
    suspend operator fun invoke(accountId: AccountId, query: String, pickedKey: String? = null) =
        repository.add(accountId, query, pickedKey)
}
