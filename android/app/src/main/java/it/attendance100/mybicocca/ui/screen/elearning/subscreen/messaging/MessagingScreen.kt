package it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.messaging.state.MessagingOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MessagingScreen(
    onOpenConversation: (ConversationId) -> Unit = {},
    viewModel: MessagingViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is MessagingOneShotEvent.OpenConversation -> onOpenConversation(event.id)
                is MessagingOneShotEvent.RefreshFailed -> Unit
            }
        }
    }
}
