package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Syncs a discussion's full post tree into the cache when the forum sheet opens a thread.
 * Throws on network failure.
 */
class RefreshPostsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, discussionId: DiscussionId) =
        repository.refreshPosts(accountId, discussionId)
}
