package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Syncs one page of a forum's discussions into the cache, for the forum sheet's list load and
 * its load-more pagination. Returns how many discussions the page contained so the caller can
 * tell whether more pages exist; throws on network failure.
 */
class RefreshDiscussionsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, forumId: ForumId, page: Int = 0, perPage: Int = 25) =
        repository.refreshDiscussions(accountId, forumId, page, perPage)
}
