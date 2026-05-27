package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.CourseGradeOverview
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningGradeRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val gradeDao: GradeDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningGradeRepository {

    private val itemsInFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()
    private val overviewInFlight = ConcurrentHashMap<String, Deferred<Unit>>()

    override fun observeCourseGradeItems(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<GradeItem>>> =
        gradeDao.observeCourseGradeItems(accountId.value, courseId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<GradeItem>> }
            .flowOn(Dispatchers.Default)

    override fun observeAllCourseGrades(accountId: AccountId): Flow<Loadable<List<CourseGradeOverview>>> =
        gradeDao.observeAllCourseGrades(accountId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<CourseGradeOverview>> }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshCourseGradeItems(accountId: AccountId, courseId: CourseId, force: Boolean) {
        val key = RefreshKey(accountId, courseId)
        val deferred = itemsInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshCourseGradeItems(accountId, courseId, force)
            }.also { d -> d.invokeOnCompletion { itemsInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshCourseGradeItems(accountId: AccountId, courseId: CourseId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_GRADES, courseId.value.toLong())) return
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        val response = api.grades.getGradeItems(token, courseId.value, account.learning.lmsUserId, null)
        val items = response.userGrades
            .firstOrNull { it.userId == account.learning.lmsUserId }
            ?.gradeItems
            .orEmpty()
        val rows = items.mapIndexed { index, dto -> dto.toEntity(accountId, courseId.value, index) }
        gradeDao.replaceCourseGradeItems(accountId.value, courseId.value, rows)
        stamp(accountId, ElearningSyncScope.COURSE_GRADES, courseId.value.toLong())
    }

    override suspend fun refreshAllCourseGrades(accountId: AccountId, force: Boolean) {
        val key = accountId.value
        val deferred = overviewInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshAllCourseGrades(accountId, force)
            }.also { d -> d.invokeOnCompletion { overviewInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshAllCourseGrades(accountId: AccountId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.ALL_COURSE_GRADES, 0)) return
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        val (api, token) = sessionManager.elearning()
        val response = api.grades.getCourseGrades(token, account.learning.lmsUserId)
        // Course names come from EnrolledCourse cache or course details — left null here; UI can resolve.
        val rows = response.grades.map { it.toEntity(accountId, courseName = null) }
        gradeDao.replaceAllCourseGrades(accountId.value, rows)
        stamp(accountId, ElearningSyncScope.ALL_COURSE_GRADES, 0)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        gradeDao.clearAllForAccount(accountId.value)
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
