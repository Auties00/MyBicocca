package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.state.ForumDetailOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ForumDetailScreen(
    forumId: Int,
    courseId: Int,
    onOpenDiscussion: (DiscussionId) -> Unit = {},
    viewModel: ForumDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is ForumDetailOneShotEvent.OpenDiscussion -> onOpenDiscussion(event.id)
                is ForumDetailOneShotEvent.DiscussionCreated -> onOpenDiscussion(event.id)
                is ForumDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") forumId
    @Suppress("UNUSED_PARAMETER") courseId
}
