package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDiscussionsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    operator fun invoke(accountId: AccountId, forumId: ForumId): Flow<Loadable<List<Discussion>>> =
        repository.observeDiscussions(accountId, forumId)
}
