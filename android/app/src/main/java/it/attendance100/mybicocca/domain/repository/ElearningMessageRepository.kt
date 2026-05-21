package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.model.elearning.message.Message
import it.attendance100.mybicocca.domain.model.elearning.message.MessageId
import kotlinx.coroutines.flow.Flow

interface ElearningMessageRepository {
    fun observeConversations(accountId: AccountId): Flow<Loadable<List<Conversation>>>
    fun observeConversation(accountId: AccountId, conversationId: ConversationId): Flow<Loadable<Conversation>>
    fun observeUnreadCount(accountId: AccountId): Flow<Int>
    fun observeMessages(accountId: AccountId, conversationId: ConversationId): Flow<Loadable<List<Message>>>

    suspend fun refreshConversations(accountId: AccountId, force: Boolean = false)
    suspend fun refreshMessages(
        accountId: AccountId,
        conversationId: ConversationId,
        beforeMessageId: MessageId? = null,
        limit: Int = 30,
    )
    suspend fun send(accountId: AccountId, toUserId: Int, text: String): MessageId
    suspend fun markRead(accountId: AccountId, conversationId: ConversationId)
    suspend fun delete(accountId: AccountId, conversationIds: List<ConversationId>)
    suspend fun block(accountId: AccountId, blockedUserId: Int)
    suspend fun unblock(accountId: AccountId, blockedUserId: Int)

    suspend fun clearForAccount(accountId: AccountId)
}
