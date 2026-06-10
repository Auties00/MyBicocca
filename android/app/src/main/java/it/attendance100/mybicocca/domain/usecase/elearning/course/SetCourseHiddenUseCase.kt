package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Hides or unhides a course when the user toggles the visibility action on a course
 * row. The flag is device-local only; hidden courses disappear from the e-learning
 * course list and its year chips without affecting the Moodle enrolment.
 */
class SetCourseHiddenUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, hidden: Boolean) =
        repository.setHidden(accountId, courseId, hidden)
}
