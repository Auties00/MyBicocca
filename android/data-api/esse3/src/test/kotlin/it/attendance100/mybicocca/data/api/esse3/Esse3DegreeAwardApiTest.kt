package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3DegreeAwardApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3DegreeAwardApiTest::class.java.name)

    @Test
    suspend fun testGetExamCalls() {
        try {
            val sessions = api.degreeAward.getExamCalls()
            logger.info("getExamCalls: found ${sessions.size} exam call sessions")
            for (session in sessions) {
                logger.info(
                    "  session: examCallId=${session.examCallId}, callId=${session.callId}, " +
                        "callDescription=${session.callDescription}, courseOfStudyCode=${session.courseOfStudyCode}, " +
                        "callStartDate=${session.callStartDate}, state=${session.state}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExamCallsWithFilters() {
        try {
            val sessions = api.degreeAward.getExamCalls(start = 0, limit = 10)
            logger.info("getExamCalls (paginated): found ${sessions.size} sessions (max 10)")
            assertTrue(sessions.size <= 10, "Paginated result should have at most 10 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExamCalls (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeCall() {
        try {
            val sessions = api.degreeAward.getExamCalls()
            if (sessions.isEmpty()) {
                logger.warning("No exam call sessions available — skipping testGetCommitteeCall")
                return
            }
            for (session in sessions) {
                val examCallId = session.examCallId ?: continue
                try {
                    val committee = api.degreeAward.getCommitteeCall(examCallId)
                    logger.info(
                        "getCommitteeCall($examCallId): found ${committee.size} entries"
                    )
                    for (entry in committee) {
                        logger.info(
                            "  entry: examCallId=${entry.examCallId}, callId=${entry.callId}, " +
                                "callDescription=${entry.callDescription}, state=${entry.state}"
                        )
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCommitteeCall($examCallId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCommitteeCall($examCallId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesesByCommitteeCallId() {
        try {
            val sessions = api.degreeAward.getExamCalls()
            if (sessions.isEmpty()) {
                logger.warning("No exam call sessions available — skipping testGetThesesByCommitteeCallId")
                return
            }
            for (session in sessions) {
                val examCallId = session.examCallId ?: continue
                try {
                    val theses = api.degreeAward.getThesesByCommitteeCallId(examCallId)
                    logger.info("getThesesByCommitteeCallId($examCallId): found ${theses.size} theses")
                    for (thesis in theses) {
                        logger.info(
                            "  thesis: thesisId=${thesis.thesisId}, studentId=${thesis.studentId}, " +
                                "callCommitteeId=${thesis.callCommitteeId}, " +
                                "titleIta=${thesis.thesisTitleItalian}, status=${thesis.thesisStatus}, " +
                                "supervisors=${thesis.supervisors.size}, attachments=${thesis.thesisAttachments.size}"
                        )
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getThesesByCommitteeCallId($examCallId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getThesesByCommitteeCallId($examCallId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExamCalls not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeApplicationByMatId() {
        val enrollmentId = studentProfile.enrollmentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByMatId(enrollmentId)
            logger.info("getCommitteeApplicationByMatId($enrollmentId): found ${applications.size} applications")
            for (app in applications) {
                logger.info(
                    "  application: domicileCommitteeId=${app.domicileCommitteeId}, " +
                        "studentId=${app.studentId}, matId=${app.matId}, " +
                        "state=${app.state}, thesisId=${app.thesisId}, thesisTitle=${app.thesisTitle}, " +
                        "committeeApplicationDate=${app.committeeApplicationDate}"
                )
                assertNotNull(app.domicileCommitteeId, "domicileCommitteeId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeApplicationByMatId($enrollmentId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCommitteeApplicationByMatId($enrollmentId) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeApplicationByStudentId() {
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            logger.info("getCommitteeApplicationByStudentId($studentId): found ${applications.size} applications")
            for (app in applications) {
                logger.info(
                    "  application: domicileCommitteeId=${app.domicileCommitteeId}, " +
                        "studentId=${app.studentId}, matId=${app.matId}, " +
                        "state=${app.state}, thesisId=${app.thesisId}, thesisTitle=${app.thesisTitle}, " +
                        "committeeApplicationDate=${app.committeeApplicationDate}"
                )
                assertNotNull(app.domicileCommitteeId, "domicileCommitteeId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeApplication() {
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetCommitteeApplication")
                return
            }
            for (app in applications) {
                val domicileCommitteeId = app.domicileCommitteeId ?: continue
                try {
                    val single = api.degreeAward.getCommitteeApplication(domicileCommitteeId)
                    logger.info(
                        "getCommitteeApplication($domicileCommitteeId): studentId=${single.studentId}, " +
                            "matId=${single.matId}, state=${single.state}, " +
                            "thesisId=${single.thesisId}, thesisTitle=${single.thesisTitle}, " +
                            "committeeCallDescription=${single.callCommitteeDescription}, " +
                            "finalGrade=${single.finalGrade}, cumLaudeFlag=${single.cumLaudeFlag}"
                    )
                    assertEquals(domicileCommitteeId, single.domicileCommitteeId, "domicileCommitteeId should match")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCommitteeApplication($domicileCommitteeId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCommitteeApplication($domicileCommitteeId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTheses() {
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetTheses")
                return
            }
            for (app in applications) {
                val domicileCommitteeId = app.domicileCommitteeId ?: continue
                try {
                    val thesis = api.degreeAward.getTheses(domicileCommitteeId)
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
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTheses($domicileCommitteeId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTheses($domicileCommitteeId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisDiscussionMode() {
        try {
            val modes = api.degreeAward.getThesisDiscussionMode()
            logger.info("getThesisDiscussionMode: found ${modes.size} modes")
            for (mode in modes) {
                logger.info(
                    "  mode: code=${mode.thesisDiscussionModeCode}, description=${mode.description}, " +
                        "authorizationFlag=${mode.authorizationFlag}, " +
                        "thesisAccessAuthorizationModeCode=${mode.thesisAccessAuthorizationModeCode}, " +
                        "embargoDays=${mode.embargoDays}"
                )
                assertNotNull(mode.thesisDiscussionModeCode, "thesisDiscussionModeCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisDiscussionMode not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisDiscussionModeWithFilter() {
        try {
            val enabledModes = api.degreeAward.getThesisDiscussionMode(authorizationFlag = 1)
            logger.info("getThesisDiscussionMode (authorizationFlag=1): found ${enabledModes.size} enabled modes")
            for (mode in enabledModes) {
                logger.info("  mode: code=${mode.thesisDiscussionModeCode}, description=${mode.description}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisDiscussionMode (authorizationFlag=1) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisRelatedDocuments() {
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments()
            logger.info("getThesisRelatedDocuments: found ${teachers.size} thesis-related supervisors/teachers")
            for (teacher in teachers) {
                logger.info(
                    "  teacher: lecturerId=${teacher.lecturerId}, surname=${teacher.surname}, " +
                        "name=${teacher.name}, departmentCode=${teacher.departmentCode}, " +
                        "lecturerRoleCode=${teacher.lecturerRoleCode}"
                )
                assertNotNull(teacher.lecturerId, "lecturerId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelatedDocuments not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisRelatedDocumentsWithSurname() {
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(surname = "A")
            logger.info("getThesisRelatedDocuments (surname='A'): found ${teachers.size} supervisors")
            for (teacher in teachers) {
                logger.info("  teacher: lecturerId=${teacher.lecturerId}, surname=${teacher.surname}, name=${teacher.name}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelatedDocuments (surname='A') not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisRelatedDocumentsWithPagination() {
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(start = 0, limit = 10)
            logger.info("getThesisRelatedDocuments (paginated): found ${teachers.size} supervisors (max 10)")
            assertTrue(teachers.size <= 10, "Paginated result should have at most 10 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelatedDocuments (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExternalSubject() {
        try {
            val subjects = api.degreeAward.getExternalSubject()
            logger.info("getExternalSubject: found ${subjects.size} external subjects")
            for (subject in subjects) {
                logger.info(
                    "  subject: externalSubjectId=${subject.externalSubjectId}, " +
                        "surname=${subject.surname}, name=${subject.name}, " +
                        "email=${subject.email}, externalSubjectTypeCode=${subject.externalSubjectTypeCode}"
                )
                assertNotNull(subject.externalSubjectId, "externalSubjectId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExternalSubject not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExternalSubjectWithSurname() {
        try {
            val subjects = api.degreeAward.getExternalSubject(surname = "A")
            logger.info("getExternalSubject (surname='A'): found ${subjects.size} external subjects")
            for (subject in subjects) {
                logger.info("  subject: externalSubjectId=${subject.externalSubjectId}, surname=${subject.surname}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExternalSubject (surname='A') not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetExternalSubjectWithPagination() {
        try {
            val subjects = api.degreeAward.getExternalSubject(start = 0, limit = 5)
            logger.info("getExternalSubject (paginated): found ${subjects.size} subjects (max 5)")
            assertTrue(subjects.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getExternalSubject (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisSupervisorsReport() {
        try {
            val teachers = api.degreeAward.getThesisRelatedDocuments(limit = 1)
            val firstTeacher = teachers.firstOrNull { it.departmentCode != null }
            if (firstTeacher == null) {
                logger.warning("No thesis-related teacher with departmentCode found — skipping testGetThesisSupervisorsReport")
                return
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
                            "    stat: relationTypeCode=${stat.relationTypeCode}, " +
                                "thesisNumber=${stat.thesisNumber}"
                        )
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getThesisSupervisorsReport($departmentCode) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getThesisSupervisorsReport($departmentCode) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelatedDocuments not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeCallSession() {
        try {
            val sessions = api.degreeAward.getCommitteeCallSession()
            logger.info("getCommitteeCallSession: found ${sessions.size} committee call sessions")
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
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeCallSession not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCommitteeCallSessionWithFilters() {
        try {
            val sessions = api.degreeAward.getCommitteeCallSession()
            if (sessions.isEmpty()) {
                logger.warning("No committee call sessions found — skipping testGetCommitteeCallSessionWithFilters")
                return
            }
            val firstSession = sessions.firstOrNull { it.academicYearId != null }
            if (firstSession == null) {
                logger.warning("No session with academicYearId — skipping filter test")
                return
            }
            val aaId = firstSession.academicYearId!!
            try {
                val filtered = api.degreeAward.getCommitteeCallSession(academicYearId = aaId)
                logger.info("getCommitteeCallSession (academicYearId=$aaId): found ${filtered.size} sessions")
                for (session in filtered) {
                    assertEquals(aaId, session.academicYearId, "academicYearId should match filter")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCommitteeCallSession (academicYearId=$aaId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCommitteeCallSession (academicYearId=$aaId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeCallSession not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisByThesisId() {
        val studentId = studentProfile.studentId
        try {
            val applications = api.degreeAward.getCommitteeApplicationByStudentId(studentId)
            if (applications.isEmpty()) {
                logger.warning("No committee applications found for student $studentId — skipping testGetThesisByThesisId")
                return
            }
            for (app in applications) {
                val thesisId = app.thesisId?.toLong() ?: continue
                try {
                    val thesis = api.degreeAward.getThesisByThesisId(thesisId)
                    logger.info(
                        "getThesisByThesisId($thesisId): thesisId=${thesis.thesisId}, " +
                            "studentId=${thesis.studentId}, callCommitteeId=${thesis.callCommitteeId}, " +
                            "titleIta=${thesis.thesisTitleItalian}, titleEng=${thesis.thesisTitleEnglish}, " +
                            "status=${thesis.thesisStatus}, language=${thesis.thesisLanguage}, " +
                            "depositDate=${thesis.thesisDepositDate}, " +
                            "supervisors=${thesis.supervisors.size}, attachments=${thesis.thesisAttachments.size}"
                    )
                    assertEquals(thesisId, thesis.thesisId, "thesisId should match requested id")
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
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getThesisByThesisId($thesisId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getThesisByThesisId($thesisId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCommitteeApplicationByStudentId($studentId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisRelationTypes() {
        val studentId = studentProfile.studentId
        try {
            val relationTypes = api.degreeAward.getThesisRelationTypes(studentId)
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
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelationTypes($studentId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getThesisRelationTypes($studentId) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisType() {
        try {
            val thesisTypes = api.degreeAward.getThesisType()
            logger.info("getThesisType: found ${thesisTypes.size} thesis types")
            for (type in thesisTypes) {
                logger.info(
                    "  thesisType: code=${type.thesisTypeCode}, description=${type.description}, " +
                        "miurTypeCode=${type.miurThesisTypeCode}, committeeRegulationId=${type.committeeRegulationId}"
                )
                assertNotNull(type.thesisTypeCode, "thesisTypeCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisType not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetThesisTypeWithRegulationId() {
        val studentId = studentProfile.studentId
        try {
            val relationTypes = api.degreeAward.getThesisRelationTypes(studentId)
            val firstRegId = relationTypes.firstOrNull { it.committeeRegulationId != null }?.committeeRegulationId
            if (firstRegId == null) {
                logger.warning("No committeeRegulationId found from getThesisRelationTypes — skipping testGetThesisTypeWithRegulationId")
                return
            }
            try {
                val thesisTypes = api.degreeAward.getThesisType(committeeRegulationId = firstRegId)
                logger.info("getThesisType (committeeRegulationId=$firstRegId): found ${thesisTypes.size} thesis types")
                for (type in thesisTypes) {
                    logger.info("  thesisType: code=${type.thesisTypeCode}, description=${type.description}")
                    assertNotNull(type.thesisTypeCode, "thesisTypeCode should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getThesisType (committeeRegulationId=$firstRegId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getThesisType (committeeRegulationId=$firstRegId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getThesisRelationTypes($studentId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getThesisRelationTypes($studentId) validation error: ${e.message}")
        }
    }

    @Disabled("Destructive: postThesisAttachmentMetadata creates a thesis attachment metadata record and cannot be safely reversed")
    @Test
    suspend fun testPostThesisAttachmentMetadata() {
        // Disabled: creates a thesis attachment record in the system.
        // No corresponding delete endpoint is available for cleanup.
    }

    @Disabled("Destructive: putAntiplagiarismData modifies antiplagiarism data on a real thesis attachment record")
    @Test
    suspend fun testPutAntiplagiarismData() {
        // Disabled: permanently modifies antiplagiarism index, link, and notes on a real attachment.
    }

    @Disabled("Destructive: postCommitteeApplication creates a degree award committee application for a student and cannot be easily reversed")
    @Test
    suspend fun testPostCommitteeApplication() {
        // Disabled: creates a committee application in the system which requires formal cancellation.
    }

    @Disabled("Destructive: putCancelCommitteeApplication cancels a committee application and may have irreversible effects on degree award state")
    @Test
    suspend fun testPutCancelCommitteeApplication() {
        // Disabled: cancellation of an application changes the system state permanently.
    }

    @Disabled("Destructive: postThesisIntoCommitteeApplication creates a thesis linked to an existing committee application")
    @Test
    suspend fun testPostThesisIntoCommitteeApplication() {
        // Disabled: modifies an existing committee application by attaching a thesis.
    }

    @Disabled("Destructive: putThesisDiscussionMode changes the thesis consultation mode on a real thesis record")
    @Test
    suspend fun testPutThesisDiscussionMode() {
        // Disabled: modifies thesis access mode which may affect embargo and consultation policies.
    }

    @Disabled("Destructive: putThesisRelation modifies the supervisors linked to a thesis and cannot be safely reversed without knowing the original state")
    @Test
    suspend fun testPutThesisRelation() {
        // Disabled: modifying supervisor relations on a real thesis may affect the graduation process.
    }
}
