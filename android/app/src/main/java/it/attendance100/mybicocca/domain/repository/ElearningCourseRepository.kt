package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import kotlinx.coroutines.flow.Flow

interface ElearningCourseRepository {
    fun observeEnrolledCourses(accountId: AccountId): Flow<Loadable<List<EnrolledCourse>>>
    fun observeCourseDetails(accountId: AccountId, courseId: CourseId): Flow<Loadable<CourseDetails>>
    fun observeCompletionStates(accountId: AccountId, courseId: CourseId): Flow<Map<Int, CompletionState>>
    fun observeFilter(): Flow<CourseFilter>

    suspend fun refreshEnrolledCourses(accountId: AccountId, force: Boolean = false)
    suspend fun refreshCourseDetails(accountId: AccountId, courseId: CourseId, force: Boolean = false)
    suspend fun setActivityCompleted(accountId: AccountId, courseId: CourseId, cmId: Int, completed: Boolean)
    suspend fun setFavourite(accountId: AccountId, courseId: CourseId, favourite: Boolean)
    suspend fun setHidden(accountId: AccountId, courseId: CourseId, hidden: Boolean)
    suspend fun setFilter(filter: CourseFilter)

    suspend fun enrolIntoCourse(accountId: AccountId, courseId: CourseId, password: String? = null)

    suspend fun clearForAccount(accountId: AccountId)
}
