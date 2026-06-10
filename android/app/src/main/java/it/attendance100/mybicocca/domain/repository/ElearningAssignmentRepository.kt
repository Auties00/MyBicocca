package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import kotlinx.coroutines.flow.Flow

/**
 * Contract for assignments (compiti) of e-learning courses, including the in-app submission
 * flow. Observe methods are hot flows from the local cache, which is the single source of
 * truth; refresh and mutation methods hit the e-learning platform and throw on failure. All
 * cached data is account-scoped.
 */
interface ElearningAssignmentRepository {
    /** Streams a course's cached assignments. */
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>>

    /** Streams one cached assignment; not-yet-loaded while absent from the cache. */
    fun observe(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>>

    /**
     * Account-wide cached stream feeding the unified search index. A plain list rather than a
     * loadable wrapper — search treats an empty cache and a not-yet-hydrated cache the same way.
     */
    fun observeAllForAccount(accountId: AccountId): Flow<List<Assignment>>

    /**
     * Syncs a course's assignments, each with the student's submission status, into the cache.
     * Skipped while the cached data is fresher than the staleness policy unless [force]d.
     */
    suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)

    /** Re-fetches the student's submission state of one assignment into the cached row. */
    suspend fun refreshSubmissionStatus(accountId: AccountId, assignmentId: AssignmentId)

    /**
     * Fetches the submission editor model fresh on every call — never cached, because
     * editability (canEdit/canSubmit) is time- and lock-sensitive.
     */
    suspend fun loadSubmissionForm(
        accountId: AccountId,
        courseId: CourseId,
        assignmentId: AssignmentId,
    ): SubmissionForm

    /**
     * Saves the submission as a draft or, when drafts are disabled, as the submission itself.
     * The platform replaces the whole file area with the uploaded contents on save, so
     * already-submitted files the user keeps must be passed as [keepFiles]; they are
     * re-downloaded and re-uploaded alongside the new ones. Throws on failure.
     */
    suspend fun saveSubmission(
        accountId: AccountId,
        assignmentId: AssignmentId,
        onlineText: String?,
        files: List<SubmissionFile>,
        keepFiles: List<Assignment.AttachmentRef>,
    )

    /** Finalizes the draft for grading. Irreversible. Throws on failure. */
    suspend fun submitForGrading(
        accountId: AccountId,
        assignmentId: AssignmentId,
        acceptStatement: Boolean,
    )

    /** Discards the current submission, reverting it to empty. Throws on failure. */
    suspend fun removeSubmission(accountId: AccountId, assignmentId: AssignmentId)

    /** Resolves display metadata for a picked `content://` uri without reading its content. */
    suspend fun probeSubmissionFile(uri: String): SubmissionFileMetadata

    /** Reads a picked file's full content into memory, ready for upload. */
    suspend fun readSubmissionFile(uri: String): SubmissionFile

    /** Drops every cached assignment of the account, e.g. on sign-out. */
    suspend fun clearForAccount(accountId: AccountId)
}
