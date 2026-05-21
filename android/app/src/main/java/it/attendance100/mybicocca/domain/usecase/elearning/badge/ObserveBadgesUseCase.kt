package it.attendance100.mybicocca.domain.usecase.elearning.badge

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBadgesUseCase @Inject constructor(
    private val repository: ElearningBadgeRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId? = null): Flow<Loadable<List<Badge>>> =
        repository.observeBadges(accountId, courseId)
}
