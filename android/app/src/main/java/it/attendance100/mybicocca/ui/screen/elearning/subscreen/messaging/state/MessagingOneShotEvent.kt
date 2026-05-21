package it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.state

import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId

sealed interface MessagingOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : MessagingOneShotEvent
    data class OpenConversation(val id: ConversationId) : MessagingOneShotEvent
}
