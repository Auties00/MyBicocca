package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.MessagingDao
import it.attendance100.mybicocca.data.datasource.messaging.ElearningMessagingDataSource
import it.attendance100.mybicocca.data.model.messaging.Conversation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingRepository @Inject constructor(
    private val dataSource: ElearningMessagingDataSource,
    private val dao: MessagingDao,
) {
    fun observeAll(): Flow<List<Conversation>> = dao.observeAll()

    suspend fun refresh(): Result<Unit> = runCatching {
        val conversations = dataSource.getConversations()
        dao.upsertAll(conversations)
    }
}
