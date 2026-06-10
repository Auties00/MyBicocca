package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Re-fetches one course's contents, staff, syllabus and completion from Moodle into
 * the local cache. Runs when the course detail screen opens and on pull-to-refresh;
 * `force` bypasses the staleness window. Throws on network failure for the caller to
 * translate into a sync status.
 */
class RefreshCourseDetailsUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshCourseDetails(accountId, courseId, force)
}
