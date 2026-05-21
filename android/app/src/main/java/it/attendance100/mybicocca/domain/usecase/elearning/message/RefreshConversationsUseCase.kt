package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import javax.inject.Inject

class RefreshConversationsUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    suspend operator fun invoke(accountId: AccountId, force: Boolean = false) =
        repository.refreshConversations(accountId, force)
}
