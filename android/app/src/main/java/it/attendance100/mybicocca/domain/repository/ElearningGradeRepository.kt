package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import kotlinx.coroutines.flow.Flow

interface ElearningGradeRepository {
    fun observeCourseGradeItems(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<GradeItem>>>
    fun observeAllCourseGrades(accountId: AccountId): Flow<Loadable<List<CourseGradeOverview>>>

    suspend fun refreshCourseGradeItems(accountId: AccountId, courseId: CourseId, force: Boolean = false)
    suspend fun refreshAllCourseGrades(accountId: AccountId, force: Boolean = false)

    suspend fun clearForAccount(accountId: AccountId)
}
