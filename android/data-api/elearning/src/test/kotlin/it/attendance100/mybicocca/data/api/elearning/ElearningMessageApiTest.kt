package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningMessageApiTest : ElearningTestBase() {

    @Test
    fun `message operations work`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        
        // Unread Counts
        val counts = api.messages.getUnreadConversationCounts(wsToken, siteInfo.userId)
        assertNotNull(counts)
        println("Unread counts: Favorites=${counts.favourites}, Types=${counts.types}")

        // Conversations
        val conversations = api.messages.getConversations(wsToken, siteInfo.userId)
        assertNotNull(conversations)
        assertNotNull(conversations.conversations)
        println("Found ${conversations.conversations.size} conversations")

        // Messages from first conversation
        if (conversations.conversations.isNotEmpty()) {
            val conversation = conversations.conversations.first()
            val messages = api.messages.getConversationMessages(wsToken, siteInfo.userId, conversation.id)
            assertNotNull(messages)
            assertNotNull(messages.messages)
            println("Found ${messages.messages.size} messages in conversation ${conversation.id}")
        }
    }
}
