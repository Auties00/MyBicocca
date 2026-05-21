package it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.conversationDetail.state.ConversationDetailOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ConversationDetailScreen(
    conversationId: Int,
    viewModel: ConversationDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is ConversationDetailOneShotEvent.MessageSent -> Unit
                is ConversationDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") conversationId
}
