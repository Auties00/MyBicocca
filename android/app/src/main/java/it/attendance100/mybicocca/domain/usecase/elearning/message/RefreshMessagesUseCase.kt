package it.attendance100.mybicocca.domain.usecase.elearning.message

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.model.elearning.message.MessageId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import javax.inject.Inject

class RefreshMessagesUseCase @Inject constructor(
    private val repository: ElearningMessageRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        conversationId: ConversationId,
        beforeMessageId: MessageId? = null,
        limit: Int = 30,
    ) = repository.refreshMessages(accountId, conversationId, beforeMessageId, limit)
}
