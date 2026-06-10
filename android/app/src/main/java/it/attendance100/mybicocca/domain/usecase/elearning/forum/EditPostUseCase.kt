package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Saves edits to one of the user's own posts from the forum sheet's composer, allowed by Moodle
 * within the post's edit window. A draft-area id from the edit-attachments flow replaces the
 * post's attachment set; null leaves attachments untouched. The cached thread is re-synced
 * afterwards; throws when the edit fails.
 */
class EditPostUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        discussionId: DiscussionId,
        postId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int? = null,
    ) = repository.editPost(accountId, discussionId, postId, subject, message, attachmentDraftItemId)
}
