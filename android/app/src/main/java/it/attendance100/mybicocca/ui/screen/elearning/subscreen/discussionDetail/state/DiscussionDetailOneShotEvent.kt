package it.attendance100.mybicocca.ui.screen.elearning.subscreen.discussionDetail.state

import it.attendance100.mybicocca.domain.model.elearning.forum.PostId

sealed interface DiscussionDetailOneShotEvent {
    data class RefreshFailed(val cause: Throwable) : DiscussionDetailOneShotEvent
    data class ReplyPosted(val id: PostId) : DiscussionDetailOneShotEvent
}
