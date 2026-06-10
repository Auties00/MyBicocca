package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Marks a manually-tracked activity as done or not done when the user taps its
 * checkmark on the course detail screen. Writes the state to Moodle, then re-fetches
 * the course's completion statuses so the local cache reflects the server's view.
 */
class SetActivityCompletedUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, cmId: Int, completed: Boolean) =
        repository.setActivityCompleted(accountId, courseId, cmId, completed)
}
