package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.elearning.SubmissionStatusJson
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.mapper.elearning.toSubmissionStatusJson
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
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

@Singleton
class ElearningAssignmentRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val assignmentDao: AssignmentDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningAssignmentRepository {

    private val refreshInFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>> =
        assignmentDao.observeForCourse(accountId.value, courseId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Assignment>> }
            .flowOn(Dispatchers.Default)

    override fun observe(accountId: AccountId, assignmentId: AssignmentId): Flow<Loadable<Assignment>> =
        assignmentDao.observe(accountId.value, assignmentId.value)
            .map { row -> if (row == null) Loadable.NotYetLoaded as Loadable<Assignment> else Loadable.Loaded(row.toDomain()) }
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

    private suspend fun doRefreshForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_ASSIGNMENTS, courseId.value.toLong())) return
        val (api, token) = sessionManager.elearning()
        val response = api.assignments.getAssignmentsForCourse(token, courseId.value)
        val assignments = response.courses.flatMap { it.assignments }
        // Status fetch is per-assignment; do them in parallel but tolerate failures.
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
