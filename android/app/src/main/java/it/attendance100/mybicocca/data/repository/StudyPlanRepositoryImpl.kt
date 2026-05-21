package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAcademicYear
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgram
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffStudyProgramSubject
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3PlansApi
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3State3
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlan
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanActivity
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3StudyPlanHeader
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyPlanRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
) : StudyPlanRepository {

    private val activitiesCache = ConcurrentHashMap<CareerId, CachedActivities>()
    private val programCache = ConcurrentHashMap<CareerId, CachedProgram>()
    private val plannedCache = ConcurrentHashMap<CareerId, CachedPlanned>()
    private val mutexes = ConcurrentHashMap<CareerId, Mutex>()

    override suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse> {
        plannedCache[careerId]?.takeIf { it.isFresh() }?.let { return it.courses }
        val mutex = mutexes.computeIfAbsent(careerId) { Mutex() }
        return mutex.withLock {
            plannedCache[careerId]?.takeIf { it.isFresh() }?.let { return@withLock it.courses }
            val career = activeCareer(careerId) ?: return@withLock emptyList()
            val result = coroutineScope {
                val activitiesDeferred = async { loadActivitiesUnlocked(careerId) }
                val programDeferred = async { loadProgramUnlocked(careerId, career) }
                val activities = activitiesDeferred.await()
                val program = programDeferred.await() ?: return@coroutineScope emptyList()
                if (activities.isEmpty()) emptyList() else intersect(activities, program)
            }
            plannedCache[careerId] = CachedPlanned(courses = result, cachedAtMs = nowMs())
            result
        }
    }

    override suspend fun getActivityYearByCodeForCareer(careerId: CareerId): Map<String, StudyYear> {
        val cachedActivities = activitiesCache[careerId]?.takeIf { it.isFresh() }?.activities
        val cachedProgram = programCache[careerId]?.takeIf { it.isFresh() }?.program
        if (cachedActivities != null && cachedProgram != null) {
            return cachedProgram.toYearByCode() + cachedActivities.toYearByCode()
        }
        val mutex = mutexes.computeIfAbsent(careerId) { Mutex() }
        return mutex.withLock {
            val career = activeCareer(careerId)
            coroutineScope {
                val activitiesDeferred = async { loadActivitiesUnlocked(careerId) }
                val programDeferred = async {
                    if (career == null) null else loadProgramUnlocked(careerId, career)
                }
                val activities = activitiesDeferred.await()
                val program = programDeferred.await()
                val planMap = activities.toYearByCode()
                val programMap = program?.toYearByCode() ?: emptyMap()
                // Personal plan overlays the program so the student's actual chosen
                // year wins on collisions; entries unique to the program are kept.
                programMap + planMap
            }
        }
    }

    private suspend fun loadActivitiesUnlocked(careerId: CareerId): List<Esse3StudyPlanActivity> {
        activitiesCache[careerId]?.takeIf { it.isFresh() }?.let { return it.activities }
        val esse3 = sessionManager.esse3()
        val plan = runCatching { fetchEsse3Plan(esse3.plans, careerId) }.getOrNull() ?: return emptyList()
        val activities = plan.activity
        activitiesCache[careerId] = CachedActivities(activities = activities, cachedAtMs = nowMs())
        return activities
    }

    private suspend fun loadProgramUnlocked(careerId: CareerId, career: Career): EasyStaffStudyProgram? {
        programCache[careerId]?.takeIf { it.isFresh() }?.let { return it.program }
        val program = runCatching { fetchEasyStaffProgram(career) }.getOrNull() ?: return null
        programCache[careerId] = CachedProgram(program = program, cachedAtMs = nowMs())
        return program
    }

    private fun List<Esse3StudyPlanActivity>.toYearByCode(): Map<String, StudyYear> = this
        .mapNotNull { a ->
            val code = a.activityTranscriptCode?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            code to (a.courseYear?.let(::StudyYear) ?: StudyYear.Unknown)
        }
        .toMap()

    private fun EasyStaffStudyProgram.toYearByCode(): Map<String, StudyYear> = years
        .flatMap { y -> y.subjects.map { s -> s.code to StudyYear(y.year) } }
        .toMap()

    private suspend fun fetchEsse3Plan(
        plansApi: Esse3PlansApi,
        careerId: CareerId,
    ): Esse3StudyPlan? {
        val headers = runCatching {
            plansApi.getStudentPlanHeaders(studentId = careerId.value)
        }.getOrDefault(emptyList())
        val header = headers.pickBest() ?: return null
        val planId = header.planId?.toLong() ?: return null
        return runCatching { plansApi.getStudentPlan(careerId.value, planId) }.getOrNull()
    }

    private suspend fun fetchEasyStaffProgram(career: Career): EasyStaffStudyProgram? {
        val code = career.easyStaffProgramCode ?: return null
        val academicYear = currentAcademicYear()
        val programs = runCatching {
            easyStaffApi.core.getStudyPrograms(academicYear)
        }.getOrDefault(emptyList())
        return programs.firstOrNull { it.code == code }
    }

    private fun intersect(
        activities: List<Esse3StudyPlanActivity>,
        program: EasyStaffStudyProgram,
    ): List<PlannedCourse> {
        val subjects = program.years.flatMap { it.subjects }
        val byCode: Map<String, EasyStaffStudyProgramSubject> = subjects.associateBy { it.code }
        val byName: Map<String, EasyStaffStudyProgramSubject> =
            subjects.associateBy { normalizeSubjectName(it.name) }
        return activities.mapNotNull { activity ->
            val activityName = activity.activityTranscriptDescription ?: return@mapNotNull null
            val activityCode = activity.activityTranscriptCode
            val subject = (activityCode?.let(byCode::get))
                ?: byName[normalizeSubjectName(activityName)]
                ?: return@mapNotNull null
            PlannedCourse(
                easyStaffSubjectId = subject.id,
                easyStaffSubjectCode = subject.code,
                name = subject.name.ifBlank { activityName },
                normalizedName = normalizeSubjectName(activityName),
                teacherName = subject.teacherName,
                periodId = subject.periodId,
                studyYear = activity.courseYear?.let(::StudyYear) ?: StudyYear.Unknown,
                cfu = activity.weight?.toInt(),
            )
        }.distinctBy { it.easyStaffSubjectCode }
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    private fun CachedActivities.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun CachedProgram.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun CachedPlanned.isFresh(): Boolean = nowMs() - cachedAtMs <= CACHE_TTL_MS
    private fun nowMs(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    private data class CachedActivities(val activities: List<Esse3StudyPlanActivity>, val cachedAtMs: Long)
    private data class CachedProgram(val program: EasyStaffStudyProgram, val cachedAtMs: Long)
    private data class CachedPlanned(val courses: List<PlannedCourse>, val cachedAtMs: Long)

    private companion object {
        const val CACHE_TTL_MS = 30L * 60_000L
    }
}

private fun List<Esse3StudyPlanHeader>.pickBest(): Esse3StudyPlanHeader? {
    val approved = firstOrNull { it.state is Esse3State3.Approved }
    return approved ?: maxByOrNull { it.planId ?: Int.MIN_VALUE }
}

private fun currentAcademicYear(): EasyStaffAcademicYear {
    val today = LocalDate.now()
    val startYear = if (today.monthValue >= 9) today.year else today.year - 1
    return EasyStaffAcademicYear(startYear = startYear)
}
