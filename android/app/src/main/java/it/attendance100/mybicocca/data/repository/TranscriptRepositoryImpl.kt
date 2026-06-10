package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.transcript.TranscriptDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateEntity
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.mapper.transcript.toDomain
import it.attendance100.mybicocca.data.mapper.transcript.toEntity
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseAttempt
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptStats
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of the transcript contract: the observe methods stream
 * straight from the career-scoped Room tables (single source of truth) while [refresh]
 * reconciles them with Esse3's record-book endpoints (`/libretti/{matId}/righe`,
 * `/stats`, `/medie`) in one atomic swap. Refreshes are TTL-gated through the sync-state
 * table and de-duplicated per career, so concurrent callers share a single in-flight
 * sync. Per-activity detail bypasses Room entirely and is fetched live on demand.
 */
@Singleton
class TranscriptRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dao: TranscriptDao,
    private val syncStateDao: TranscriptSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : TranscriptRepository {

    private val inFlight = ConcurrentHashMap<CareerId, Deferred<Unit>>()

    override fun observeRows(careerId: CareerId): Flow<Loadable<List<TranscriptRow>>> =
        dao.observeRows(careerId.value)
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()
            .map<List<TranscriptRow>, Loadable<List<TranscriptRow>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)

    override fun observeStats(careerId: CareerId): Flow<Loadable<TranscriptStats>> =
        dao.observeStats(careerId.value)
            .distinctUntilChanged()
            .map { entity -> entity?.let { Loadable.Loaded(it.toDomain()) } ?: Loadable.NotYetLoaded }
            .flowOn(Dispatchers.Default)

    override fun observeGradeRollup(careerId: CareerId): Flow<Loadable<GradeRollup>> =
        dao.observeGradeRollup(careerId.value)
            .distinctUntilChanged()
            .map { projection -> projection?.let { Loadable.Loaded(it.toDomain()) } ?: Loadable.NotYetLoaded }
            .flowOn(Dispatchers.Default)

    override suspend fun refresh(careerId: CareerId, force: Boolean) {
        val deferred = inFlight.computeIfAbsent(careerId) {
            applicationScope.async(start = CoroutineStart.LAZY) {
                doRefresh(careerId, force)
            }.also { d -> d.invokeOnCompletion { inFlight.remove(careerId, d) } }
        }
        deferred.await()
    }

    /**
     * Fans out three concurrent live fetches: the attempt history (the primary fetch,
     * whose failure propagates), the prerequisite check (skipped for passed activities
     * because the endpoint responds 422 on them, and degraded to Unknown on any failure
     * rather than surfacing a misleading warning), and a cheap single-row re-read that
     * yields a fresh bookable-calls count without relying on a possibly-stale cached
     * value.
     */
    override suspend fun getCourseDetail(
        careerId: CareerId,
        activityChoiceId: Long,
        alreadyPassed: Boolean,
    ): CourseDetail {
        val career = activeCareer(careerId) ?: error("No active career for $careerId")
        val matId = career.enrollmentTraitId.takeIf { it > 0L } ?: error("No enrollment trait for $careerId")
        val esse3 = sessionManager.esse3()

        return coroutineScope {
            val attemptsAsync = async {
                esse3.transcript.getRecordBookRowTests(matId, activityChoiceId)
                    .map { it.toDomain() }
            }
            val prereqAsync = async {
                if (alreadyPassed) PrerequisiteStatus.Unknown
                else fetchPrerequisiteStatus(esse3, matId, activityChoiceId)
            }
            val bookableAsync = async {
                runCatching {
                    esse3.transcript.getRecordBookRow(matId, activityChoiceId).bookableCallsNumber
                }.getOrNull()
            }

            val attempts: List<CourseAttempt> = attemptsAsync.await()
                .sortedByDescending { it.callDate }
            CourseDetail(
                attempts = attempts,
                prerequisites = prereqAsync.await(),
                bookableCallsCount = bookableAsync.await() ?: 0,
            )
        }
    }

    override suspend fun getPrerequisiteStatus(
        careerId: CareerId,
        activityChoiceId: Long,
    ): PrerequisiteStatus {
        val career = activeCareer(careerId) ?: return PrerequisiteStatus.Unknown
        val matId = career.enrollmentTraitId.takeIf { it > 0L } ?: return PrerequisiteStatus.Unknown
        return fetchPrerequisiteStatus(sessionManager.esse3(), matId, activityChoiceId)
    }

    private suspend fun fetchPrerequisiteStatus(
        esse3: it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api,
        matId: Long,
        activityChoiceId: Long,
    ): PrerequisiteStatus = runCatching {
        esse3.transcript.getCheckProposalRecordBookRow(matId, activityChoiceId).toDomain()
    }.getOrDefault(PrerequisiteStatus.Unknown)

    /**
     * Serves normalized passed-course names from the Room cache, triggering a
     * best-effort refresh first when the cache is empty so first-run callers still get
     * data.
     */
    override suspend fun getPassedCourseNames(careerId: CareerId): Set<String> {
        val cached = dao.getPassedActivityNames(careerId.value)
        if (cached.isNotEmpty()) return cached.toNormalizedSet()
        runCatching { refresh(careerId, force = false) }
        return dao.getPassedActivityNames(careerId.value).toNormalizedSet()
    }

    /**
     * Fetches rows, stats and averages concurrently and swaps them into Room in one
     * transaction. The `/stats` endpoint returns an empty `medie` for some careers, so
     * the averages also come from the dedicated endpoint (best-effort) and are merged
     * into the stats when the inline ones are missing.
     */
    private suspend fun doRefresh(careerId: CareerId, force: Boolean) {
        val career = activeCareer(careerId) ?: return
        val matId = career.enrollmentTraitId.takeIf { it > 0L } ?: return
        if (!force && !isStale(careerId)) return

        val esse3 = sessionManager.esse3()
        val (rowsDto, statsDto, averagesDto) = coroutineScope {
            val rowsAsync = async { esse3.transcript.getRecordBookRows(matId) }
            val statsAsync = async { esse3.transcript.getRecordBookStats(matId) }
            val averagesAsync = async {
                runCatching { esse3.transcript.getRecordBookAverages(matId) }.getOrDefault(emptyList())
            }
            Triple(rowsAsync.await(), statsAsync.await(), averagesAsync.await())
        }

        val effectiveStats = if (statsDto.averages.isEmpty()) statsDto.copy(averages = averagesDto) else statsDto
        val rowEntities = rowsDto.map { it.toEntity(careerId) }
        val statsEntity = effectiveStats.toEntity(careerId)
        dao.replaceAll(careerId.value, rowEntities, statsEntity)
        syncStateDao.upsertState(
            TranscriptSyncStateEntity(
                careerId = careerId.value,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private suspend fun isStale(careerId: CareerId): Boolean {
        val state = syncStateDao.getState(careerId.value) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(SOURCE_KEY)
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    private fun List<String>.toNormalizedSet(): Set<String> =
        asSequence()
            .map(::normalizeSubjectName)
            .filter { it.isNotEmpty() }
            .toSet()

    private companion object {
        const val SOURCE_KEY = "transcript"
    }
}
