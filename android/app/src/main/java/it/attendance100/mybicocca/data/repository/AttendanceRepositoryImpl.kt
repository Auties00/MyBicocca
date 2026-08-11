package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.settings.DeviceIdentityStore
import it.attendance100.mybicocca.data.location.DeviceLocationProvider
import it.attendance100.mybicocca.data.mapper.attendance.toDomain
import it.attendance100.mybicocca.data.mapper.attendance.toOutcome
import it.attendance100.mybicocca.data.mapper.calendar.normalizeSubjectName
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceRecord
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
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

/**
 * Network-only attendance implementation fanning out to three backends: the Esse3 study plan
 * and transcript (which courses are pending), the EasyStaff EasyBadge attendance endpoints
 * (classroom certifications, plus the no-auth GPS-based certify write), and Moodle
 * mod_attendance read through the tool_mobile_get_content bridge (session registers, open
 * sessions, and the self-mark write via the mobile_view_activity bridge — the dedicated
 * mod_attendance_* web services are blocked server-side).
 */
@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val easyStaffApi: EasyStaffApi,
    private val studyPlanRepository: StudyPlanRepository,
    private val transcriptRepository: TranscriptRepository,
    private val elearningCourseRepository: ElearningCourseRepository,
    private val deviceIdentityStore: DeviceIdentityStore,
    private val locationProvider: DeviceLocationProvider,
) : AttendanceRepository {

    /**
     * Attendance modules discovered on the last [getPendingCourses] pass, reused to resolve
     * which course a scanned QR's session belongs to (the QR carries only sessid).
     */
    @Volatile
    private var cachedModules: List<AttendanceModuleRef> = emptyList()

    /**
     * Builds the pending-course list by joining all sources concurrently. The plan is the
     * page's backbone: a failure there surfaces as a sync error, while the enrichment sources
     * (badge, elearning) degrade to "no data" instead. Only the current academic year's course
     * editions carry a live session register, so Moodle editions are filtered by the year
     * prefix of their idnumber.
     */
    override suspend fun getPendingCourses(careerId: CareerId): List<CourseAttendance> = coroutineScope {
        val planDeferred = async { studyPlanRepository.getStudyPlan(careerId) }
        val plannedDeferred = async {
            runCatching { studyPlanRepository.getPlannedCoursesForActiveCareer(careerId) }
                .getOrDefault(emptyList())
        }
        val transcriptDeferred = async { loadTranscriptRows(careerId) }
        val badgeDeferred = async { loadBadgeRecords(careerId) }
        val enrolledDeferred = async { loadEnrolledCourses() }

        val plan = planDeferred.await()
        val planned = plannedDeferred.await()
        val transcriptRows = transcriptDeferred.await()

        val today = LocalDate.now()
        val academicYear = academicYearOf(today)
        val currentSemester = semesterOf(today)
        val currentYear = currentYearOfStudy(transcriptRows, academicYear, plan)

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
        val academicYearPrefix = "%02d%02d".format(academicYear % 100, (academicYear + 1) % 100)

        val courses = seeds
            .map { seed ->
                async {
                    val edition = loadEditionAttendance(enrolled, seed, academicYearPrefix)
                    CourseAttendance(
                        name = seed.name,
                        code = seed.code,
                        year = seed.year,
                        semester = seed.semester,
                        credits = seed.credits,
                        teacherName = seed.teacherName,
                        classroomAttendance = matchBadgeRecord(badgeRecords, seed),
                        sessionAttendance = edition.sessions,
                        attendanceModules = edition.modules,
                    )
                }
            }
            .awaitAll()
            .sortedWith(compareBy({ it.year.value }, { it.semester.ordinal }, { it.name }))

        cachedModules = courses.flatMap { it.attendanceModules }.distinctBy { it.courseModuleId }
        courses
    }

    /**
     * One pending-course candidate before attendance enrichment. The plan is the primary
     * source; the EasyStaff-planned list fills in semester and teacher.
     */
    private data class CourseSeed(
        val name: String,
        val code: String?,
        val normalizedName: String,
        val credits: Float,
        val year: StudyYear,
        val semester: Semester,
        val teacherName: String?,
    )

    /**
     * Seeds the candidate list from the Esse3 personal plan; when no plan exists on Esse3 the
     * EasyStaff-intersected planned list takes its place.
     */
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
        return planned.map { seedOf(it.name, null, it.cfu?.toFloat() ?: 0f, it.studyYear) }
    }

    /**
     * "Up to the current semester": everything from earlier years, plus the current year's
     * courses excluding second-semester ones while the first is ongoing. Unknowns lean towards
     * inclusion — hiding a real course is worse than showing one a semester early.
     */
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

    /** Tolerates a failed refresh: Room may still hold rows from a previous sync. */
    private suspend fun loadTranscriptRows(careerId: CareerId): List<TranscriptRow> {
        runCatching { transcriptRepository.refresh(careerId) }
        return (transcriptRepository.observeRows(careerId).first() as? Loadable.Loaded)?.value.orEmpty()
    }

    private suspend fun loadBadgeRecords(careerId: CareerId): List<EasyStaffAttendanceRecord> {
        val studentNumber = activeCareer(careerId)?.studentNumber ?: return emptyList()
        return runCatching {
            easyStaffApi.attendance.getAttendanceHistory(studentNumber).flatMap { it.records }
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

    private data class EditionAttendance(
        val modules: List<AttendanceModuleRef>,
        val sessions: List<SessionAttendance>,
    )

    /**
     * Resolves the Moodle edition of a seed course and reads its attendance registers. Moodle
     * course idnumbers follow "aa-courseYear-esse3Code" with an optional "-T turno" suffix
     * (e.g. "2526-3-E3101Q123", "2425-2-E3101Q103-T1"); the join matches the year prefix and
     * the Esse3 activity code, preferring the plain edition over per-turno splits.
     */
    private suspend fun loadEditionAttendance(
        enrolled: List<EnrolledCourse>,
        seed: CourseSeed,
        academicYearPrefix: String,
    ): EditionAttendance {
        val code = seed.code ?: return EditionAttendance(emptyList(), emptyList())
        val editions = enrolled.mapNotNull { course ->
            val idNumber = course.idNumber ?: return@mapNotNull null
            val match = COURSE_ID_NUMBER_REGEX.matchEntire(idNumber) ?: return@mapNotNull null
            val (yearPrefix, _, courseCode) = match.destructured
            if (yearPrefix != academicYearPrefix) return@mapNotNull null
            if (!courseCode.substringBefore('-').equals(code, ignoreCase = true)) return@mapNotNull null
            course
        }
        val edition = editions.minByOrNull { it.idNumber?.length ?: Int.MAX_VALUE }
            ?: return EditionAttendance(emptyList(), emptyList())
        return runCatching {
            val (api, token) = sessionManager.elearning()
            val modules = api.attendance.getAttendanceModules(token, edition.id.value)
            val refs = modules.map { AttendanceModuleRef(edition.id.value, it.id, it.name) }
            val sessions = modules.mapNotNull { module ->
                runCatching { api.attendance.getAttendanceSummary(token, edition.id.value, module.id) }
                    .getOrNull()
                    ?.toDomain(module.name)
            }
            EditionAttendance(refs, sessions)
        }.getOrDefault(EditionAttendance(emptyList(), emptyList()))
    }

    override suspend fun getOpenSessions(
        modules: List<AttendanceModuleRef>,
    ): List<OpenAttendanceSession> {
        if (modules.isEmpty()) return emptyList()
        val (api, token) = sessionManager.elearning()
        return modules.flatMap { module ->
            runCatching {
                api.attendance.getMarkableSessions(token, module.courseId, module.courseModuleId)
                    .map { it.toDomain(module) }
            }.getOrDefault(emptyList())
        }
    }

    override suspend fun markSession(
        module: AttendanceModuleRef,
        sessionId: String,
        statusId: String?,
        password: String?,
    ): PresenceMarkOutcome = runCatching {
        val (api, token) = sessionManager.elearning()
        api.attendance.markSession(
            wsToken = token,
            courseId = module.courseId,
            moduleId = module.courseModuleId,
            sessionId = sessionId,
            statusId = statusId ?: DEFAULT_STATUS_ID,
            studentPass = password,
        ).toOutcome()
    }.getOrElse { PresenceMarkOutcome.Failed() }

    override suspend fun registerPresence(
        scan: PresenceScan,
        careerId: CareerId,
    ): PresenceMarkOutcome = when (scan) {
        is PresenceScan.SessionLink -> registerSessionLink(scan.sessionId, scan.password)
        is PresenceScan.LessonCode -> registerLessonCode(careerId, scan.code)
        is PresenceScan.Unrecognized ->
            PresenceMarkOutcome.Failed(message = UiText.StringResource(R.string.attendance_msg_code_unrecognized))
    }

    /**
     * A scanned attendance session carries only its id, so the marking resolves which of the
     * student's attendance modules owns it (by listing each one's open sessions) first.
     */
    private suspend fun registerSessionLink(
        sessionId: String,
        password: String?,
    ): PresenceMarkOutcome {
        val modules = resolveModules()
        if (modules.isEmpty()) {
            return PresenceMarkOutcome.NotOpen(message = UiText.StringResource(R.string.attendance_msg_no_register_found))
        }
        val target = runCatching { getOpenSessions(modules) }.getOrDefault(emptyList())
            .firstOrNull { it.sessionId == sessionId }
            ?: return PresenceMarkOutcome.NotOpen(message = UiText.StringResource(R.string.attendance_msg_session_not_in_courses))
        return markSession(target.module, sessionId, target.statuses.firstOrNull()?.id, password)
    }

    /**
     * Certifies through the no-auth EasyBadge endpoint, identified by matricola plus a stable
     * device token and the last known GPS position (0/0 when unavailable).
     */
    private suspend fun registerLessonCode(
        careerId: CareerId,
        lessonCode: String,
    ): PresenceMarkOutcome {
        val studentNumber = activeCareer(careerId)?.studentNumber
            ?: return PresenceMarkOutcome.Failed(message = UiText.StringResource(R.string.attendance_msg_student_number_unavailable))
        val deviceId = deviceIdentityStore.deviceId()
        val location = locationProvider.lastKnownLatLong()
        return runCatching {
            easyStaffApi.attendance.certifyAttendance(
                studentId = studentNumber,
                lessonCode = lessonCode,
                deviceToken = deviceId,
                longitude = location?.second ?: 0.0,
                latitude = location?.first ?: 0.0,
            ).toOutcome()
        }.getOrElse { PresenceMarkOutcome.Failed() }
    }

    private suspend fun resolveModules(): List<AttendanceModuleRef> {
        cachedModules.takeIf { it.isNotEmpty() }?.let { return it }
        val careerId = sessionManager.activeAccount.value?.academic?.selectedCareerId ?: return emptyList()
        runCatching { getPendingCourses(careerId) }
        return cachedModules
    }

    /**
     * Esse3 stamps each libretto row with the academic year it is frequented in; the highest
     * course year among the current academic year's rows is the current year of study, with
     * the plan's deepest year as fallback.
     */
    private fun currentYearOfStudy(
        rows: List<TranscriptRow>,
        academicYear: Int,
        plan: StudyPlan?,
    ): StudyYear {
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

        /**
         * mod_attendance overrides this to the highest status server-side for auto-assign
         * sessions, so any non-empty value works as a fallback.
         */
        const val DEFAULT_STATUS_ID = "1"

        fun academicYearOf(today: LocalDate): Int =
            if (today.monthValue >= 9) today.year else today.year - 1

        /** Bicocca's first semester runs September to January, the second February to July. */
        fun semesterOf(today: LocalDate): Semester =
            if (today.monthValue in 2..8) Semester.Second else Semester.First
    }
}
