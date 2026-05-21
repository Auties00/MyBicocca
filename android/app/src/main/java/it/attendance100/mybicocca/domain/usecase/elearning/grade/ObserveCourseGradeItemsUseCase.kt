package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCourseGradeItemsUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<GradeItem>>> =
        repository.observeCourseGradeItems(accountId, courseId)
}
