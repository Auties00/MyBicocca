package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.flow.Flow

interface ElearningAssignmentRepository {
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>>
    fun observe(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>>

    suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)
    suspend fun refreshSubmissionStatus(accountId: AccountId, assignmentId: AssignmentId)

    suspend fun clearForAccount(accountId: AccountId)
}
