package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Marks a discussion read when the forum sheet opens its thread: the cached unread count is
 * cleared immediately and the view is logged to the server best-effort, so the badge never
 * resurrects on a network hiccup.
 */
class MarkDiscussionReadUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, discussionId: DiscussionId) =
        repository.markDiscussionRead(accountId, discussionId)
}
