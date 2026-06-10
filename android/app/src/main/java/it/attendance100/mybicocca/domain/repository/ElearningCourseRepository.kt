package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import kotlinx.coroutines.flow.Flow

/**
 * Access to the student's Moodle courses: the enrolled list with its deadlines, each
 * course's contents/staff/syllabus, activity completion, and the persisted list filter.
 * The local cache is the single source of truth: `observe*` methods stream hot from it
 * and keep emitting across refreshes; `refresh*` methods hit the Moodle web services,
 * write back to the cache, skip the network while the data is within its staleness
 * window unless `force` is set, and throw on failure for the ViewModel to translate
 * into a sync status.
 */
interface ElearningCourseRepository {
    /** Streams the enrolled courses with their cached deadlines attached. */
    fun observeEnrolledCourses(accountId: AccountId): Flow<Loadable<List<EnrolledCourse>>>

    /**
     * Streams one course's full detail; not-yet-loaded until the course has been
     * cached at least once.
     */
    fun observeCourseDetails(accountId: AccountId, courseId: CourseId): Flow<Loadable<CourseDetails>>

    /** Streams a course's activity completion states keyed by course-module id. */
    fun observeCompletionStates(accountId: AccountId, courseId: CourseId): Flow<Map<Int, CompletionState>>

    /** Streams the persisted course-list filter; emits the default until one is set. */
    fun observeFilter(): Flow<CourseFilter>

    /** Re-fetches the enrolled-course list and the account's deadlines from Moodle. */
    suspend fun refreshEnrolledCourses(accountId: AccountId, force: Boolean = false)

    /** Re-fetches one course's contents, staff, syllabus and completion from Moodle. */
    suspend fun refreshCourseDetails(accountId: AccountId, courseId: CourseId, force: Boolean = false)

    /**
     * Writes a manual completion tick to Moodle, then re-fetches the course's
     * completion states to reconcile the cache with the server.
     */
    suspend fun setActivityCompleted(accountId: AccountId, courseId: CourseId, cmId: Int, completed: Boolean)

    /** Stores the favourite flag locally; nothing is written to Moodle. */
    suspend fun setFavourite(accountId: AccountId, courseId: CourseId, favourite: Boolean)

    /** Stores the hidden flag locally; nothing is written to Moodle. */
    suspend fun setHidden(accountId: AccountId, courseId: CourseId, hidden: Boolean)

    /** Persists the course-list filter. */
    suspend fun setFilter(filter: CourseFilter)

    /**
     * Self-enrols the account into a course, with the enrolment key when required.
     * Throws when Moodle rejects the enrolment; the cached course list is unaffected
     * until the next refresh.
     */
    suspend fun enrolIntoCourse(accountId: AccountId, courseId: CourseId, password: String? = null)

    /** Drops every cached course row of the account, e.g. on sign-out. */
    suspend fun clearForAccount(accountId: AccountId)
}
