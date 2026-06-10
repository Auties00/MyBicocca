package it.attendance100.mybicocca.domain.usecase.elearning.badge

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the account's Moodle badges hot from the local cache — all of them, or only
 * those awarded by one course when a course id is given. Backs the badge gallery and
 * the course detail's badge section.
 */
class ObserveBadgesUseCase @Inject constructor(
    private val repository: ElearningBadgeRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId? = null): Flow<Loadable<List<Badge>>> =
        repository.observeBadges(accountId, courseId)
}
