package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Re-fetches the enrolled-course list and the account's deadlines from Moodle into the
 * local cache. Runs when the e-learning tab appears and on pull-to-refresh; `force`
 * bypasses the staleness window. Throws on network failure for the caller to translate
 * into a sync status.
 */
class RefreshEnrolledCoursesUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, force: Boolean = false) =
        repository.refreshEnrolledCourses(accountId, force)
}
