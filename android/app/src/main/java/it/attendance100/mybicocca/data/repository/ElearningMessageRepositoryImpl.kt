package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.message.MessageDao
import it.attendance100.mybicocca.data.local.elearning.message.MessageEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.toDomain
import it.attendance100.mybicocca.data.mapper.toProjection
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.model.elearning.message.Message
import it.attendance100.mybicocca.domain.model.elearning.message.MessageId
import it.attendance100.mybicocca.domain.repository.ElearningMessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningMessageRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val messageDao: MessageDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningMessageRepository {

    private val convosInFlight = ConcurrentHashMap<String, Deferred<Unit>>()

    override fun observeConversations(accountId: AccountId): Flow<Loadable<List<Conversation>>> =
        messageDao.observeConversations(accountId.value)
            .map { list ->
                // List view doesn't render member chips, so members are left empty here.
                // The conversation detail screen pulls members via observeConversation().
                val convos = list.map { row -> row.toDomain(members = emptyList(), lastMessage = null) }
                Loadable.Loaded(convos) as Loadable<List<Conversation>>
            }
            .flowOn(Dispatchers.Default)

    override fun observeConversation(
        accountId: AccountId,
        conversationId: ConversationId,
    ): Flow<Loadable<Conversation>> {
        val convo = messageDao.observeConversation(accountId.value, conversationId.value)
        val members = messageDao.observeMembers(accountId.value, conversationId.value)
        return combine(convo, members) { c, m ->
            if (c == null) Loadable.NotYetLoaded as Loadable<Conversation>
            else Loadable.Loaded(c.toDomain(m.map { it.toDomain() }, lastMessage = null))
        }.flowOn(Dispatchers.Default)
    }

    override fun observeUnreadCount(accountId: AccountId): Flow<Int> =
        messageDao.observeUnreadCount(accountId.value).flowOn(Dispatchers.Default)

    override fun observeMessages(
        accountId: AccountId,
        conversationId: ConversationId,
    ): Flow<Loadable<List<Message>>> =
        messageDao.observeMessages(accountId.value, conversationId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Message>> }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshConversations(accountId: AccountId, force: Boolean) {
        val key = accountId.value
        val deferred = convosInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshConversations(accountId, force)
            }.also { d -> d.invokeOnCompletion { convosInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshConversations(accountId: AccountId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.CONVERSATIONS, 0)) return
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        val response = api.messages.getConversations(
            wsToken = token,
            userId = account.learning.lmsUserId,
            limitFrom = 0,
            limitNum = 50,
            type = null,
            favourites = null,
        )
        response.conversations.forEach { dto ->
            val projection = dto.toProjection(accountId)
            messageDao.replaceConversation(projection.conversation, projection.members)
            if (projection.messages.isNotEmpty()) messageDao.upsertMessages(projection.messages)
        }
        stamp(accountId, ElearningSyncScope.CONVERSATIONS, 0)
    }

    override suspend fun refreshMessages(
        accountId: AccountId,
        conversationId: ConversationId,
        beforeMessageId: MessageId?,
        limit: Int,
    ) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        val response = api.messages.getConversationMessages(
            wsToken = token,
            currentUserId = account.learning.lmsUserId,
            conversationId = conversationId.value,
            limitFrom = 0,
            limitNum = limit,
            newestFirst = true,
            timeFrom = 0,
        )
        val rows = response.messages.map {
            MessageEntity(
                accountId = accountId.value,
                messageId = it.id,
                conversationId = conversationId.value,
                senderUserId = it.userIdFrom ?: 0,
                text = it.text.orEmpty(),
                sentAtMs = it.timeCreated?.takeIf { ts -> ts > 0 }?.let { ts -> ts * 1000L },
                isRead = false,
            )
        }
        if (rows.isNotEmpty()) messageDao.upsertMessages(rows)
        stamp(accountId, ElearningSyncScope.CONVERSATION_MESSAGES, conversationId.value.toLong())
    }

    override suspend fun send(accountId: AccountId, toUserId: Int, text: String): MessageId {
        val (api, token) = sessionManager.elearning()
        val response = api.messages.sendInstantMessage(token, toUserId, text)
        val sent = response.items.firstOrNull()
            ?: error("Send failed: empty response")
        val msgId = sent.messageId ?: error(sent.errorMessage ?: "Send failed")
        return MessageId(msgId)
    }

    override suspend fun markRead(accountId: AccountId, conversationId: ConversationId) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        runCatching {
            api.messages.markAllConversationMessagesAsRead(
                wsToken = token,
                userId = account.learning.lmsUserId,
                conversationId = conversationId.value,
            )
        }
        messageDao.markAllRead(accountId.value, conversationId.value)
        messageDao.zeroUnread(accountId.value, conversationId.value)
    }

    override suspend fun delete(accountId: AccountId, conversationIds: List<ConversationId>) {
        if (conversationIds.isEmpty()) return
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        api.messages.deleteConversations(
            wsToken = token,
            userId = account.learning.lmsUserId,
            conversationIds = conversationIds.map { it.value },
        )
        messageDao.deleteFully(accountId.value, conversationIds.map { it.value })
    }

    override suspend fun block(accountId: AccountId, blockedUserId: Int) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        api.messages.blockUser(token, account.learning.lmsUserId, blockedUserId)
    }

    override suspend fun unblock(accountId: AccountId, blockedUserId: Int) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        api.messages.unblockUser(token, account.learning.lmsUserId, blockedUserId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        messageDao.clearAllForAccount(accountId.value)
    }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }
}
