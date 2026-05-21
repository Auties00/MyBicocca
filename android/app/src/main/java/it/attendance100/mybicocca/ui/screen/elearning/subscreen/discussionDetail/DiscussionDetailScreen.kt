package it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.state.DiscussionDetailOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DiscussionDetailScreen(
    discussionId: Int,
    viewModel: DiscussionDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is DiscussionDetailOneShotEvent.ReplyPosted -> Unit
                is DiscussionDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") discussionId
}
