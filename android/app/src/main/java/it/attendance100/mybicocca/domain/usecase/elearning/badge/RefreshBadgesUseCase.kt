package it.attendance100.mybicocca.domain.usecase.elearning.badge

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import javax.inject.Inject

class RefreshBadgesUseCase @Inject constructor(
    private val repository: ElearningBadgeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId? = null, force: Boolean = false) =
        repository.refreshBadges(accountId, courseId, force)
}
