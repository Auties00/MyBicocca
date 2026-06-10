package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the course-total grades of every enrolled course hot from the local cache,
 * for the cross-course grade overview in the e-learning tab.
 */
class ObserveAllCourseGradesUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Loadable<List<CourseGradeOverview>>> =
        repository.observeAllCourseGrades(accountId)
}
