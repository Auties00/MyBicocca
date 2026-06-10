package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Prepares a Moodle draft area seeded with a post's current attachments before an edit starts in
 * the forum sheet, so saving the edit keeps existing files alongside newly uploaded ones.
 * Returns the draft-area id; throws on network failure.
 */
class PrepareEditAttachmentsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, postId: PostId): Int =
        repository.prepareEditAttachments(accountId, postId)
}
