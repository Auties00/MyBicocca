package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams one cached assignment, backing the assignment detail sheet; emits not-yet-loaded
 * while the assignment is absent from the cache.
 */
class ObserveAssignmentUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    operator fun invoke(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>> =
        repository.observe(accountId, assignmentId)
}
