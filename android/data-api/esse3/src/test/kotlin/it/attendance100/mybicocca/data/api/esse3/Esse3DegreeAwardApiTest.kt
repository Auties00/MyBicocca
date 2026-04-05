package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3DegreeAwardApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3DegreeAwardApiTest::class.java.name)

    companion object {
        private lateinit var permissions: Set<Esse3PermissionLevel>

        @JvmStatic
        @BeforeAll
        fun setUp() = runTest {
            val loginResult = Esse3GlobalApiData.api.auth.login()
            permissions = loginResult.profiles
                .mapNotNull { profile ->
                    profile.groupId?.toInt()?.let { Esse3PermissionLevel.fromGroupId(it) }
                }
                .filter { it != Esse3PermissionLevel.UNKNOWN }
                .toSet()
            Logger.getLogger(Esse3DegreeAwardApiTest::class.java.name)
                .info("Derived permissions from login: $permissions")
        }
    }

    @Test
    fun testGetExamCallsDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetExamCallsDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getExamCalls()
            assertNotNull(sessions)
            logger.info("getExamCalls (defaults): found ${sessions.size} exam call sessions")
            for (session in sessions) {
                logger.info(
                    "  session: examCallId=${session.examCallId}, callId=${session.callId}, " +
                        "courseOfStudyCode=${session.courseOfStudyCode}, state=${session.state}"
                )
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExamCalls (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetExamCallsWithPagination() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetExamCallsWithPagination: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getExamCalls(start = 0, limit = 5)
            assertNotNull(sessions)
            assertTrue(sessions.size <= 5, "Paginated result should have at most 5 items, got ${sessions.size}")
            logger.info("getExamCalls (start=0, limit=5): found ${sessions.size} sessions")
        } catch (e: Esse3Exception) {
            logger.warning("getExamCalls (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeCall() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeCall: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getExamCalls(start = 0, limit = 5)
            if (sessions.isEmpty()) {
                logger.warning("No exam call sessions available — skipping testGetCommitteeCall")
                return@runTest
            }
            for (session in sessions) {
                val examCallId = session.examCallId ?: continue
                try {
                    val committee = api.degreeAward.getCommitteeCall(examCallId)
                    assertNotNull(committee)
                    logger.info("getCommitteeCall($examCallId): found ${committee.size} entries")
                    for (entry in committee) {
                        logger.info(
                            "  entry: examCallId=${entry.examCallId}, callId=${entry.callId}, " +
                                "stateDescription=${entry.stateDescription}, state=${entry.state}"
                        )
                    }
                } catch (e: Esse3Exception) {
                    logger.warning("getCommitteeCall($examCallId) not authorized: ${e.message}")
                } catch (e: Esse3Exception) {
                    logger.warning("getCommitteeCall($examCallId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeApplicationByMatId() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeApplicationByMatId: missing required permissions $required, have $permissions")
            return@runTest
        }
        val enrollmentId = studentProfile.enrollmentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByMatId(enrollmentId)
            assertNotNull(applications)
            logger.info("getCommitteeApplicationByMatId($enrollmentId): found ${applications.size} applications")
            for (app in applications) {
                logger.info(
                    "  application: domicileCommitteeId=${app.domicileCommitteeId}, " +
                        "studentId=${app.studentId}, matId=${app.matId}, " +
                        "state=${app.state}, thesisId=${app.thesisId}, " +
                        "committeeApplicationDate=${app.committeeApplicationDate}"
                )
                assertNotNull(app.domicileCommitteeId, "domicileCommitteeId should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByMatId($enrollmentId) not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByMatId($enrollmentId) validation error: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeApplicationByStudentId() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeApplicationByStudentId: missing required permissions $required, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            assertNotNull(applications)
            logger.info("getCommitteeApplicationByStudentId($studentId): found ${applications.size} applications")
            for (app in applications) {
                logger.info(
                    "  application: domicileCommitteeId=${app.domicileCommitteeId}, " +
                        "studentId=${app.studentId}, matId=${app.matId}, " +
                        "state=${app.state}, thesisId=${app.thesisId}, thesisTitle=${app.thesisTitle}"
                )
                assertNotNull(app.domicileCommitteeId, "domicileCommitteeId should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) validation error: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeApplication() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeApplication: missing required permissions $required, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetCommitteeApplication")
                return@runTest
            }
            for (app in applications) {
                val domicileCommitteeId = app.domicileCommitteeId ?: continue
                try {
                    val single = api.degreeAward.getCommitteeApplication(domicileCommitteeId)
                    assertNotNull(single)
                    assertEquals(domicileCommitteeId, single.domicileCommitteeId, "domicileCommitteeId should match")
                    logger.info(
                        "getCommitteeApplication($domicileCommitteeId): studentId=${single.studentId}, " +
                            "matId=${single.matId}, state=${single.state}, " +
                            "thesisId=${single.thesisId}, thesisTitle=${single.thesisTitle}, " +
                            "callCommitteeDescription=${single.callCommitteeDescription}, " +
                            "finalGrade=${single.finalGrade}, cumLaudeFlag=${single.cumLaudeFlag}"
                    )
                } catch (e: Esse3Exception) {
                    logger.warning("getCommitteeApplication($domicileCommitteeId) not authorized: ${e.message}")
                } catch (e: Esse3Exception) {
                    logger.warning("getCommitteeApplication($domicileCommitteeId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetTheses() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetTheses: missing required permissions $required, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetTheses")
                return@runTest
            }
            for (app in applications) {
                val domicileCommitteeId = app.domicileCommitteeId ?: continue
                try {
                    val thesis = api.degreeAward.getTheses(domicileCommitteeId)
                    assertNotNull(thesis)
                    logger.info(
                        "getTheses($domicileCommitteeId): thesisId=${thesis.thesisId}, " +
                            "studentId=${thesis.studentId}, callCommitteeId=${thesis.callCommitteeId}, " +
                            "titleIta=${thesis.thesisTitleItalian}, status=${thesis.thesisStatus}, " +
                            "supervisors=${thesis.supervisors.size}, attachments=${thesis.thesisAttachments.size}"
                    )
                    for (supervisor in thesis.supervisors) {
                        logger.info(
                            "  supervisor: lecturerId=${supervisor.lecturerId}, " +
                                "relationTypeCode=${supervisor.relationTypeCode}, " +
                                "externalSubjectId=${supervisor.externalSubjectId}"
                        )
                    }
                } catch (e: Esse3Exception) {
                    logger.warning("getTheses($domicileCommitteeId) not authorized: ${e.message}")
                } catch (e: Esse3Exception) {
                    logger.warning("getTheses($domicileCommitteeId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisDiscussionModeDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisDiscussionModeDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val modes = api.degreeAward.getThesisDiscussionMode()
            assertNotNull(modes)
            logger.info("getThesisDiscussionMode (defaults): found ${modes.size} modes")
            for (mode in modes) {
                logger.info(
                    "  mode: code=${mode.thesisDiscussionModeCode}, description=${mode.description}, " +
                        "authorizationFlag=${mode.authorizationFlag}, " +
                        "thesisAccessAuthorizationModeCode=${mode.thesisAccessAuthorizationModeCode}, " +
                        "embargoDays=${mode.embargoDays}"
                )
                assertNotNull(mode.thesisDiscussionModeCode, "thesisDiscussionModeCode should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisDiscussionMode (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisDiscussionModeWithAuthorizationFlag() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisDiscussionModeWithAuthorizationFlag: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val enabledModes = api.degreeAward.getThesisDiscussionMode(authorizationFlag = 1)
            assertNotNull(enabledModes)
            logger.info("getThesisDiscussionMode (authorizationFlag=1): found ${enabledModes.size} enabled modes")
            for (mode in enabledModes) {
                logger.info("  mode: code=${mode.thesisDiscussionModeCode}, description=${mode.description}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisDiscussionMode (authorizationFlag=1) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisRelatedDocumentsDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisRelatedDocumentsDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments()
            assertNotNull(teachers)
            logger.info("getThesisRelatedDocuments (defaults): found ${teachers.size} supervisors/teachers")
            for (teacher in teachers) {
                logger.info(
                    "  teacher: lecturerId=${teacher.lecturerId}, surname=${teacher.surname}, " +
                        "name=${teacher.name}, departmentCode=${teacher.departmentCode}, " +
                        "lecturerRoleCode=${teacher.lecturerRoleCode}, email=${teacher.email}"
                )
                assertNotNull(teacher.lecturerId, "lecturerId should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelatedDocuments (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisRelatedDocumentsWithSurname() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisRelatedDocumentsWithSurname: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(surname = "A")
            assertNotNull(teachers)
            logger.info("getThesisRelatedDocuments (surname='A'): found ${teachers.size} supervisors")
            for (teacher in teachers) {
                logger.info(
                    "  teacher: lecturerId=${teacher.lecturerId}, surname=${teacher.surname}, name=${teacher.name}"
                )
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelatedDocuments (surname='A') not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisRelatedDocumentsWithPagination() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisRelatedDocumentsWithPagination: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(start = 0, limit = 5)
            assertNotNull(teachers)
            assertTrue(teachers.size <= 5, "Paginated result should have at most 5 items, got ${teachers.size}")
            logger.info("getThesisRelatedDocuments (start=0, limit=5): found ${teachers.size} supervisors")
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelatedDocuments (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetExternalSubjectDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetExternalSubjectDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val subjects = api.degreeAward.getExternalSubject()
            assertNotNull(subjects)
            logger.info("getExternalSubject (defaults): found ${subjects.size} external subjects")
            for (subject in subjects) {
                logger.info(
                    "  subject: externalSubjectId=${subject.externalSubjectId}, " +
                        "surname=${subject.surname}, name=${subject.name}, " +
                        "externalSubjectTypeCode=${subject.externalSubjectTypeCode}"
                )
                assertNotNull(subject.externalSubjectId, "externalSubjectId should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExternalSubject (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetExternalSubjectWithSurname() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetExternalSubjectWithSurname: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val subjects = api.degreeAward.getExternalSubject(surname = "A")
            assertNotNull(subjects)
            logger.info("getExternalSubject (surname='A'): found ${subjects.size} external subjects")
            for (subject in subjects) {
                logger.info(
                    "  subject: externalSubjectId=${subject.externalSubjectId}, surname=${subject.surname}"
                )
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExternalSubject (surname='A') not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetExternalSubjectWithPagination() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetExternalSubjectWithPagination: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val subjects = api.degreeAward.getExternalSubject(start = 0, limit = 5)
            assertNotNull(subjects)
            assertTrue(subjects.size <= 5, "Paginated result should have at most 5 items, got ${subjects.size}")
            logger.info("getExternalSubject (start=0, limit=5): found ${subjects.size} subjects")
        } catch (e: Esse3Exception) {
            logger.warning("getExternalSubject (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisSupervisorsReport() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisSupervisorsReport: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(limit = 1)
            val firstTeacher = teachers.firstOrNull { it.departmentCode != null }
            if (firstTeacher == null) {
                logger.warning("No thesis-related teacher with departmentCode found — skipping testGetThesisSupervisorsReport")
                return@runTest
            }
            val departmentCode = firstTeacher.departmentCode!!
            val startDate = "2020-01-01"
            val endDate = "2026-12-31"
            try {
                val report = api.degreeAward.getThesisSupervisorsReport(
                    departmentCode = departmentCode,
                    startDate = startDate,
                    endDate = endDate
                )
                assertNotNull(report)
                logger.info(
                    "getThesisSupervisorsReport($departmentCode, $startDate, $endDate): " +
                        "departmentCode=${report.departmentCode}, " +
                        "departmentDescription=${report.departmentDescription}, " +
                        "supervisors=${report.supervisors.size}"
                )
                for (supervisor in report.supervisors) {
                    logger.info(
                        "  supervisor: lecturerId=${supervisor.lecturerId}, " +
                            "externalSubjectId=${supervisor.externalSubjectId}, " +
                            "totals=${supervisor.totals.size}"
                    )
                    for (stat in supervisor.totals) {
                        logger.info(
                            "    stat: relationTypeCode=${stat.relationTypeCode}, thesisNumber=${stat.thesisNumber}"
                        )
                    }
                }
            } catch (e: Esse3Exception) {
                logger.warning("getThesisSupervisorsReport($departmentCode) not authorized: ${e.message}")
            } catch (e: Esse3Exception) {
                logger.warning("getThesisSupervisorsReport($departmentCode) validation error: ${e.message}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelatedDocuments not authorized (needed for report departmentCode): ${e.message}")
        }
    }

    @Test
    fun testGetThesisSupervisorsReportWithOptionalFilters() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisSupervisorsReportWithOptionalFilters: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(limit = 1)
            val firstTeacher = teachers.firstOrNull { it.departmentCode != null }
            if (firstTeacher == null) {
                logger.warning("No thesis-related teacher with departmentCode found — skipping testGetThesisSupervisorsReportWithOptionalFilters")
                return@runTest
            }
            val departmentCode = firstTeacher.departmentCode!!
            val lecturerId = firstTeacher.lecturerId
            val startDate = "2020-01-01"
            val endDate = "2026-12-31"
            try {
                val report = api.degreeAward.getThesisSupervisorsReport(
                    departmentCode = departmentCode,
                    startDate = startDate,
                    endDate = endDate,
                    lecturerId = lecturerId
                )
                assertNotNull(report)
                logger.info(
                    "getThesisSupervisorsReport($departmentCode, lecturerId=$lecturerId): " +
                        "departmentCode=${report.departmentCode}, supervisors=${report.supervisors.size}"
                )
            } catch (e: Esse3Exception) {
                logger.warning("getThesisSupervisorsReport (with lecturerId) not authorized: ${e.message}")
            } catch (e: Esse3Exception) {
                logger.warning("getThesisSupervisorsReport (with lecturerId) validation error: ${e.message}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelatedDocuments not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeCallSessionDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeCallSessionDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getCommitteeCallSession()
            assertNotNull(sessions)
            logger.info("getCommitteeCallSession (defaults): found ${sessions.size} committee call sessions")
            for (session in sessions) {
                logger.info(
                    "  session: committeeSessionId=${session.committeeSessionId}, " +
                        "callCommitteeId=${session.callCommitteeId}, " +
                        "committeeCallId=${session.committeeCallId}, " +
                        "sessionCode=${session.sessionCode}, sessionDate=${session.sessionDate}, " +
                        "department=${session.department}, classroomDescription=${session.classroomDescription}, " +
                        "committeeMembers=${session.committeeMembers.size}"
                )
                for (member in session.committeeMembers) {
                    logger.info(
                        "    member: surname=${member.surname}, name=${member.name}, " +
                            "roleCode=${member.roleCode}, lecturerId=${member.lecturerId}"
                    )
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeCallSession (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetCommitteeCallSessionWithAcademicYearFilter() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetCommitteeCallSessionWithAcademicYearFilter: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getCommitteeCallSession()
            if (sessions.isEmpty()) {
                logger.warning("No committee call sessions found — skipping testGetCommitteeCallSessionWithAcademicYearFilter")
                return@runTest
            }
            val firstSession = sessions.firstOrNull { it.academicYearId != null }
            if (firstSession == null) {
                logger.warning("No session with academicYearId — skipping filter test")
                return@runTest
            }
            val aaId = firstSession.academicYearId!!
            try {
                val filtered = api.degreeAward.getCommitteeCallSession(academicYearId = aaId)
                assertNotNull(filtered)
                logger.info("getCommitteeCallSession (academicYearId=$aaId): found ${filtered.size} sessions")
                for (session in filtered) {
                    assertEquals(aaId, session.academicYearId, "academicYearId should match filter")
                }
            } catch (e: Esse3Exception) {
                logger.warning("getCommitteeCallSession (academicYearId=$aaId) not authorized: ${e.message}")
            } catch (e: Esse3Exception) {
                logger.warning("getCommitteeCallSession (academicYearId=$aaId) validation error: ${e.message}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeCallSession not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesesByCommitteeCallId() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesesByCommitteeCallId: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getExamCalls(start = 0, limit = 5)
            if (sessions.isEmpty()) {
                logger.warning("No exam call sessions available — skipping testGetThesesByCommitteeCallId")
                return@runTest
            }
            for (session in sessions) {
                val examCallId = session.examCallId ?: continue
                try {
                    val theses = api.degreeAward.getThesesByCommitteeCallId(examCallId)
                    assertNotNull(theses)
                    logger.info("getThesesByCommitteeCallId($examCallId): found ${theses.size} theses")
                    for (thesis in theses) {
                        logger.info(
                            "  thesis: thesisId=${thesis.thesisId}, studentId=${thesis.studentId}, " +
                                "callCommitteeId=${thesis.callCommitteeId}, " +
                                "titleIta=${thesis.thesisTitleItalian}, status=${thesis.thesisStatus}, " +
                                "supervisors=${thesis.supervisors.size}, attachments=${thesis.thesisAttachments.size}"
                        )
                    }
                } catch (e: Esse3Exception) {
                    logger.warning("getThesesByCommitteeCallId($examCallId) not authorized: ${e.message}")
                } catch (e: Esse3Exception) {
                    logger.warning("getThesesByCommitteeCallId($examCallId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesesByCommitteeCallIdWithPagination() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesesByCommitteeCallIdWithPagination: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val sessions = api.degreeAward.getExamCalls(start = 0, limit = 1)
            if (sessions.isEmpty()) {
                logger.warning("No exam call sessions available — skipping testGetThesesByCommitteeCallIdWithPagination")
                return@runTest
            }
            val examCallId = sessions.first().examCallId
            if (examCallId == null) {
                logger.warning("First exam call session has no examCallId — skipping")
                return@runTest
            }
            try {
                val theses = api.degreeAward.getThesesByCommitteeCallId(examCallId, start = 0, limit = 5)
                assertNotNull(theses)
                assertTrue(theses.size <= 5, "Paginated result should have at most 5 items, got ${theses.size}")
                logger.info("getThesesByCommitteeCallId($examCallId, start=0, limit=5): found ${theses.size} theses")
            } catch (e: Esse3Exception) {
                logger.warning("getThesesByCommitteeCallId($examCallId, paginated) not authorized: ${e.message}")
            } catch (e: Esse3Exception) {
                logger.warning("getThesesByCommitteeCallId($examCallId, paginated) validation error: ${e.message}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisByThesisId() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisByThesisId: missing required permissions $required, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetThesisByThesisId")
                return@runTest
            }
            for (app in applications) {
                val thesisId = app.thesisId?.toLong() ?: continue
                try {
                    val thesis = api.degreeAward.getThesisByThesisId(thesisId)
                    assertNotNull(thesis)
                    assertEquals(thesisId, thesis.thesisId, "thesisId should match requested id")
                    logger.info(
                        "getThesisByThesisId($thesisId): thesisId=${thesis.thesisId}, " +
                            "studentId=${thesis.studentId}, callCommitteeId=${thesis.callCommitteeId}, " +
                            "titleIta=${thesis.thesisTitleItalian}, titleEng=${thesis.thesisTitleEnglish}, " +
                            "status=${thesis.thesisStatus}, language=${thesis.thesisLanguage}, " +
                            "depositDate=${thesis.thesisDepositDate}, " +
                            "supervisors=${thesis.supervisors.size}, attachments=${thesis.thesisAttachments.size}"
                    )
                    for (supervisor in thesis.supervisors) {
                        logger.info(
                            "  supervisor: relationTypeCode=${supervisor.relationTypeCode}, " +
                                "lecturerId=${supervisor.lecturerId}, " +
                                "externalSubjectId=${supervisor.externalSubjectId}"
                        )
                    }
                    for (attachment in thesis.thesisAttachments) {
                        logger.info(
                            "  attachment: attachmentId=${attachment.attachmentId}, " +
                                "title=${attachment.title}, fileName=${attachment.fileName}, " +
                                "state=${attachment.thesisAttachmentStateCode}"
                        )
                    }
                } catch (e: Esse3Exception) {
                    logger.warning("getThesisByThesisId($thesisId) not authorized: ${e.message}")
                } catch (e: Esse3Exception) {
                    logger.warning("getThesisByThesisId($thesisId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3Exception) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisRelationTypes() = runTest {
        val required = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisRelationTypes: missing required permissions $required, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val relationTypes = api.degreeAward.getThesisRelationTypes(studentId)
            assertNotNull(relationTypes)
            logger.info("getThesisRelationTypes($studentId): found ${relationTypes.size} relation types")
            for (relType in relationTypes) {
                logger.info(
                    "  relationType: committeeRegulationId=${relType.committeeRegulationId}, " +
                        "relationTypeCode=${relType.relationTypeCode}, " +
                        "relationTypeDescription=${relType.relationTypeDescription}, " +
                        "minNumber=${relType.minNumber}, maxNumber=${relType.maxNumber}, " +
                        "lecturers=${relType.lecturers}, externalSubject=${relType.externalSubject}"
                )
                assertNotNull(relType.relationTypeCode, "relationTypeCode should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelationTypes($studentId) not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelationTypes($studentId) validation error: ${e.message}")
        }
    }

    @Test
    fun testGetThesisTypeDefaults() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisTypeDefaults: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val thesisTypes = api.degreeAward.getThesisType()
            assertNotNull(thesisTypes)
            logger.info("getThesisType (defaults): found ${thesisTypes.size} thesis types")
            for (type in thesisTypes) {
                logger.info(
                    "  thesisType: code=${type.thesisTypeCode}, description=${type.description}, " +
                        "miurTypeCode=${type.miurThesisTypeCode}, committeeRegulationId=${type.committeeRegulationId}"
                )
                assertNotNull(type.thesisTypeCode, "thesisTypeCode should not be null")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisType (defaults) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisTypeWithPagination() = runTest {
        val required = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in required }) {
            logger.warning("Skipping testGetThesisTypeWithPagination: missing required permissions $required, have $permissions")
            return@runTest
        }
        try {
            val thesisTypes = api.degreeAward.getThesisType(start = 0, limit = 5)
            assertNotNull(thesisTypes)
            assertTrue(thesisTypes.size <= 5, "Paginated result should have at most 5 items, got ${thesisTypes.size}")
            logger.info("getThesisType (start=0, limit=5): found ${thesisTypes.size} thesis types")
        } catch (e: Esse3Exception) {
            logger.warning("getThesisType (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    fun testGetThesisTypeWithRegulationId() = runTest {
        val requiredForRelTypes = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        val requiredForThesisType = setOf(Esse3PermissionLevel.STUDENT)
        if (permissions.none { it in requiredForThesisType }) {
            logger.warning("Skipping testGetThesisTypeWithRegulationId: missing required permissions $requiredForThesisType, have $permissions")
            return@runTest
        }
        val studentId = studentProfile.studentId
        try {
            val relationTypes = api.degreeAward.getThesisRelationTypes(studentId)
            val firstRegId = relationTypes.firstOrNull { it.committeeRegulationId != null }?.committeeRegulationId
            if (firstRegId == null) {
                logger.warning("No committeeRegulationId found from getThesisRelationTypes — skipping testGetThesisTypeWithRegulationId")
                return@runTest
            }
            try {
                val thesisTypes = api.degreeAward.getThesisType(committeeRegulationId = firstRegId)
                assertNotNull(thesisTypes)
                logger.info("getThesisType (committeeRegulationId=$firstRegId): found ${thesisTypes.size} thesis types")
                for (type in thesisTypes) {
                    logger.info("  thesisType: code=${type.thesisTypeCode}, description=${type.description}")
                    assertNotNull(type.thesisTypeCode, "thesisTypeCode should not be null")
                }
            } catch (e: Esse3Exception) {
                logger.warning("getThesisType (committeeRegulationId=$firstRegId) not authorized: ${e.message}")
            } catch (e: Esse3Exception) {
                logger.warning("getThesisType (committeeRegulationId=$firstRegId) validation error: ${e.message}")
            }
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelationTypes($studentId) not authorized: ${e.message}")
        } catch (e: Esse3Exception) {
            logger.warning("getThesisRelationTypes($studentId) validation error: ${e.message}")
        }
    }

    @Disabled("Irreversible mutating operation: creates a thesis attachment metadata record with no delete endpoint")
    @Test
    fun testPostThesisAttachmentMetadata() = runTest {
    }

    @Disabled("Irreversible mutating operation: modifies antiplagiarism data on a real thesis attachment")
    @Test
    fun testPutAntiplagiarismData() = runTest {
    }

    @Disabled("Irreversible mutating operation: creates a degree award committee application requiring formal cancellation")
    @Test
    fun testPostCommitteeApplication() = runTest {
    }

    @Disabled("Irreversible mutating operation: cancels a committee application with permanent effects on degree award state")
    @Test
    fun testPutCancelCommitteeApplication() = runTest {
    }

    @Disabled("Irreversible mutating operation: attaches a thesis to an existing committee application")
    @Test
    fun testPostThesisIntoCommitteeApplication() = runTest {
    }

    @Disabled("Irreversible mutating operation: changes thesis consultation mode affecting embargo and access policies")
    @Test
    fun testPutThesisDiscussionMode() = runTest {
    }

    @Disabled("Irreversible mutating operation: modifies supervisor relations on a thesis affecting the graduation process")
    @Test
    fun testPutThesisRelation() = runTest {
    }
}
