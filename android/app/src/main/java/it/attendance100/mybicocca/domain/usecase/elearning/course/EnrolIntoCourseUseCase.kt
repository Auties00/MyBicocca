package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Self-enrols the student into a catalog course when confirmed from the add-course
 * modal, passing the enrolment key when the course requires one. On success it forces
 * a refresh of the enrolled-course list so the new course appears immediately; throws
 * when Moodle rejects the enrolment (e.g. wrong key).
 */
class EnrolIntoCourseUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, password: String? = null) {
        repository.enrolIntoCourse(accountId, courseId, password)
        repository.refreshEnrolledCourses(accountId, force = true)
    }
}
