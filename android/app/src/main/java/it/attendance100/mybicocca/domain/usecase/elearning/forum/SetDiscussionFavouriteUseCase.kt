package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Stars or unstars (favourites) a discussion from the forum sheet. The cached flag flips
 * optimistically before the server call and is reverted when the call fails, in which case the
 * failure is rethrown.
 */
class SetDiscussionFavouriteUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        favourite: Boolean,
    ) = repository.setFavourite(accountId, forumId, discussionId, favourite)
}
