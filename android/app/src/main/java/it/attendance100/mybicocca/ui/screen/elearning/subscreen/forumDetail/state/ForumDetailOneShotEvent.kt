package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forumDetail.state

import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId

sealed interface ForumDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : ForumDetailOneShotEvent
    data class DiscussionCreated(val id: DiscussionId) : ForumDetailOneShotEvent
    data class OpenDiscussion(val id: DiscussionId) : ForumDetailOneShotEvent
}
