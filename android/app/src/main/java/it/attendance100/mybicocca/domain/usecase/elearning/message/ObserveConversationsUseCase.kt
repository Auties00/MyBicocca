package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Loadable<List<Conversation>>> =
        repository.observeConversations(accountId)
}
