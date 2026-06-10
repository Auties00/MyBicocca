package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateEntity
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.mapper.calendar.toCalendarEvent
import it.attendance100.mybicocca.data.mapper.calendar.toDomain
import it.attendance100.mybicocca.data.mapper.calendar.toEntity
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffScheduleCell
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import it.attendance100.mybicocca.domain.repository.CalendarRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.ExamRepository
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates five sources behind one stream: EasyStaff lessons (weekly schedule grid) and
 * Esse3 exam bookings are materialized into calendar_events — month-keyed and career-global
 * respectively — while elearning deadlines, segreteria appointments and library seats are
 * merged in at observe time from the stores their own features already maintain. Each source
 * refreshes through its own deduplicated job so a slow source never delays the others; the
 * UI hydrates as rows land in Room.
 *
 * Cross-feature invalidation rides the [CalendarSyncInvalidator] bus: feature repositories
 * signal it after writes the calendar mirrors (e.g. booking an exam), and this repository
 * reacts by dropping the affected source's sync stamps and re-syncing it right away instead
 * of waiting out the TTL.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
    private val dao: CalendarDao,
    private val syncStateDao: CalendarSyncStateDao,
    private val deadlineDao: DeadlineDao,
    private val studyPlanRepository: StudyPlanRepository,
    private val transcriptRepository: TranscriptRepository,
    private val examRepository: ExamRepository,
    private val elearningCourseRepository: ElearningCourseRepository,
    private val appointmentRepository: AppointmentRepository,
    private val libraryRepository: LibraryRepository,
    private val invalidator: CalendarSyncInvalidator,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : CalendarRepository {

    private val inFlight = ConcurrentHashMap<SourceKey, Deferred<Unit>>()

    init {
        applicationScope.launch {
            invalidator.invalidations.collect { invalidation ->
                runCatching {
                    syncStateDao.deleteStatesForSource(
                        invalidation.careerId.value,
                        invalidation.source.code,
                    )
                    if (invalidation.source == EventSource.EXAM) {
                        refreshSource(examSourceJob(invalidation.careerId, force = true))
                    }
                }
            }
        }
    }

    override fun observeMonth(careerId: CareerId, yearMonth: YearMonth): Flow<Loadable<List<CalendarEvent>>> =
        observeRange(careerId, yearMonth.startDate(), yearMonth.endDate())
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it) }

    override fun observeDay(careerId: CareerId, date: LocalDate): Flow<Loadable<List<CalendarEvent>>> =
        observeRange(careerId, date, date)
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it) }

    override fun observeUpcoming(careerId: CareerId, from: LocalDate, limit: Int): Flow<Loadable<List<CalendarEvent>>> =
        observeRange(careerId, from, from.plusYears(1))
            .map<List<CalendarEvent>, Loadable<List<CalendarEvent>>> { Loadable.Loaded(it.take(limit)) }

    /**
     * Lessons are the layer every month is guaranteed to sync (even to an empty list), so
     * their stamp doubles as "this month completed a first load".
     */
    override fun observeMonthHydrated(careerId: CareerId, yearMonth: YearMonth): Flow<Boolean> =
        syncStateDao.observeState(careerId.value, EventSource.LESSON.code, yearMonth.toString())
            .map { it != null }
            .distinctUntilChanged()

    override suspend fun isMonthFresh(careerId: CareerId, yearMonth: YearMonth): Boolean =
        !isStale(careerId, EventSource.LESSON, yearMonth.toString()) &&
            !isStale(careerId, EventSource.EXAM, GLOBAL_SCOPE)

    /**
     * Fans out one job per source and awaits them all before reporting: required sources
     * (lessons, exams, deadlines) propagate their first failure once every job has settled,
     * while appointment and library jobs are best-effort and never fail the refresh.
     */
    override suspend fun refreshMonth(careerId: CareerId, yearMonth: YearMonth, force: Boolean) {
        val career = activeCareer(careerId) ?: return
        val accountId = sessionManager.activeAccount.value?.id
        coroutineScope {
            val required = listOf(
                async { refreshSource(lessonSourceJob(career, yearMonth, force)) },
                async { refreshSource(examSourceJob(careerId, force)) },
                async { refreshDeadlines(accountId, force) },
            )
            val auxiliary = listOf(
                async { runCatching { refreshSource(appointmentSourceJob(careerId, force)) } },
                async { runCatching { refreshSource(librarySourceJob(careerId, force)) } },
            )
            val outcomes = required.map { runCatching { it.await() } }
            auxiliary.forEach { it.await() }
            outcomes.firstOrNull { it.isFailure }?.exceptionOrNull()?.let { throw it }
        }
    }

    override suspend fun prefetchAdjacent(careerId: CareerId, yearMonth: YearMonth) {
        listOf(yearMonth.minusMonths(1), yearMonth.plusMonths(1)).forEach { neighbor ->
            applicationScope.launch {
                runCatching { refreshMonth(careerId, neighbor, force = false) }
            }
        }
    }

    /**
     * The merge point of the five sources: materialized rows from calendar_events combined
     * with the live deadline, appointment and library flows, sorted chronologically. Rows
     * that fail to rehydrate are dropped silently.
     */
    private fun observeRange(careerId: CareerId, start: LocalDate, end: LocalDate): Flow<List<CalendarEvent>> =
        combine(
            dao.observeInRange(careerId.value, start.toString(), end.toString())
                .map { rows -> rows.mapNotNull { entity -> runCatching { entity.toDomain() }.getOrNull() } },
            observeDeadlineEvents(careerId, start, end),
            observeAppointmentEvents(careerId, start, end),
            observeLibraryEvents(careerId, start, end),
        ) { stored, deadlines, appointments, library ->
            (stored + deadlines + appointments + library)
                .sortedWith(compareBy({ it.date }, { it.start }))
        }.flowOn(Dispatchers.Default)

    /**
     * Deadlines are cached per account, not per career, so the stream follows the active
     * account — but only while the observed career belongs to it, and it falls back to empty
     * otherwise. Filtering to the date range happens after mapping to the device zone.
     */
    private fun observeDeadlineEvents(careerId: CareerId, start: LocalDate, end: LocalDate): Flow<List<CalendarEvent>> =
        sessionManager.activeAccount
            .map { account ->
                account?.takeIf { it.academic.careers.any { career -> career.id == careerId } }?.id
            }
            .distinctUntilChanged()
            .flatMapLatest { accountId ->
                if (accountId == null) flowOf(emptyList())
                else deadlineDao.observeForAccount(accountId.value).map { rows ->
                    val zone = ZoneId.systemDefault()
                    rows.mapNotNull { it.toCalendarEvent(careerId, zone) }
                        .filter { it.date in start..end }
                }
            }

    /** Device-local appointment reservations clipped to the range, attributed to [careerId]. */
    private fun observeAppointmentEvents(careerId: CareerId, start: LocalDate, end: LocalDate): Flow<List<CalendarEvent>> =
        appointmentRepository.observeReservations().map { reservations ->
            reservations.map { it.toCalendarEvent(careerId) }.filter { it.date in start..end }
        }

    /** Library seat reservations clipped to the range, attributed to [careerId]; cancelled ones are dropped. */
    private fun observeLibraryEvents(careerId: CareerId, start: LocalDate, end: LocalDate): Flow<List<CalendarEvent>> =
        libraryRepository.observeReservations().map { reservations ->
            reservations.mapNotNull { it.toCalendarEvent(careerId) }.filter { it.date in start..end }
        }

    /**
     * One deduplicated job per (career, source, scope): concurrent month refreshes share the
     * same global exam/appointment/library sync instead of racing it.
     */
    private fun refreshSourceJob(key: SourceKey, block: suspend () -> Unit): Deferred<Unit> =
        inFlight.computeIfAbsent(key) {
            applicationScope.async(start = CoroutineStart.LAZY) { block() }
                .also { d -> d.invokeOnCompletion { inFlight.remove(key, d) } }
        }

    private suspend fun refreshSource(job: Deferred<Unit>) = job.await()

    private fun lessonSourceJob(career: Career, yearMonth: YearMonth, force: Boolean): Deferred<Unit> =
        refreshSourceJob(SourceKey(career.id, EventSource.LESSON, yearMonth.toString())) {
            if (force || isStale(career.id, EventSource.LESSON, yearMonth.toString())) {
                refreshLessons(career, yearMonth)
            }
        }

    private fun examSourceJob(careerId: CareerId, force: Boolean): Deferred<Unit> =
        refreshSourceJob(SourceKey(careerId, EventSource.EXAM, GLOBAL_SCOPE)) {
            if (force || isStale(careerId, EventSource.EXAM, GLOBAL_SCOPE)) {
                refreshExams(careerId)
            }
        }

    private fun appointmentSourceJob(careerId: CareerId, force: Boolean): Deferred<Unit> =
        refreshSourceJob(SourceKey(careerId, EventSource.APPOINTMENT, GLOBAL_SCOPE)) {
            if (force || isStale(careerId, EventSource.APPOINTMENT, GLOBAL_SCOPE)) {
                appointmentRepository.refreshReservations()
                stampSync(careerId, EventSource.APPOINTMENT, GLOBAL_SCOPE)
            }
        }

    /**
     * Having no linked library account is a normal state, not a failure: the refresh is
     * skipped, the local store stays empty and the calendar simply shows no library events.
     */
    private fun librarySourceJob(careerId: CareerId, force: Boolean): Deferred<Unit> =
        refreshSourceJob(SourceKey(careerId, EventSource.LIBRARY, GLOBAL_SCOPE)) {
            if (force || isStale(careerId, EventSource.LIBRARY, GLOBAL_SCOPE)) {
                try {
                    libraryRepository.refreshReservations()
                    stampSync(careerId, EventSource.LIBRARY, GLOBAL_SCOPE)
                } catch (_: LibraryNotLoggedInException) {
                }
            }
        }

    /**
     * Deadlines ride the enrolled-courses sync, which has its own TTL and in-flight
     * de-duplication — calling through is cheap when fresh.
     */
    private suspend fun refreshDeadlines(accountId: AccountId?, force: Boolean) {
        if (accountId == null) return
        elearningCourseRepository.refreshEnrolledCourses(accountId, force)
    }

    /**
     * Syncs the lesson layer of one month from the EasyStaff weekly grid. The subject set is
     * the study plan minus courses already passed per the transcript (their lessons are
     * noise); an empty set still clears the slice and stamps the sync so the month counts as
     * hydrated. One batched request covers each week touching the month — the grid endpoint
     * merges all the subjects into a single grid, returning the same cells as per-subject
     * calls at a fraction of the round-trips. Fetched cells are clipped to the month,
     * de-duplicated by id across the overlapping edge weeks, and splice-replaced into the
     * lesson slice.
     */
    private suspend fun refreshLessons(career: Career, yearMonth: YearMonth) {
        val raw = studyPlanRepository.getPlannedCoursesForActiveCareer(career.id)
        val passed = transcriptRepository.getPassedCourseNames(career.id)
        val plannedSubjects = raw.filterNot { it.normalizedName in passed }
        if (plannedSubjects.isEmpty()) {
            replaceSourceRange(career.id, EventSource.LESSON, yearMonth, emptyList())
            stampSync(career.id, EventSource.LESSON, yearMonth.toString())
            return
        }
        val academicYear = academicYearFor(yearMonth)
        val subjects = plannedSubjects.map { it.toEasyStaffSubject() }
        val activityCodeBySubjectCode = plannedSubjects
            .mapNotNull { pc -> pc.activityCode?.let { pc.easyStaffSubjectCode to it } }
            .toMap()
        val events = coroutineScope {
            yearMonth.coveringMondays().map { monday ->
                async {
                    easyStaffApi.schedule.getScheduleBySubjects(academicYear, subjects, monday)
                }
            }.awaitAll()
        }.asSequence()
            .flatten()
            .filterIsInstance<EasyStaffScheduleCell.Lesson>()
            .map { it.toDomain(career.id, activityCodeBySubjectCode[it.subjectCode]) }
            .filter { it.date in yearMonth.startDate()..yearMonth.endDate() }
            .distinctBy { it.id }
            .toList()
        replaceSourceRange(career.id, EventSource.LESSON, yearMonth, events)
        stampSync(career.id, EventSource.LESSON, yearMonth.toString())
    }

    /**
     * Syncs the exam layer from the Esse3 bookings endpoint, which returns the student's
     * whole booking history in one response — so the layer is career-global: everything is
     * replaced, not a month slice. Booked exams join to their activity codes by normalized
     * course name against the cached transcript.
     */
    private suspend fun refreshExams(careerId: CareerId) {
        val bookings = examRepository.getBookings(careerId)
        val activityCodes = transcriptActivityCodes(careerId)
        val events = bookings
            .mapNotNull { it.toCalendarEvent(careerId, activityCodes) }
            .distinctBy { it.id }
        dao.replaceSourceAll(
            careerId = careerId.value,
            source = EventSource.EXAM.code,
            rows = events.mapNotNull { it.toEntity() },
        )
        stampSync(careerId, EventSource.EXAM, GLOBAL_SCOPE)
    }

    /**
     * Bookings carry no activity code, but the cached transcript has both names and codes;
     * a name join lets exam events deep-link to their elearning course. Best-effort: an
     * empty map just means no "open course" action.
     */
    private suspend fun transcriptActivityCodes(careerId: CareerId): Map<String, String> =
        runCatching {
            transcriptRepository.observeRows(careerId).first().valueOrNull().orEmpty()
                .mapNotNull { row -> row.activityCode?.let { normalizeSubjectName(row.activityName) to it } }
                .toMap()
        }.getOrDefault(emptyMap())

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
            rows = events.mapNotNull { it.toEntity() },
        )
    }

    private suspend fun stampSync(careerId: CareerId, source: EventSource, scope: String) {
        syncStateDao.upsertState(
            CalendarSyncStateEntity(
                careerId = careerId.value,
                source = source.code,
                yearMonth = scope,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    /** True when the unit has never synced or its stamp is older than the source's TTL. */
    private suspend fun isStale(careerId: CareerId, source: EventSource, scope: String): Boolean {
        val state = syncStateDao.getState(careerId.value, source.code, scope) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(source.code)
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    /** Identity of one deduplicated sync unit; scope is a year-month or the global marker. */
    private data class SourceKey(val careerId: CareerId, val source: EventSource, val scope: String)

    private companion object {
        /**
         * Sync-state scope for sources whose remote returns the full career data set in one
         * call rather than a month slice.
         */
        const val GLOBAL_SCOPE = "*"
    }
}

private fun YearMonth.bounds(): Pair<LocalDate, LocalDate> = atDay(1) to atEndOfMonth()
private fun YearMonth.startDate(): LocalDate = atDay(1)
private fun YearMonth.endDate(): LocalDate = atEndOfMonth()

/**
 * Mondays of every week overlapping the month, including the spill-over weeks at both edges;
 * EasyStaff schedules are fetched per week starting on Monday.
 */
private fun YearMonth.coveringMondays(): List<LocalDate> {
    val firstMonday = atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastDay = atEndOfMonth()
    return generateSequence(firstMonday) { it.plusWeeks(1) }
        .takeWhile { !it.isAfter(lastDay) }
        .toList()
}

/** Italian academic years roll over in September: months before it belong to the prior start year. */
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
