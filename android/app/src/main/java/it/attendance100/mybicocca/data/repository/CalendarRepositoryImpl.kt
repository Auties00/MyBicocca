package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateEntity
import it.attendance100.mybicocca.data.mapper.calendar.toDomain
import it.attendance100.mybicocca.data.mapper.calendar.toEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
    private val dao: CalendarDao,
    private val syncStateDao: CalendarSyncStateDao,
    private val studyPlanRepository: StudyPlanRepository,
    private val transcriptRepository: TranscriptRepository,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : CalendarRepository {

    private val inFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()

    override fun observeMonth(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>> {
        val (start, end) = yearMonth.bounds()
        return dao.observeInRange(careerId.value, start.toString(), end.toString())
            .map { rows -> rows.toDomainEvents().sortedByDateAndStart() }
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)
    }

    override fun observeDay(careerId: CareerId, date: LocalDate): Flow<Loadable<List<CalendarEvent>>> =
        dao.observeForDay(careerId.value, date.toString())
            .map { rows -> rows.toDomainEvents().sortedByDateAndStart() }
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)

    override fun observeUpcoming(careerId: CareerId, from: LocalDate, limit: Int): Flow<Loadable<List<CalendarEvent>>> =
        dao.observeFrom(careerId.value, from.toString(), limit)
            .map { rows -> rows.toDomainEvents().sortedByDateAndStart() }
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it) }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshMonth(careerId: CareerId, yearMonth: YearMonth, force: Boolean) {
        val key = RefreshKey(careerId, yearMonth)
        val deferred = inFlight.computeIfAbsent(key) {
            applicationScope.async(start = CoroutineStart.LAZY) {
                doRefresh(careerId, yearMonth, force)
            }.also { d -> d.invokeOnCompletion { inFlight.remove(key, d) } }
        }
        deferred.await()
    }

    override suspend fun prefetchAdjacent(careerId: CareerId, yearMonth: YearMonth) {
        listOf(yearMonth.minusMonths(1), yearMonth.plusMonths(1)).forEach { neighbor ->
            applicationScope.launch {
                runCatching { refreshMonth(careerId, neighbor, force = false) }
            }
        }
    }

    private suspend fun doRefresh(careerId: CareerId, yearMonth: YearMonth, force: Boolean) {
        val career = activeCareer(careerId) ?: return
        val needsLessons = force || isStale(careerId, EventSource.LESSON, yearMonth)
        val needsExams = force || isStale(careerId, EventSource.EXAM, yearMonth)
        if (!needsLessons && !needsExams) return

        val raw = runCatching { studyPlanRepository.getPlannedCoursesForActiveCareer(careerId) }.getOrDefault(emptyList())
        val passed = runCatching { transcriptRepository.getPassedCourseNames(careerId) }.getOrDefault(emptySet())
        val plannedSubjects = raw.filterNot { it.normalizedName in passed }

        coroutineScope {
            val jobs = mutableListOf<Deferred<Unit>>()
            if (needsLessons) jobs += async { refreshLessons(career, yearMonth, plannedSubjects) }
            if (needsExams) jobs += async { refreshExams(career, yearMonth, plannedSubjects) }
            val outcomes = jobs.map { runCatching { it.await() } }
            outcomes.firstOrNull { it.isFailure }?.exceptionOrNull()?.let { throw it }
        }
    }

    private suspend fun refreshLessons(career: Career, yearMonth: YearMonth, plannedSubjects: List<PlannedCourse>) {
        if (plannedSubjects.isEmpty()) {
            replaceSourceRange(career.id, EventSource.LESSON, yearMonth, emptyList())
            stampSync(career.id, EventSource.LESSON, yearMonth)
            return
        }
        val academicYear = academicYearFor(yearMonth)
        val mondays = yearMonth.coveringMondays()
        val events = coroutineScope {
            plannedSubjects.map { pc ->
                async {
                    val subject = pc.toEasyStaffSubject()
                    mondays.flatMap { monday ->
                        runCatching {
                            easyStaffApi.schedule.getScheduleBySubject(academicYear, subject, monday)
                        }.getOrDefault(emptyList())
                    }.filterIsInstance<EasyStaffScheduleCell.Lesson>()
                        .map { it.toDomain(career.id, pc.activityCode) }
                }
            }.awaitAll()
        }.asSequence()
            .flatten()
            .filter { it.date in yearMonth.startDate()..yearMonth.endDate() }
            .distinctBy { it.id }.toList()
        replaceSourceRange(career.id, EventSource.LESSON, yearMonth, events)
        stampSync(career.id, EventSource.LESSON, yearMonth)
    }

    private suspend fun refreshExams(career: Career, yearMonth: YearMonth, plannedSubjects: List<PlannedCourse>) {
        if (plannedSubjects.isEmpty()) {
            replaceSourceRange(career.id, EventSource.EXAM, yearMonth, emptyList())
            stampSync(career.id, EventSource.EXAM, yearMonth)
            return
        }
        val (start, end) = yearMonth.bounds()
        val events = coroutineScope {
            plannedSubjects.map { pc ->
                async {
                    val subject = pc.toEasyStaffSubject()
                    runCatching {
                        easyStaffApi.exams.getExamsBySubject(subject, start, end)
                    }.getOrDefault(emptyList()).map { it.toDomain(career.id, pc.activityCode) }
                }
            }.awaitAll()
        }.flatten()
            .distinctBy { it.id }
        replaceSourceRange(career.id, EventSource.EXAM, yearMonth, events)
        stampSync(career.id, EventSource.EXAM, yearMonth)
    }

    private suspend fun replaceSourceRange(
        careerId: CareerId,
        source: EventSource,
        yearMonth: YearMonth,
        events: List<CalendarEvent>,
    ) {
        val (start, end) = yearMonth.bounds()
        dao.replaceSource(
            careerId = careerId.value,
            source = source.code,
            startIso = start.toString(),
            endIso = end.toString(),
            rows = events.map { it.toEntity() },
        )
    }

    private suspend fun stampSync(careerId: CareerId, source: EventSource, yearMonth: YearMonth) {
        syncStateDao.upsertState(
            CalendarSyncStateEntity(
                careerId = careerId.value,
                source = source.code,
                yearMonth = yearMonth.toString(),
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private suspend fun isStale(careerId: CareerId, source: EventSource, yearMonth: YearMonth): Boolean {
        val state = syncStateDao.getState(careerId.value, source.code, yearMonth.toString()) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(source.code)
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    private data class RefreshKey(val careerId: CareerId, val yearMonth: YearMonth)
}

private fun YearMonth.bounds(): Pair<LocalDate, LocalDate> = atDay(1) to atEndOfMonth()
private fun YearMonth.startDate(): LocalDate = atDay(1)
private fun YearMonth.endDate(): LocalDate = atEndOfMonth()

private fun YearMonth.coveringMondays(): List<LocalDate> {
    val firstMonday = atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastDay = atEndOfMonth()
    return generateSequence(firstMonday) { it.plusWeeks(1) }
        .takeWhile { !it.isAfter(lastDay) }
        .toList()
}

private fun academicYearFor(yearMonth: YearMonth): EasyStaffAcademicYear {
    val startYear = if (yearMonth.monthValue >= 9) yearMonth.year else yearMonth.year - 1
    return EasyStaffAcademicYear(startYear = startYear)
}

private fun PlannedCourse.toEasyStaffSubject(): EasyStaffStudyProgramSubject =
    EasyStaffStudyProgramSubject(
        id = easyStaffSubjectId,
        code = easyStaffSubjectCode,
        name = name,
        teacherName = teacherName,
        periodId = periodId,
    )

private fun List<CalendarEventEntity>.toDomainEvents(): List<CalendarEvent> =
    mapNotNull { entity -> runCatching { entity.toDomain() }.getOrNull() }

private fun List<CalendarEvent>.sortedByDateAndStart(): List<CalendarEvent> =
    sortedWith(compareBy({ it.date }, { it.start }))
