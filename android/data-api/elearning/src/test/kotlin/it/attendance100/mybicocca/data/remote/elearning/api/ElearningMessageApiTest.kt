package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningMessageApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getUnreadConversationCounts() {
        val counts = api.messages.getUnreadConversationCounts(session.wsToken, profile.userId)
        assertNotNull(counts)
    }

    @Test
    suspend fun getConversations() {
        val conversations = api.messages.getConversations(session.wsToken, profile.userId)
        assertNotNull(conversations)
        assertNotNull(conversations.conversations)
    }

    @Test
    suspend fun getConversationMessages() {
        val conversations = api.messages.getConversations(session.wsToken, profile.userId)
        if (conversations.conversations.isNotEmpty()) {
            val conversation = conversations.conversations.first()
            val messages = api.messages.getConversationMessages(session.wsToken, profile.userId, conversation.id)
            assertNotNull(messages)
            assertNotNull(messages.messages)
        }
    }
}
