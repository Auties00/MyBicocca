package it.attendance100.mybicocca.domain.usecase.elearning.forum

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import javax.inject.Inject

/**
 * Syncs a course's forums from Moodle into the cache when the course detail screen opens or the
 * user pulls to refresh. Skipped while the cached data is still fresh unless [invoke] is forced;
 * throws on network failure.
 */
class RefreshCourseForumsUseCase @Inject constructor(
    private val repository: ElearningForumRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshForumsForCourse(accountId, courseId, force)
}
