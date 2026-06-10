package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Stars or unstars a course when the user toggles the favourite action on a course
 * row. The flag is device-local only; it feeds the "Preferiti" filter of the
 * e-learning course list and is not written back to Moodle.
 */
class ToggleCourseFavouriteUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, favourite: Boolean) =
        repository.setFavourite(accountId, courseId, favourite)
}
