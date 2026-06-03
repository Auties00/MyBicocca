package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.attendance.toDomain
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceRecord
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
    private val studyPlanRepository: StudyPlanRepository,
    private val transcriptRepository: TranscriptRepository,
    private val elearningCourseRepository: ElearningCourseRepository,
) : AttendanceRepository {

    override suspend fun getPendingCourses(careerId: CareerId): List<CourseAttendance> = coroutineScope {
        val planDeferred = async { studyPlanRepository.getStudyPlan(careerId) }
        val plannedDeferred = async {
            runCatching { studyPlanRepository.getPlannedCoursesForActiveCareer(careerId) }
                .getOrDefault(emptyList())
        }
        val transcriptDeferred = async { loadTranscriptRows(careerId) }
        val badgeDeferred = async { loadBadgeRecords(careerId) }
        val enrolledDeferred = async { loadEnrolledCourses() }

        // The plan is the page's backbone: a failure here must surface as a sync error.
        // The enrichment sources (badge, elearning) degrade to "no data" instead.
        val plan = planDeferred.await()
        val planned = plannedDeferred.await()
        val transcriptRows = transcriptDeferred.await()

        val today = LocalDate.now()
        val academicYear = academicYearOf(today)
        val currentSemester = semesterOf(today)
        val currentYear = currentYearOfStudy(transcriptRows, academicYear, plan)

        val plannedByName = planned.associateBy { it.normalizedName }
        val transcriptByCode = transcriptRows
            .mapNotNull { row -> row.activityCode?.let { it to row } }
            .toMap()
        val transcriptByName = transcriptRows.associateBy { normalizeSubjectName(it.activityName) }

        val seeds = baseCourses(plan, planned)
            .distinctBy { it.code ?: it.normalizedName }
            .mapNotNull { seed ->
                val row = seed.code?.let(transcriptByCode::get) ?: transcriptByName[seed.normalizedName]
                if (row?.state == TranscriptRowState.Passed) return@mapNotNull null
                if (!isWithinCurrentPosition(seed.year, seed.semester, currentYear, currentSemester)) {
                    return@mapNotNull null
                }
                seed
            }

        val badgeRecords = badgeDeferred.await()
        val enrolled = enrolledDeferred.await()
        // Only the current academic year's course editions carry a live session register.
        val academicYearPrefix = "%02d%02d".format(academicYear % 100, (academicYear + 1) % 100)

        seeds
            .map { seed ->
                async {
                    CourseAttendance(
                        name = seed.name,
                        code = seed.code,
                        year = seed.year,
                        semester = seed.semester,
                        credits = seed.credits,
                        teacherName = seed.teacherName,
                        classroomAttendance = matchBadgeRecord(badgeRecords, seed),
                        sessionAttendance = loadSessionAttendance(enrolled, seed, academicYearPrefix),
                    )
                }
            }
            .awaitAll()
            .sortedWith(compareBy({ it.year.value }, { it.semester.ordinal }, { it.name }))
    }

    // One pending-course candidate before attendance enrichment. The plan is the
    // primary source; the EasyStaff-planned list fills in semester and teacher.
    private data class CourseSeed(
        val name: String,
        val code: String?,
        val normalizedName: String,
        val credits: Float,
        val year: StudyYear,
        val semester: Semester,
        val teacherName: String?,
    )

    private fun baseCourses(plan: StudyPlan?, planned: List<PlannedCourse>): List<CourseSeed> {
        val plannedByName = planned.associateBy { it.normalizedName }

        fun seedOf(name: String, code: String?, credits: Float, year: StudyYear): CourseSeed {
            val normalizedName = normalizeSubjectName(name)
            val plannedMatch = plannedByName[normalizedName]
            return CourseSeed(
                name = name,
                code = code,
                normalizedName = normalizedName,
                credits = credits,
                year = year.takeIf { it != StudyYear.Unknown } ?: plannedMatch?.studyYear ?: StudyYear.Unknown,
                semester = plannedMatch?.semester ?: Semester.Unknown,
                teacherName = plannedMatch?.teacherName?.takeIf { it.isNotBlank() },
            )
        }

        val planCourses = plan?.courses.orEmpty()
        if (planCourses.isNotEmpty()) {
            return planCourses.map { seedOf(it.name, it.code, it.credits, it.year) }
        }
        // No personal plan on Esse3: fall back to the EasyStaff-intersected list.
        return planned.map { seedOf(it.name, null, it.cfu?.toFloat() ?: 0f, it.studyYear) }
    }

    // "Up to the current semester": everything from earlier years, plus the current
    // year's courses excluding second-semester ones while the first is ongoing.
    // Unknowns lean towards inclusion — hiding a real course is worse than showing
    // one a semester early.
    private fun isWithinCurrentPosition(
        year: StudyYear,
        semester: Semester,
        currentYear: StudyYear,
        currentSemester: Semester,
    ): Boolean {
        if (currentYear == StudyYear.Unknown || year == StudyYear.Unknown) return true
        if (year < currentYear) return true
        if (year > currentYear) return false
        return currentSemester == Semester.Second || semester != Semester.Second
    }

    private suspend fun loadTranscriptRows(careerId: CareerId): List<TranscriptRow> {
        // Tolerate a failed refresh: Room may still hold rows from a previous sync.
        runCatching { transcriptRepository.refresh(careerId) }
        return (transcriptRepository.observeRows(careerId).first() as? Loadable.Loaded)?.value.orEmpty()
    }

    private suspend fun loadBadgeRecords(careerId: CareerId): List<EasyStaffAttendanceRecord> {
        val matricola = activeCareer(careerId)?.matricola ?: return emptyList()
        return runCatching {
            easyStaffApi.attendance.getAttendanceHistory(matricola).flatMap { it.records }
        }.getOrDefault(emptyList())
    }

    private suspend fun loadEnrolledCourses(): List<EnrolledCourse> {
        val account = sessionManager.activeAccount.value ?: return emptyList()
        runCatching { elearningCourseRepository.refreshEnrolledCourses(account.id, force = false) }
        return (elearningCourseRepository.observeEnrolledCourses(account.id).first() as? Loadable.Loaded)
            ?.value.orEmpty()
    }

    private fun matchBadgeRecord(
        records: List<EasyStaffAttendanceRecord>,
        seed: CourseSeed,
    ): ClassroomAttendance? = records
        .filter { record ->
            (seed.code != null && record.courseCode.equals(seed.code, ignoreCase = true)) ||
                normalizeSubjectName(record.courseName) == seed.normalizedName
        }
        .maxByOrNull { it.requirementProgressPercentage }
        ?.toDomain()

    private suspend fun loadSessionAttendance(
        enrolled: List<EnrolledCourse>,
        seed: CourseSeed,
        academicYearPrefix: String,
    ): List<SessionAttendance> {
        val code = seed.code ?: return emptyList()
        // Moodle course idnumbers follow "<aa>-<courseYear>-<esse3Code>[-T<turno>]"
        // (e.g. "2526-3-E3101Q123", "2425-2-E3101Q103-T1").
        val editions = enrolled.mapNotNull { course ->
            val idNumber = course.idNumber ?: return@mapNotNull null
            val match = COURSE_ID_NUMBER_REGEX.matchEntire(idNumber) ?: return@mapNotNull null
            val (yearPrefix, _, courseCode) = match.destructured
            if (yearPrefix != academicYearPrefix) return@mapNotNull null
            if (!courseCode.substringBefore('-').equals(code, ignoreCase = true)) return@mapNotNull null
            course
        }
        // Prefer the plain edition over per-turno splits.
        val edition = editions.minByOrNull { it.idNumber?.length ?: Int.MAX_VALUE } ?: return emptyList()
        return runCatching {
            val (api, token) = sessionManager.elearning()
            api.attendance.getAttendanceModules(token, edition.id.value).mapNotNull { module ->
                runCatching { api.attendance.getAttendanceSummary(token, edition.id.value, module.id) }
                    .getOrNull()
                    ?.toDomain(module.name)
            }
        }.getOrDefault(emptyList())
    }

    private fun currentYearOfStudy(
        rows: List<TranscriptRow>,
        academicYear: Int,
        plan: StudyPlan?,
    ): StudyYear {
        // Esse3 stamps each libretto row with the academic year it is frequented in;
        // the highest course year among this AA's rows is the current year of study.
        val fromTranscript = rows
            .filter { it.academicYear == academicYear }
            .maxOfOrNull { it.courseYear }
        if (fromTranscript != null && fromTranscript > 0) return StudyYear(fromTranscript)
        val fromPlan = plan?.courses?.maxOfOrNull { it.year.value }
        return fromPlan?.takeIf { it > 0 }?.let(::StudyYear) ?: StudyYear.Unknown
    }

    private fun activeCareer(careerId: CareerId): Career? {
        val account = sessionManager.activeAccount.value ?: return null
        return account.academic.careers.firstOrNull { it.id == careerId }
    }

    private companion object {
        val COURSE_ID_NUMBER_REGEX = Regex("^(\\d{4})-(\\d+)-(.+)$")

        fun academicYearOf(today: LocalDate): Int =
            if (today.monthValue >= 9) today.year else today.year - 1

        // Bicocca's first semester runs September to January, the second February to July.
        fun semesterOf(today: LocalDate): Semester =
            if (today.monthValue in 2..8) Semester.Second else Semester.First
    }
}
