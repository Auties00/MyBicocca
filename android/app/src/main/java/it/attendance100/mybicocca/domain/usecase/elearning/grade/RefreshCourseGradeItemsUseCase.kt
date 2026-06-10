package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import javax.inject.Inject

/**
 * Re-fetches one course's gradebook rows from Moodle into the local cache when the
 * course detail's grades section appears or is pulled to refresh; `force` bypasses the
 * staleness window. Throws on network failure for the caller to translate into a sync
 * status.
 */
class RefreshCourseGradeItemsUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshCourseGradeItems(accountId, courseId, force)
}
