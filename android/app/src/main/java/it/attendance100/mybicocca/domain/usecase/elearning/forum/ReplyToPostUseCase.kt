package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Publishes a reply to a post from the forum sheet's composer and returns the new post's id.
 * Pass a draft-area id from the attachment upload flow to attach files. The cached thread is
 * re-synced afterwards so the reply appears in place; throws when posting fails.
 */
class ReplyToPostUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        discussionId: DiscussionId,
        parentPostId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int? = null,
    ): PostId = repository.reply(
        accountId, discussionId, parentPostId, subject, message, attachmentDraftItemId,
    )
}
