package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEnrolledCoursesUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Loadable<List<EnrolledCourse>>> =
        repository.observeEnrolledCourses(accountId)
}
