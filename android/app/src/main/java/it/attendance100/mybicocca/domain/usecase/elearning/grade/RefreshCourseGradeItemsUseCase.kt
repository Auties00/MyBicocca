package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import javax.inject.Inject

class RefreshCourseGradeItemsUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshCourseGradeItems(accountId, courseId, force)
}
