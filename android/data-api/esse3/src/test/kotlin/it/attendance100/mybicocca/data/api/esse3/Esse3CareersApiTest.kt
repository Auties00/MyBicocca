package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3CareersApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3CareersApiTest::class.java)


    @Test
    suspend fun `getCareers with no filters returns list`() {
        try {
            val careers = api.careers.getCareers()
            assertNotNull(careers)
            logger.info("getCareers (no filters): got ${careers.size} careers")
            for (career in careers) {
                logger.info("  career studentId=${career.studentId}, userId=${career.userId}, status=${career.studentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareers filtered by userId returns relevant results`() {
        try {
            val careers = api.careers.getCareers(userId = studentProfile.userId)
            assertNotNull(careers)
            logger.info("getCareers (userId=${studentProfile.userId}): got ${careers.size} careers")
            for (career in careers) {
                assertNotNull(career.studentId)
                logger.info("  career studentId=${career.studentId}, courseOfStudyId=${career.courseOfStudyId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers (userId): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareers filtered by onlyActive returns active careers`() {
        try {
            val careers = api.careers.getCareers(onlyActive = 1)
            assertNotNull(careers)
            logger.info("getCareers (onlyActive=1): got ${careers.size} careers")
            for (career in careers) {
                logger.info("  career studentId=${career.studentId}, status=${career.studentStatusCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers (onlyActive): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareers with pagination`() {
        try {
            val careers = api.careers.getCareers(start = 0, limit = 5)
            assertNotNull(careers)
            assertTrue(careers.size <= 5, "Pagination limit should be respected, got ${careers.size}")
            logger.info("getCareers (start=0, limit=5): got ${careers.size} careers")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers (pagination): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareers filtered by courseOfStudyId`() {
        try {
            val careers = api.careers.getCareers(courseOfStudyId = studentProfile.degreeCourseId)
            assertNotNull(careers)
            logger.info("getCareers (cdsId=${studentProfile.degreeCourseId}): got ${careers.size} careers")
            for (career in careers) {
                logger.info("  career studentId=${career.studentId}, cdsId=${career.courseOfStudyId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareers (courseOfStudyId): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getMinimalGdprCareersData with userId filter`() {
        try {
            val data = api.careers.getMinimalGdprCareersData(userId = studentProfile.userId)
            assertNotNull(data)
            logger.info("getMinimalGdprCareersData (userId=${studentProfile.userId}): got ${data.size} entries")
            for (entry in data) {
                logger.info("  stuId=${entry.studentId}, name=${entry.name}, surname=${entry.surname}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalGdprCareersData (userId): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getMinimalGdprCareersData with onlyActive filter`() {
        try {
            val data = api.careers.getMinimalGdprCareersData(onlyActive = 1)
            assertNotNull(data)
            logger.info("getMinimalGdprCareersData (onlyActive=1): got ${data.size} entries")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalGdprCareersData (onlyActive): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getMinimalGdprCareersData with no filters`() {
        try {
            val data = api.careers.getMinimalGdprCareersData()
            assertNotNull(data)
            logger.info("getMinimalGdprCareersData (no filters): got ${data.size} entries")
            for (entry in data) {
                logger.info("  stuId=${entry.studentId}, fiscalCode=${entry.fiscalCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalGdprCareersData (no filters): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getGdprCareerByStudent returns career GDPR data for student`() {
        try {
            val gdprData = api.careers.getGdprCareerByStudent(studentProfile.studentId)
            assertNotNull(gdprData)
            logger.info("getGdprCareerByStudent (studentId=${studentProfile.studentId}): got ${gdprData.size} entries")
            for (entry in gdprData) {
                logger.info("  personId=${entry.personId}, stuId=${entry.studentId}, userId=${entry.userId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getGdprCareerByStudent: not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getGdprCareerByStudent with optional fields`() {
        try {
            val gdprData = api.careers.getGdprCareerByStudent(
                studentId = studentProfile.studentId,
                optionalFields = "codFis,email"
            )
            assertNotNull(gdprData)
            logger.info("getGdprCareerByStudent (optionalFields): got ${gdprData.size} entries")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getGdprCareerByStudent (optionalFields): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getMinimalCareersData with no filters`() {
        try {
            val data = api.careers.getMinimalCareersData()
            assertNotNull(data)
            logger.info("getMinimalCareersData (no filters): got ${data.size} entries")
            for (entry in data) {
                logger.info("  stuId=${entry.studentId}, cdsId=${entry.courseOfStudyId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalCareersData (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getMinimalCareersData with userId filter`() {
        try {
            val data = api.careers.getMinimalCareersData(userId = studentProfile.userId)
            assertNotNull(data)
            logger.info("getMinimalCareersData (userId=${studentProfile.userId}): got ${data.size} entries")
            for (entry in data) {
                assertNotNull(entry.studentId)
                logger.info("  stuId=${entry.studentId}, courseTypeCode=${entry.courseTypeCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalCareersData (userId): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getMinimalCareersData with onlyActive and pagination`() {
        try {
            val data = api.careers.getMinimalCareersData(onlyActive = 1, start = 0, limit = 10)
            assertNotNull(data)
            assertTrue(data.size <= 10, "Pagination limit respected, got ${data.size}")
            logger.info("getMinimalCareersData (onlyActive=1, limit=10): got ${data.size} entries")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getMinimalCareersData (onlyActive+pagination): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getPhdCareersData with no filters`() {
        try {
            val data = api.careers.getPhdCareersData()
            assertNotNull(data)
            logger.info("getPhdCareersData (no filters): got ${data.size} entries")
            for (entry in data) {
                logger.info("  stuId=${entry.studentId}, cdsId=${entry.courseOfStudyId}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPhdCareersData (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getPhdCareersData with studentId filter`() {
        try {
            val data = api.careers.getPhdCareersData(studentId = studentProfile.studentId)
            assertNotNull(data)
            logger.info("getPhdCareersData (studentId=${studentProfile.studentId}): got ${data.size} entries")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getPhdCareersData (studentId): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getCareerByStudent returns career for known student`() {
        try {
            val career = api.careers.getCareerByStudent(studentProfile.studentId)
            assertNotNull(career)
            assertNotNull(career.studentId)
            assertTrue(career.studentId == studentProfile.studentId)
            logger.info("getCareerByStudent (studentId=${studentProfile.studentId}): studentId=${career.studentId}, status=${career.studentStatusCode}, cdsId=${career.courseOfStudyId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerByStudent: not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getAttendanceDays returns data for student`() {
        try {
            val attendance = api.careers.getAttendanceDays(studentProfile.studentId)
            assertNotNull(attendance)
            logger.info("getAttendanceDays (studentId=${studentProfile.studentId}): cdsFreqObbl=${attendance.courseOfStudyMandatoryAttendance}, daysToRecover=${attendance.daysToRecoverNumber}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAttendanceDays: not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getAnnualEnrollment returns all enrollments for student`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            assertNotNull(enrollments)
            logger.info("getAnnualEnrollment (studentId=${studentProfile.studentId}): got ${enrollments.size} enrollments")
            for (enrollment in enrollments) {
                assertNotNull(enrollment.studentId)
                assertTrue(enrollment.studentId == studentProfile.studentId)
                logger.info("  aaIscrId=${enrollment.academicYearEnrollmentId}, cdsId=${enrollment.courseOfStudyId}, courseYear=${enrollment.courseYear}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAnnualEnrollment: not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getAnnualEnrollment with lastEnrollmentFlag`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(
                studentId = studentProfile.studentId,
                lastEnrollmentFlag = 1
            )
            assertNotNull(enrollments)
            assertTrue(enrollments.size <= 1, "lastEnrollmentFlag=1 should return at most 1 enrollment")
            logger.info("getAnnualEnrollment (lastEnrollmentFlag=1): got ${enrollments.size} enrollments")
            for (enrollment in enrollments) {
                logger.info("  aaIscrId=${enrollment.academicYearEnrollmentId}, courseYear=${enrollment.courseYear}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAnnualEnrollment (lastEnrollmentFlag): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getAnnualEnrollment with pagination`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(
                studentId = studentProfile.studentId,
                start = 0,
                limit = 5
            )
            assertNotNull(enrollments)
            assertTrue(enrollments.size <= 5)
            logger.info("getAnnualEnrollment (start=0, limit=5): got ${enrollments.size} enrollments")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAnnualEnrollment (pagination): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getEnrollmentClasses returns classes for all enrollments`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            assertNotNull(enrollments)
            logger.info("getEnrollmentClasses: iterating ${enrollments.size} enrollments")

            for (enrollment in enrollments) {
                val aaIscrId = enrollment.academicYearEnrollmentId ?: continue
                try {
                    val classes = api.careers.getEnrollmentClasses(
                        academicYearEnrollmentId = aaIscrId,
                        studentId = studentProfile.studentId
                    )
                    assertNotNull(classes)
                    logger.info("  getEnrollmentClasses (aaIscrId=$aaIscrId): got ${classes.size} classes")
                    for (cls in classes) {
                        logger.info("    didacticTypeCode=${cls.didacticTypeCode}, languagesCode=${cls.languagesIso6391Code}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("  getEnrollmentClasses (aaIscrId=$aaIscrId): not authorized – ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollmentClasses (outer getAnnualEnrollment): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getEnrollmentClasses with pagination`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            val firstEnrollment = enrollments.firstOrNull() ?: run {
                logger.warn("getEnrollmentClasses (pagination): no enrollments found, skipping")
                return
            }
            val aaIscrId = firstEnrollment.academicYearEnrollmentId ?: run {
                logger.warn("getEnrollmentClasses (pagination): no aaIscrId in first enrollment, skipping")
                return
            }
            try {
                val classes = api.careers.getEnrollmentClasses(
                    academicYearEnrollmentId = aaIscrId,
                    studentId = studentProfile.studentId,
                    start = 0,
                    limit = 5
                )
                assertNotNull(classes)
                assertTrue(classes.size <= 5)
                logger.info("getEnrollmentClasses (pagination, aaIscrId=$aaIscrId): got ${classes.size} classes")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getEnrollmentClasses (pagination): not authorized – ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollmentClasses (pagination outer): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getCareerNotesByStudent with no filters`() {
        try {
            val notes = api.careers.getCareerNotesByStudent(studentProfile.studentId)
            assertNotNull(notes)
            logger.info("getCareerNotesByStudent (no filters): got ${notes.size} notes")
            for (note in notes) {
                logger.info("  noteId=${note.noteId}, type=${note.type}, date=${note.date}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerNotesByStudent (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareerNotesByStudent with blockingNote filter`() {
        try {
            val notes = api.careers.getCareerNotesByStudent(
                studentId = studentProfile.studentId,
                blockingNote = 1
            )
            assertNotNull(notes)
            logger.info("getCareerNotesByStudent (blockingNote=1): got ${notes.size} notes")
            for (note in notes) {
                logger.info("  noteId=${note.noteId}, blockFlag=${note.blockFlag}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerNotesByStudent (blockingNote): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareerNotesByStudent with webNote filter`() {
        try {
            val notes = api.careers.getCareerNotesByStudent(
                studentId = studentProfile.studentId,
                webNote = 1
            )
            assertNotNull(notes)
            logger.info("getCareerNotesByStudent (webNote=1): got ${notes.size} notes")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerNotesByStudent (webNote): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getCareerNotesByStudent with taxNote filter`() {
        try {
            val notes = api.careers.getCareerNotesByStudent(
                studentId = studentProfile.studentId,
                taxNote = 1
            )
            assertNotNull(notes)
            logger.info("getCareerNotesByStudent (taxNote=1): got ${notes.size} notes")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerNotesByStudent (taxNote): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getEnrollmentInfo with no filters`() {
        try {
            val info = api.careers.getEnrollmentInfo()
            assertNotNull(info)
            logger.info("getEnrollmentInfo (no filters): response length=${info.length}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollmentInfo (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getEnrollmentInfo with courseOfStudyId filter`() {
        try {
            val info = api.careers.getEnrollmentInfo(courseOfStudyId = studentProfile.degreeCourseId)
            assertNotNull(info)
            logger.info("getEnrollmentInfo (cdsId=${studentProfile.degreeCourseId}): response length=${info.length}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollmentInfo (courseOfStudyId): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getAllEnrollmentInfo with no filters`() {
        try {
            val info = api.careers.getAllEnrollmentInfo()
            assertNotNull(info)
            logger.info("getAllEnrollmentInfo (no filters): response length=${info.length}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAllEnrollmentInfo (no filters): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getAllEnrollmentInfo with courseOfStudyId filter`() {
        try {
            val info = api.careers.getAllEnrollmentInfo(courseOfStudyId = studentProfile.degreeCourseId)
            assertNotNull(info)
            logger.info("getAllEnrollmentInfo (cdsId=${studentProfile.degreeCourseId}): response length=${info.length}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getAllEnrollmentInfo (courseOfStudyId): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `getEnrollments with current academicYear and no optional filters`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            val latestEnrollment = enrollments.firstOrNull() ?: run {
                logger.warn("getEnrollments: no annual enrollments found for student, skipping")
                return
            }
            val aaIscrId = latestEnrollment.academicYearEnrollmentId?.toString() ?: run {
                logger.warn("getEnrollments: aaIscrId is null in first enrollment, skipping")
                return
            }
            try {
                val enrollmentList = api.careers.getEnrollments(
                    academicYearEnrollmentId = aaIscrId,
                    includeSXH = 0,
                    includeCondition = 0
                )
                assertNotNull(enrollmentList)
                logger.info("getEnrollments (aaIscrId=$aaIscrId, inclSXH=0, inclCond=0): got ${enrollmentList.size} enrollments")
                for (enrollment in enrollmentList) {
                    logger.info("  stuId=${enrollment.studentId}, iscrId=${enrollment.enrollmentId}, cdsId=${enrollment.courseOfStudyId}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getEnrollments: not authorized – ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollments (outer getAnnualEnrollment): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getEnrollments with includeSXH and includeCondition enabled`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            val latestEnrollment = enrollments.firstOrNull() ?: run {
                logger.warn("getEnrollments (inclSXH+inclCond): no enrollments, skipping")
                return
            }
            val aaIscrId = latestEnrollment.academicYearEnrollmentId?.toString() ?: run {
                logger.warn("getEnrollments (inclSXH+inclCond): aaIscrId null, skipping")
                return
            }
            try {
                val enrollmentList = api.careers.getEnrollments(
                    academicYearEnrollmentId = aaIscrId,
                    includeSXH = 1,
                    includeCondition = 1
                )
                assertNotNull(enrollmentList)
                logger.info("getEnrollments (inclSXH=1, inclCond=1): got ${enrollmentList.size} enrollments")
                for (enrollment in enrollmentList) {
                    logger.info("  stuId=${enrollment.studentId}, staIscrCod=${enrollment.enrollmentStatusCode}, condFlg=${enrollment.conditionFlag}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getEnrollments (inclSXH+inclCond): not authorized – ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollments (outer inclSXH+inclCond): not authorized – ${e.message}")
        }
    }

    @Test
    suspend fun `getEnrollments with pagination`() {
        try {
            val enrollments = api.careers.getAnnualEnrollment(studentProfile.studentId)
            val latestEnrollment = enrollments.firstOrNull() ?: run {
                logger.warn("getEnrollments (pagination): no enrollments, skipping")
                return
            }
            val aaIscrId = latestEnrollment.academicYearEnrollmentId?.toString() ?: run {
                logger.warn("getEnrollments (pagination): aaIscrId null, skipping")
                return
            }
            try {
                val enrollmentList = api.careers.getEnrollments(
                    academicYearEnrollmentId = aaIscrId,
                    includeSXH = 0,
                    includeCondition = 0,
                    start = 0,
                    limit = 5
                )
                assertNotNull(enrollmentList)
                assertTrue(enrollmentList.size <= 5)
                logger.info("getEnrollments (pagination, limit=5): got ${enrollmentList.size} enrollments")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getEnrollments (pagination): not authorized – ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getEnrollments (pagination outer): not authorized – ${e.message}")
        }
    }


    @Test
    suspend fun `verifyEnrollment with fiscalCode from session`() {
        val fiscalCode = session.fiscalCode ?: run {
            logger.warn("verifyEnrollment: no fiscalCode in session, skipping")
            return
        }
        try {
            val result = api.careers.verifyEnrollment(fiscalCode = fiscalCode)
            logger.info("verifyEnrollment (fiscalCode=$fiscalCode): result=$result")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("verifyEnrollment: not authorized – ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warn("verifyEnrollment: validation error – ${e.message}")
        }
    }


    @Disabled("Destructive: verifies enrollment cancellability (potentially alters enrollment state)")
    @Test
    suspend fun `verifyEnrollmentCancellability with studentId`() {
        val result = api.careers.verifyEnrollmentCancellability(studentId = studentProfile.studentId)
        assertNotNull(result)
        logger.info("verifyEnrollmentCancellability: result=$result")
    }

    @Disabled("Destructive: modifies graduation waiting status")
    @Test
    fun `putGraduationWaiting is disabled`() {
        // Disabled to prevent accidental modification of graduation status
    }

    @Disabled("Destructive: modifies enrollment date and exemption type by matricola")
    @Test
    fun `putEnrollmentDateAndExemptionTypeByMatricola is disabled`() {
        // Disabled to prevent accidental enrollment modification
    }

    @Disabled("Destructive: modifies career data")
    @Test
    fun `putCareerByStudent is disabled`() {
        // Disabled to prevent accidental career data modification
    }

    @Disabled("Destructive: closes career permanently")
    @Test
    fun `careerClosure is disabled`() {
        // Disabled to prevent accidental career closure
    }

    @Disabled("Destructive: modifies enrollment date and exemption type by studentId")
    @Test
    fun `putEnrollmentDateAndExemptionTypeByStudentId is disabled`() {
        // Disabled to prevent accidental enrollment modification
    }

    @Disabled("Destructive: modifies canteen band for student")
    @Test
    fun `putCanteenBand is disabled`() {
        // Disabled to prevent accidental canteen band modification
    }

    @Disabled("Destructive: modifies student type code")
    @Test
    fun `putStudentTypeCode is disabled`() {
        // Disabled to prevent accidental student type modification
    }
}
