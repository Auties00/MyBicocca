package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

class CreateDiscussionUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int? = null,
    ): DiscussionId = repository.createDiscussion(accountId, forumId, subject, message, groupId)
}
