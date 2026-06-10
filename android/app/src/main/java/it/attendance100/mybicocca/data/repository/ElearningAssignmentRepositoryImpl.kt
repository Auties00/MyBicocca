package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.local.file.ContentUriReader
import it.attendance100.mybicocca.data.mapper.elearning.SubmissionConfigJson
import it.attendance100.mybicocca.data.mapper.elearning.SubmissionStatusJson
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.mapper.elearning.toSubmissionConfigJson
import it.attendance100.mybicocca.data.mapper.elearning.toSubmissionForm
import it.attendance100.mybicocca.data.mapper.elearning.toSubmissionStatusJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpload
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed assignment repository on top of Moodle's mod_assign web services.
 *
 * Cache rows are fed by mod_assign_get_assignments plus one mod_assign_get_submission_status
 * call per assignment. Course refreshes are deduplicated through an in-flight map so concurrent
 * callers await a single network pass, and are skipped while the per-course sync-state stamp is
 * fresher than the staleness TTL unless forced.
 *
 * The submission write path follows Moodle's draft-area protocol: new and kept files are
 * uploaded together to upload.php as a single draft item id, then mod_assign_save_submission
 * points the submission's file area at that draft — the platform replaces the whole file area
 * on save, which is why kept files are re-downloaded and re-uploaded. Finalizing and removing
 * map to mod_assign_submit_for_grading and mod_assign_remove_submission; every mutation
 * re-fetches the submission status into the cache.
 */
@Singleton
class ElearningAssignmentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val assignmentDao: AssignmentDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    private val contentUriReader: ContentUriReader,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningAssignmentRepository {

    private val refreshInFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()

    override suspend fun probeSubmissionFile(uri: String): SubmissionFileMetadata = contentUriReader.probe(uri)

    override suspend fun readSubmissionFile(uri: String): SubmissionFile = contentUriReader.read(uri)

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>> =
        assignmentDao.observeForCourse(accountId.value, courseId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Assignment>> }
            .flowOn(Dispatchers.Default)

    override fun observe(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>> =
        assignmentDao.observe(accountId.value, assignmentId.value)
            .map { row -> if (row == null) Loadable.NotYetLoaded as Loadable<Assignment> else Loadable.Loaded(row.toDomain()) }
            .flowOn(Dispatchers.Default)

    override fun observeAllForAccount(accountId: AccountId): Flow<List<Assignment>> =
        assignmentDao.observeForAccount(accountId.value)
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        val key = RefreshKey(accountId, courseId)
        val deferred = refreshInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshForCourse(accountId, courseId, force)
            }.also { d -> d.invokeOnCompletion { refreshInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    /**
     * Status fetches are per-assignment, so they run in parallel; a failed one degrades that
     * assignment to not-submitted rather than failing the whole refresh.
     */
    private suspend fun doRefreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_ASSIGNMENTS, courseId.value.toLong())) return
        val (api, token) = sessionManager.elearning()
        val response = api.assignments.getAssignmentsForCourse(token, courseId.value)
        val assignments = response.courses.flatMap { it.assignments }
        val rows = coroutineScope {
            assignments.map { dto ->
                async {
                    val status = runCatching {
                        api.assignments.getSubmissionStatus(token, dto.id, null, null).toSubmissionStatusJson(token)
                    }.getOrDefault(SubmissionStatusJson.NotSubmitted)
                    dto.toEntity(accountId, status, token)
                }
            }.map { it.await() }
        }
        assignmentDao.replaceForCourse(accountId.value, courseId.value, rows)
        stamp(accountId, ElearningSyncScope.COURSE_ASSIGNMENTS, courseId.value.toLong())
    }

    override suspend fun refreshSubmissionStatus(accountId: AccountId, assignmentId: AssignmentId) {
        val (api, token) = sessionManager.elearning()
        val statusJson = api.assignments.getSubmissionStatus(token, assignmentId.value, null, null)
            .toSubmissionStatusJson(token)
        val row = assignmentDao.observe(accountId.value, assignmentId.value).firstOrNull()
            ?: return
        val encoded = ElearningJson.encodeToString(SubmissionStatusJson.serializer(), statusJson)
        assignmentDao.upsert(row.copy(submissionStatusJson = encoded))
    }

    /**
     * The submission config is normally cached with the assignment; on a deep-open before any
     * list sync it can be missing, in which case it falls back to a fresh assignments fetch for
     * that one assignment.
     */
    override suspend fun loadSubmissionForm(
        accountId: AccountId,
        courseId: CourseId,
        assignmentId: AssignmentId,
    ): SubmissionForm {
        val (api, token) = sessionManager.elearning()
        val row = assignmentDao.observe(accountId.value, assignmentId.value).firstOrNull()
        val config = row?.submissionConfigJson
            ?.let { runCatching { ElearningJson.decodeFromString(SubmissionConfigJson.serializer(), it) }.getOrNull() }
            ?: api.assignments.getAssignmentsForCourse(token, courseId.value)
                .courses.flatMap { it.assignments }
                .firstOrNull { it.id == assignmentId.value }
                ?.toSubmissionConfigJson()
            ?: SubmissionConfigJson()
        val draftsEnabled = row?.allowDrafts ?: true
        val status = api.assignments.getSubmissionStatus(token, assignmentId.value, null, null)
        return status.toSubmissionForm(config, draftsEnabled, token)
    }

    /**
     * Kept files are re-downloaded and re-uploaded together with the new ones into a single
     * draft area, since the platform replaces the file area with the draft's contents on save.
     * With nothing to upload the draft item id stays null, leaving the existing file area
     * untouched.
     */
    override suspend fun saveSubmission(
        accountId: AccountId,
        assignmentId: AssignmentId,
        onlineText: String?,
        files: List<SubmissionFile>,
        keepFiles: List<Assignment.AttachmentRef>,
    ) {
        val (api, token) = sessionManager.elearning()
        val uploads = buildList {
            keepFiles.forEach { ref ->
                val url = ref.fileUrl ?: return@forEach
                add(ElearningUpload(ref.fileName, ref.mimeType, api.files.downloadFileBytes(token, url)))
            }
            files.forEach { add(ElearningUpload(it.fileName, it.mimeType, it.bytes)) }
        }
        val filesItemId = if (uploads.isEmpty()) null else api.files.uploadToDraftArea(token, uploads).first().itemId
        api.assignments.saveSubmission(
            wsToken = token,
            assignmentId = assignmentId.value,
            onlineText = onlineText,
            filesDraftItemId = filesItemId,
        )
        refreshSubmissionStatus(accountId, assignmentId)
    }

    override suspend fun submitForGrading(
        accountId: AccountId,
        assignmentId: AssignmentId,
        acceptStatement: Boolean,
    ) {
        val (api, token) = sessionManager.elearning()
        api.assignments.submitForGrading(token, assignmentId.value, acceptStatement)
        refreshSubmissionStatus(accountId, assignmentId)
    }

    override suspend fun removeSubmission(accountId: AccountId, assignmentId: AssignmentId) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        api.assignments.removeSubmission(token, assignmentId.value, account.learning.lmsUserId)
        refreshSubmissionStatus(accountId, assignmentId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        assignmentDao.deleteForAccount(accountId.value)
    }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private data class RefreshKey(val accountId: AccountId, val courseId: CourseId)
}
