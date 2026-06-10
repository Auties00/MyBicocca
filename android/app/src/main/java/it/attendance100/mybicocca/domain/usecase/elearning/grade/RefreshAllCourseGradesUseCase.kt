package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import javax.inject.Inject

/**
 * Re-fetches the course-total grades of every enrolled course from Moodle into the
 * local cache when the grade overview appears or is pulled to refresh; `force`
 * bypasses the staleness window. Throws on network failure for the caller to translate
 * into a sync status.
 */
class RefreshAllCourseGradesUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, force: Boolean = false) =
        repository.refreshAllCourseGrades(accountId, force)
}
