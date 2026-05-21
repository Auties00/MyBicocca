package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCompletionStatesUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Map<Int, CompletionState>> =
        repository.observeCompletionStates(accountId, courseId)
}
