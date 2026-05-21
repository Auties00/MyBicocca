package it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.state

import it.attendance100.mybicocca.domain.model.elearning.message.MessageId

sealed interface ConversationDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : ConversationDetailOneShotEvent
    data class MessageSent(val id: MessageId) : ConversationDetailOneShotEvent
}
