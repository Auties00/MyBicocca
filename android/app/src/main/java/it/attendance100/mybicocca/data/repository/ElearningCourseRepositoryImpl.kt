package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.local.settings.ElearningFilterStore
import it.attendance100.mybicocca.data.mapper.elearning.toDeadlineEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.mapper.elearning.toModuleEntities
import it.attendance100.mybicocca.data.mapper.elearning.toStaffEntities
import it.attendance100.mybicocca.data.mapper.elearning.toSyllabusEntity
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock.System

/**
 * Course repository backed by the Moodle web services and the Room course cache.
 * Enrolled courses come from the user-courses endpoint and are stored alongside the
 * account's deadlines, which are paged out of the calendar action-events endpoint;
 * course detail fans out concurrently to the course-contents, public-course-info and
 * activity-completion endpoints, each tolerated to fail independently so one bad
 * endpoint doesn't lose the others' data. Refreshes are deduplicated per key through
 * lazily-started application-scoped jobs (concurrent callers await the same fetch,
 * which also survives the caller's cancellation), gated by the per-scope staleness
 * rows unless forced, and re-stamped on success. Observe flows combine the Room
 * tables off the main thread.
 */
@Singleton
class ElearningCourseRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val courseDao: CourseDao,
    private val deadlineDao: DeadlineDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    private val filterStore: ElearningFilterStore,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningCourseRepository {

    override fun observeFilter(): Flow<CourseFilter> = filterStore.filter

    override suspend fun setFilter(filter: CourseFilter) = filterStore.setFilter(filter)

    private val enrolledInFlight = ConcurrentHashMap<String, Deferred<Unit>>()
    private val detailsInFlight = ConcurrentHashMap<DetailKey, Deferred<Unit>>()

    override fun observeEnrolledCourses(accountId: AccountId): Flow<Loadable<List<EnrolledCourse>>> =
        combine(
            courseDao.observeEnrolled(accountId.value),
            deadlineDao.observeForAccount(accountId.value),
        ) { rows, deadlineRows ->
            val deadlinesByCourse: Map<Int, List<Deadline>> = deadlineRows
                .mapNotNull { it.toDomain() }
                .groupBy { it.courseId.value }
            Loadable.Loaded(
                rows.map { row -> row.toDomain(deadlinesByCourse[row.courseId].orEmpty()) }
            ) as Loadable<List<EnrolledCourse>>
        }.flowOn(Dispatchers.Default)

    override fun observeCourseDetails(
        accountId: AccountId,
        courseId: CourseId,
    ): Flow<Loadable<CourseDetails>> {
        val acc = accountId.value
        val cid = courseId.value
        val enrolledFlow = courseDao.observeEnrolledOne(acc, cid)
        val sectionsFlow = courseDao.observeSections(acc, cid)
        val modulesFlow = courseDao.observeModules(acc, cid)
        val staffFlow = courseDao.observeStaff(acc, cid)
        val syllabusFlow = courseDao.observeSyllabus(acc, cid)
        return combine(enrolledFlow, sectionsFlow, modulesFlow, staffFlow, syllabusFlow) {
                enrolled, sections, modules, staff, syllabus ->
            if (enrolled == null) Loadable.NotYetLoaded as Loadable<CourseDetails>
            else Loadable.Loaded(
                CourseDetails(
                    enrolled = enrolled.toDomain(),
                    sections = sections.map { it.toDomain(modules) },
                    staff = staff.map { it.toDomain() },
                    syllabus = syllabus?.toDomain(),
                ),
            )
        }.flowOn(Dispatchers.Default)
    }

    override fun observeCompletionStates(
        accountId: AccountId,
        courseId: CourseId,
    ): Flow<Map<Int, CompletionState>> {
        return courseDao.observeCompletion(accountId.value, courseId.value)
            .map { rows -> rows.associate { it.cmId to it.toDomain() } }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun refreshEnrolledCourses(accountId: AccountId, force: Boolean) {
        val key = accountId.value
        val deferred = enrolledInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                if (!force && !isStale(accountId, ElearningSyncScope.ENROLLED_COURSES, 0)) {
                    return@async
                }
                coroutineScope {
                    val coursesAsync = async { fetchAndStoreEnrolled(accountId) }
                    val deadlinesAsync = async { fetchAndStoreDeadlines(accountId) }
                    coursesAsync.await()
                    deadlinesAsync.await()
                }
            }.also { d -> d.invokeOnCompletion { enrolledInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun fetchAndStoreEnrolled(accountId: AccountId) {
        val account = sessionManager.activeAccount.value ?: error("No active account.")
        require(account.id == accountId) { "Active account changed during refresh." }
        val (api, token) = sessionManager.elearning()
        val response = api.courses.getUserCourses(token, account.learning.lmsUserId)
        val rows = response.courses.mapIndexed { index, dto -> dto.toEntity(accountId, index) }
        courseDao.replaceEnrolled(accountId.value, rows)
        stamp(accountId, ElearningSyncScope.ENROLLED_COURSES, 0)
    }

    private suspend fun fetchAndStoreDeadlines(accountId: AccountId) {
        val (api, token) = sessionManager.elearning()
        val from = 0L
        val rows = mutableListOf<DeadlineEntity>()
        var afterEventId: Int? = null
        while (true) {
            val response = api.calendar.getActionEventsByTimesort(
                wsToken = token,
                timesortFrom = from,
                afterEventId = afterEventId,
                limitNum = DEADLINE_PAGE_SIZE,
            )
            if (response.events.isEmpty()) break
            response.events.forEach { event ->
                event.toDeadlineEntity(accountId)?.let(rows::add)
            }
            if (response.events.size < DEADLINE_PAGE_SIZE) break
            afterEventId = response.lastId ?: break
        }
        deadlineDao.replaceForAccount(accountId.value, resolveDeadlineInstanceIds(rows))
    }

    /**
     * Rewrites each deadline's instance id from the course-module id to the real
     * module instance id. The calendar exporter puts the course-module id in
     * `instance` ($cm->get('id') in event_exporter_base), but assignment/quiz rows and
     * their detail screens are keyed by module instance ids; the translation goes
     * through the batch assignment/quiz list endpoints, and rows that can't be
     * resolved are dropped since an unresolvable deadline couldn't be opened anyway.
     */
    private suspend fun resolveDeadlineInstanceIds(rows: List<DeadlineEntity>): List<DeadlineEntity> {
        val (api, token) = sessionManager.elearning()
        return coroutineScope {
            val assignCourseIds = rows
                .filter { it.kind == DeadlineEntity.Kind.ASSIGNMENT }
                .map { it.courseId }
                .distinct()
            val quizCourseIds = rows
                .filter { it.kind == DeadlineEntity.Kind.QUIZ }
                .map { it.courseId }
                .distinct()
            val assignIdByCmId = async {
                if (assignCourseIds.isEmpty()) emptyMap()
                else api.assignments.getAssignments(token, assignCourseIds)
                    .courses
                    .flatMap { it.assignments }
                    .mapNotNull { dto -> dto.courseModuleId?.let { it to dto.id } }
                    .toMap()
            }
            val quizIdByCmId = async {
                if (quizCourseIds.isEmpty()) emptyMap()
                else api.quizzes.getQuizzes(token, quizCourseIds)
                    .quizzes
                    .associate { it.courseModuleId to it.id }
            }
            rows.mapNotNull { row ->
                val instanceId = when (row.kind) {
                    DeadlineEntity.Kind.ASSIGNMENT -> assignIdByCmId.await()[row.instanceId]
                    DeadlineEntity.Kind.QUIZ -> quizIdByCmId.await()[row.instanceId]
                    else -> null
                }
                instanceId?.let { row.copy(instanceId = it) }
            }
        }
    }

    override suspend fun refreshCourseDetails(
        accountId: AccountId,
        courseId: CourseId,
        force: Boolean,
    ) {
        val key = DetailKey(accountId, courseId)
        val deferred = detailsInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshDetails(accountId, courseId, force)
            }.also { d -> d.invokeOnCompletion { detailsInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshDetails(
        accountId: AccountId,
        courseId: CourseId,
        force: Boolean,
    ) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_DETAILS, courseId.value.toLong())) return
        val (api, token) = sessionManager.elearning()
        coroutineScope {
            val sectionsAsync = async { runCatching { api.courses.getCourseContents(token, courseId.value) }.getOrNull() }
            val publicAsync = async { runCatching { api.courses.getCoursePublicInfo(courseId.value) }.getOrNull() }
            val completionAsync = async {
                runCatching { api.completion.getActivitiesCompletionStatus(token, courseId.value, null) }.getOrNull()
            }
            val sectionsResp = sectionsAsync.await()
            val publicInfo = publicAsync.await()
            val completion = completionAsync.await()

            if (sectionsResp != null) {
                val sectionRows = sectionsResp.sections.map { it.toEntity(accountId, courseId.value) }
                val moduleRows = sectionsResp.sections.toModuleEntities(accountId, courseId.value)
                courseDao.replaceCourseStructure(accountId.value, courseId.value, sectionRows, moduleRows)
            }
            if (publicInfo != null) {
                val staffRows = publicInfo.toStaffEntities(accountId, courseId.value)
                courseDao.replaceCourseStaff(accountId.value, courseId.value, staffRows)
                publicInfo.toSyllabusEntity(accountId, courseId.value)?.let { courseDao.upsertSyllabus(it) }
            }
            if (completion != null) {
                val completionRows = completion.statuses.map { it.toEntity(accountId, courseId.value) }
                if (completionRows.isNotEmpty()) courseDao.upsertCompletionAll(completionRows)
            }
        }
        stamp(accountId, ElearningSyncScope.COURSE_DETAILS, courseId.value.toLong())
    }

    override suspend fun setActivityCompleted(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        completed: Boolean,
    ) {
        val (api, token) = sessionManager.elearning()
        api.completion.updateActivityCompletion(token, cmId, completed)
        runCatching {
            val statuses = api.completion.getActivitiesCompletionStatus(token, courseId.value, null)
            val rows = statuses.statuses.map { it.toEntity(accountId, courseId.value) }
            if (rows.isNotEmpty()) courseDao.upsertCompletionAll(rows)
        }
    }

    /** Local-only by design; Moodle's user-preference API would be best-effort at most. */
    override suspend fun setFavourite(accountId: AccountId, courseId: CourseId, favourite: Boolean) {
        courseDao.setFavourite(accountId.value, courseId.value, favourite)
    }

    override suspend fun setHidden(accountId: AccountId, courseId: CourseId, hidden: Boolean) {
        courseDao.setHidden(accountId.value, courseId.value, hidden)
    }

    override suspend fun enrolIntoCourse(accountId: AccountId, courseId: CourseId, password: String?) {
        val (api, token) = sessionManager.elearning()
        val response = api.courses.enrollIntoCourse(token, courseId.value, password)
        if (!response.status) {
            val warning = response.warnings.firstOrNull()?.message
            error(warning ?: "Iscrizione al corso non riuscita.")
        }
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        courseDao.clearAllForAccount(accountId.value)
        deadlineDao.deleteForAccount(accountId.value)
    }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = System.now().toEpochMilliseconds(),
            )
        )
    }

    private data class DetailKey(val accountId: AccountId, val courseId: CourseId)

    private companion object {
        const val DEADLINE_PAGE_SIZE = 50
    }
}
