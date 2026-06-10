package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Deletes one of the user's own posts from the forum sheet's thread page. The cached thread is
 * re-synced afterwards, since Moodle may tombstone the post when it still has visible replies;
 * throws when the server rejects the deletion.
 */
class DeletePostUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, discussionId: DiscussionId, postId: PostId) =
        repository.deletePost(accountId, discussionId, postId)
}
