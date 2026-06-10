package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import kotlinx.coroutines.flow.Flow

/**
 * Access to the student's Moodle gradebook: per-course grade rows and the cross-course
 * total overview. The local cache is the single source of truth: `observe*` methods
 * stream hot from it; `refresh*` methods hit the Moodle grade web services, write back
 * to the cache, skip the network while the data is within its staleness window unless
 * `force` is set, and throw on failure for the ViewModel to translate into a sync
 * status.
 */
interface ElearningGradeRepository {
    /** Streams one course's gradebook rows in gradebook order. */
    fun observeCourseGradeItems(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<GradeItem>>>

    /** Streams the course-total grades of every enrolled course. */
    fun observeAllCourseGrades(accountId: AccountId): Flow<Loadable<List<CourseGradeOverview>>>

    /** Re-fetches one course's gradebook rows from Moodle. */
    suspend fun refreshCourseGradeItems(accountId: AccountId, courseId: CourseId, force: Boolean = false)

    /** Re-fetches the course-total grades of every enrolled course from Moodle. */
    suspend fun refreshAllCourseGrades(accountId: AccountId, force: Boolean = false)

    /** Drops every cached grade row of the account, e.g. on sign-out. */
    suspend fun clearForAccount(accountId: AccountId)
}
