package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    operator fun invoke(accountId: AccountId, conversationId: ConversationId): Flow<Loadable<Conversation>> =
        repository.observeConversation(accountId, conversationId)
}
