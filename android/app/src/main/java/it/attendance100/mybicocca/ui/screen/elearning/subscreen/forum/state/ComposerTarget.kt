package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state

import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostAttachment
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId

/**
 * What the in-sheet composer is producing. The same composer page serves all three; the target
 * decides which fields show (a new discussion needs a subject; a reply/edit pre-fills one) and
 * which repository call runs on submit.
 */
sealed interface ComposerTarget {
    /** A new top-level discussion in the forum. */
    data class NewDiscussion(val forumId: ForumId, val canAttach: Boolean) : ComposerTarget

    /** A reply to a specific post within the open discussion. */
    data class Reply(
        val discussionId: DiscussionId,
        val parentPostId: PostId,
        val replySubject: String,
        val canAttach: Boolean,
    ) : ComposerTarget

    /**
     * Editing one of the user's own posts. Existing attachments are always preserved; the user
     * can add new ones when the forum allows attachments.
     */
    data class Edit(
        val discussionId: DiscussionId,
        val postId: PostId,
        val subject: String,
        val initialMessage: String,
        val canAttach: Boolean,
        val existingAttachments: List<PostAttachment>,
    ) : ComposerTarget
}
