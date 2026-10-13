package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Favourites, hidden courses, the filter and ticked activities change in memory for the session. */
@Singleton
class DemoElearningCourseRepository @Inject constructor() : ElearningCourseRepository {

    private val courses = MutableStateFlow(DemoElearning.enrolled)
    private val filter = MutableStateFlow<CourseFilter>(CourseFilter.All)
    private val completionOverrides = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    override fun observeEnrolledCourses(accountId: AccountId): Flow<Loadable<List<EnrolledCourse>>> =
        courses.map { Loadable.Loaded(it) }

    override fun observeCourseDetails(accountId: AccountId, courseId: CourseId): Flow<Loadable<CourseDetails>> {
        val course = DemoElearning.course(courseId) ?: return flowOf(Loadable.NotYetLoaded)
        return courses.map { current ->
            val details = DemoElearning.details(course)
            Loadable.Loaded(details.copy(enrolled = current.firstOrNull { it.id == courseId } ?: details.enrolled))
        }
    }

    override fun observeCompletionStates(accountId: AccountId, courseId: CourseId): Flow<Map<Int, CompletionState>> {
        val course = DemoElearning.course(courseId) ?: return flowOf(emptyMap())
        return completionOverrides.map { overrides ->
            DemoElearning.completionStates(course).mapValues { (cmId, state) ->
                overrides[cmId]?.let { state.copy(isCompleted = it) } ?: state
            }
        }
    }

    override fun observeFilter(): Flow<CourseFilter> = filter

    override suspend fun refreshEnrolledCourses(accountId: AccountId, force: Boolean) = Unit

    override suspend fun refreshCourseDetails(accountId: AccountId, courseId: CourseId, force: Boolean) = Unit

    override suspend fun setActivityCompleted(accountId: AccountId, courseId: CourseId, cmId: Int, completed: Boolean) {
        completionOverrides.update { it + (cmId to completed) }
    }

    override suspend fun setFavourite(accountId: AccountId, courseId: CourseId, favourite: Boolean) {
        courses.update { list -> list.map { if (it.id == courseId) it.copy(isFavourite = favourite) else it } }
    }

    override suspend fun setHidden(accountId: AccountId, courseId: CourseId, hidden: Boolean) {
        courses.update { list -> list.map { if (it.id == courseId) it.copy(hidden = hidden) else it } }
    }

    override suspend fun setFilter(filter: CourseFilter) {
        this.filter.value = filter
    }

    override suspend fun enrolIntoCourse(accountId: AccountId, courseId: CourseId, password: String?) = Unit

    override suspend fun clearForAccount(accountId: AccountId) = Unit
}
