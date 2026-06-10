package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Subscribes to or unsubscribes from a single discussion's notifications from the forum sheet.
 * Server-only: subscription state is not part of the cached discussion data. Throws on the
 * server's business errors — e.g. forced-subscription forums — which the caller surfaces as an
 * outcome.
 */
class SetDiscussionSubscriptionUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        subscribed: Boolean,
    ) = repository.setDiscussionSubscription(accountId, forumId, discussionId, subscribed)
}
