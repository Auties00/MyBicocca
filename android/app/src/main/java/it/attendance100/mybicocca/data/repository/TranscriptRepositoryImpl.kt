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
import it.attendance100.mybicocca.domain.model.transcript.GradeRollup
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

    override suspend fun getPassedCourseNames(careerId: CareerId): Set<String> {
        val cached = dao.getPassedActivityNames(careerId.value)
        if (cached.isNotEmpty()) return cached.toNormalizedSet()
        runCatching { refresh(careerId, force = false) }
        return dao.getPassedActivityNames(careerId.value).toNormalizedSet()
    }

    private suspend fun doRefresh(careerId: CareerId, force: Boolean) {
        val career = activeCareer(careerId) ?: return
        val matId = career.enrollmentTraitId.takeIf { it > 0L } ?: return
        if (!force && !isStale(careerId)) return

        val esse3 = sessionManager.esse3()
        val (rowsDto, statsDto, averagesDto) = coroutineScope {
            val rowsAsync = async { esse3.transcript.getRecordBookRows(matId) }
            val statsAsync = async { esse3.transcript.getRecordBookStats(matId) }
            // The /stats endpoint returns an empty `medie` for some careers, so the averages
            // come from the dedicated endpoint and are merged in below.
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
