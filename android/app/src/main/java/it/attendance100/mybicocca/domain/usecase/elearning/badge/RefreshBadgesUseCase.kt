package it.attendance100.mybicocca.domain.usecase.elearning.badge

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import javax.inject.Inject

/**
 * Re-fetches the account's badges (optionally scoped to one course) from Moodle into
 * the local cache when the badge gallery or a course detail appears; `force` bypasses
 * the staleness window. Throws on network failure for the caller to translate into a
 * sync status.
 */
class RefreshBadgesUseCase @Inject constructor(
    private val repository: ElearningBadgeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId? = null, force: Boolean = false) =
        repository.refreshBadges(accountId, courseId, force)
}
