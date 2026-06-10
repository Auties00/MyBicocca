package it.attendance100.mybicocca.domain.usecase.elearning.quiz

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningQuizRepository
import javax.inject.Inject

/**
 * Syncs a course's quizzes from the e-learning platform into the cache when the course detail
 * screen opens or is refreshed. Skipped while the cache is fresh unless forced; throws on
 * failure.
 */
class RefreshCourseQuizzesUseCase @Inject constructor(
    private val repository: ElearningQuizRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshForCourse(accountId, courseId, force)
}
