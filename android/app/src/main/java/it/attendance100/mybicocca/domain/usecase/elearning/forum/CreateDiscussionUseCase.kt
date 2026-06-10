package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Starts a new discussion in a forum from the forum sheet's composer and returns its id. Group
 * forums take the target group picked by the user; a draft-area id from the attachment upload
 * flow attaches files. The cached discussion list is re-synced afterwards; throws when posting
 * fails.
 */
class CreateDiscussionUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int? = null,
        attachmentDraftItemId: Int? = null,
    ): DiscussionId = repository.createDiscussion(
        accountId, forumId, subject, message, groupId, attachmentDraftItemId,
    )
}
