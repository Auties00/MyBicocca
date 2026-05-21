package it.attendance100.mybicocca.data.local.elearning.message

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query(
        "SELECT * FROM elearning_conversations " +
            "WHERE account_id = :accountId " +
            "ORDER BY is_favourite DESC, sort_at_ms DESC"
    )
    fun observeConversations(accountId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM elearning_conversations WHERE account_id = :accountId AND conversation_id = :conversationId")
    fun observeConversation(accountId: String, conversationId: Int): Flow<ConversationEntity?>

    @Query(
        "SELECT * FROM elearning_conversation_members " +
            "WHERE account_id = :accountId AND conversation_id = :conversationId " +
            "ORDER BY full_name"
    )
    fun observeMembers(accountId: String, conversationId: Int): Flow<List<ConversationMemberEntity>>

    @Query("SELECT IFNULL(SUM(unread_count), 0) FROM elearning_conversations WHERE account_id = :accountId")
    fun observeUnreadCount(accountId: String): Flow<Int>

    @Query(
        "SELECT * FROM elearning_messages " +
            "WHERE account_id = :accountId AND conversation_id = :conversationId " +
            "ORDER BY sent_at_ms"
    )
    fun observeMessages(accountId: String, conversationId: Int): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsertConversations(rows: List<ConversationEntity>)

    @Upsert
    suspend fun upsertConversation(row: ConversationEntity)

    @Upsert
    suspend fun upsertMembers(rows: List<ConversationMemberEntity>)

    @Upsert
    suspend fun upsertMessages(rows: List<MessageEntity>)

    @Upsert
    suspend fun upsertMessage(row: MessageEntity)

    @Query(
        "UPDATE elearning_messages SET is_read = 1 " +
            "WHERE account_id = :accountId AND conversation_id = :conversationId"
    )
    suspend fun markAllRead(accountId: String, conversationId: Int)

    @Query(
        "UPDATE elearning_conversations SET unread_count = 0 " +
            "WHERE account_id = :accountId AND conversation_id = :conversationId"
    )
    suspend fun zeroUnread(accountId: String, conversationId: Int)

    @Query(
        "DELETE FROM elearning_conversations " +
            "WHERE account_id = :accountId AND conversation_id IN (:conversationIds)"
    )
    suspend fun deleteConversations(accountId: String, conversationIds: List<Int>)

    @Query(
        "DELETE FROM elearning_messages " +
            "WHERE account_id = :accountId AND conversation_id IN (:conversationIds)"
    )
    suspend fun deleteMessagesForConversations(accountId: String, conversationIds: List<Int>)

    @Query(
        "DELETE FROM elearning_conversation_members " +
            "WHERE account_id = :accountId AND conversation_id IN (:conversationIds)"
    )
    suspend fun deleteMembersForConversations(accountId: String, conversationIds: List<Int>)

    @Query("DELETE FROM elearning_conversation_members WHERE account_id = :accountId AND conversation_id = :conversationId")
    suspend fun deleteMembers(accountId: String, conversationId: Int)

    @Query("DELETE FROM elearning_conversations WHERE account_id = :accountId")
    suspend fun deleteAllConversations(accountId: String)

    @Query("DELETE FROM elearning_conversation_members WHERE account_id = :accountId")
    suspend fun deleteAllMembers(accountId: String)

    @Query("DELETE FROM elearning_messages WHERE account_id = :accountId")
    suspend fun deleteAllMessages(accountId: String)

    @Transaction
    suspend fun replaceConversation(
        conversation: ConversationEntity,
        members: List<ConversationMemberEntity>,
    ) {
        upsertConversation(conversation)
        deleteMembers(conversation.accountId, conversation.conversationId)
        if (members.isNotEmpty()) upsertMembers(members)
    }

    @Transaction
    suspend fun deleteFully(accountId: String, conversationIds: List<Int>) {
        if (conversationIds.isEmpty()) return
        deleteMessagesForConversations(accountId, conversationIds)
        deleteMembersForConversations(accountId, conversationIds)
        deleteConversations(accountId, conversationIds)
    }

    @Transaction
    suspend fun clearAllForAccount(accountId: String) {
        deleteAllMessages(accountId)
        deleteAllMembers(accountId)
        deleteAllConversations(accountId)
    }
}
