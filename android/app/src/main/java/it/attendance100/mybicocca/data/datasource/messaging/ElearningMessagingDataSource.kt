package it.attendance100.mybicocca.data.datasource.messaging

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.messaging.Conversation
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningMessagingDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getConversations(): List<Conversation> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        val userId = authTokenStore.elearningUserId
        if (userId == -1) return@withContext emptyList()

        elearningApi.messages.getConversations(wsToken, userId).conversations.map { dto ->
            Conversation(
                id = dto.id,
                name = dto.name,
                type = dto.type ?: 1,
                memberCount = dto.memberCount ?: 0,
                unreadCount = dto.unreadCount ?: 0,
                isFavourite = dto.isFavourite ?: false,
                isMuted = dto.isMuted ?: false,
                lastMessageText = dto.messages?.lastOrNull()?.text,
                lastMessageDate = dto.messages?.lastOrNull()?.timeCreated,
            )
        }
    }
}
