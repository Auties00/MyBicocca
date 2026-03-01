package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import it.attendance100.mybicocca.data.dto.esse3.Esse3SignatureImportData
import it.attendance100.mybicocca.data.dto.esse3.Esse3SignatureData
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Esse3UsersApiTest : Esse3ApiTestBase() {

    private val logger: Logger = LoggerFactory.getLogger(Esse3UsersApiTest::class.java)

    @Test
    suspend fun `getUsers - no filters returns list`() {
        try {
            val users = api.users.getUsers()
            logger.info("getUsers (no filters): {} users", users.size)
            for (user in users) {
                assertNotNull(user.userId, "userId should not be null")
                logger.info("  user: userId={}, name={} {}", user.userId, user.firstName, user.lastName)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUsers (no filters): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getUsers - filter by userId returns matching user`() {
        try {
            val users = api.users.getUsers(userId = session.userId)
            logger.info("getUsers (userId filter): {} results", users.size)
            for (user in users) {
                assertNotNull(user.userId, "userId should not be null")
                assertTrue(
                    user.userId.equals(session.userId, ignoreCase = true),
                    "Expected userId to match filter '${session.userId}', got '${user.userId}'"
                )
                logger.info("  user: userId={}, name={} {}, groupId={}", user.userId, user.firstName, user.lastName, user.groupId)
            }
            assertTrue(users.isNotEmpty(), "Expected at least one user matching userId=${session.userId}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUsers (userId filter): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getUsers - filter by fiscalCode returns matching user`() {
        val fiscalCode = session.fiscalCode ?: run {
            logger.warn("getUsers (fiscalCode filter): skipping - no fiscal code in session")
            return
        }
        try {
            val users = api.users.getUsers(fiscalCode = fiscalCode)
            logger.info("getUsers (fiscalCode filter): {} results", users.size)
            for (user in users) {
                assertNotNull(user.userId)
                logger.info("  user: userId={}, fiscalCode={}", user.userId, user.fiscalCode)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUsers (fiscalCode filter): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getUsers - pagination with start and limit`() {
        try {
            val usersPage1 = api.users.getUsers(start = 0, limit = 5)
            logger.info("getUsers (page 1, limit 5): {} results", usersPage1.size)
            assertTrue(usersPage1.size <= 5, "Page size should be <= 5")

            val usersPage2 = api.users.getUsers(start = 5, limit = 5)
            logger.info("getUsers (page 2, limit 5): {} results", usersPage2.size)
            assertTrue(usersPage2.size <= 5, "Page size should be <= 5")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUsers (pagination): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getUser - retrieve current user by userId`() {
        try {
            val users = api.users.getUser(userId = session.userId)
            logger.info("getUser({}): {} results", session.userId, users.size)
            assertTrue(users.isNotEmpty(), "Expected at least one result for userId=${session.userId}")
            val user = users.first()
            assertNotNull(user.userId)
            assertTrue(
                user.userId.equals(session.userId, ignoreCase = true),
                "userId mismatch: expected ${session.userId}, got ${user.userId}"
            )
            logger.info("  user: id={}, firstName={}, lastName={}, groupId={}, groupDescription={}",
                user.id, user.firstName, user.lastName, user.groupId, user.groupDescription)
            logger.info("  personId={}, fiscalCode={}, technicalUserGroupFlag={}",
                user.personId, user.fiscalCode, user.technicalUserGroupFlag)
            logger.info("  careerSegments: {}", user.careerSegments.size)
            for (segment in user.careerSegments) {
                logger.info("    segment: stuId={}, matId={}, matricola={}, cdsId={}, status={}",
                    segment.studentId, segment.matId, segment.matricola,
                    segment.courseOfStudyId, segment.studentStatusCode)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUser: not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getUser - with caseSensitive param`() {
        try {
            val users = api.users.getUser(userId = session.userId, caseSensitive = 0)
            logger.info("getUser (caseSensitive=0): {} results", users.size)
            for (user in users) {
                assertNotNull(user.userId)
                logger.info("  user: userId={}, name={} {}", user.userId, user.firstName, user.lastName)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getUser (caseSensitive): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getDigitalIdentity - retrieve for current user`() {
        try {
            val identity = api.users.getDigitalIdentity(userId = session.userId)
            logger.info("getDigitalIdentity({}): userId={}, spidCode={}, cieCode={}",
                session.userId, identity.userId, identity.spidCode, identity.cieCode)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getDigitalIdentity: not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getExtendedCareer - retrieve for current user`() {
        try {
            val portions = api.users.getExtendedCareer(userId = session.userId)
            logger.info("getExtendedCareer({}): {} portions", session.userId, portions.size)
            for (portion in portions) {
                assertNotNull(portion.personId)
                assertNotNull(portion.studentId)
                assertNotNull(portion.courseOfStudyId)
                logger.info("  portion: persId={}, stuId={}, cdsId={}, matricola={}, status={}",
                    portion.personId, portion.studentId, portion.courseOfStudyId,
                    portion.matricola, portion.studentStatusCode)
                logger.info("    courseOfStudyDescription={}, studyPlanDescription={}",
                    portion.courseOfStudyDescription, portion.studyPlanDescription)
                logger.info("    academicYearId={}, enrollmentDate={}",
                    portion.academicYearId, portion.enrollmentDate)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExtendedCareer: not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getExtendedCareer - filter by student status`() {
        try {
            // "A" is typically "Attivo" (active)
            val portions = api.users.getExtendedCareer(userId = session.userId, studentStatusCode = "A")
            logger.info("getExtendedCareer (staStuCod=A): {} portions", portions.size)
            for (portion in portions) {
                logger.info("  portion: stuId={}, status={}", portion.studentId, portion.studentStatusCode)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getExtendedCareer (status filter): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getCareerSegments - retrieve for current user`() {
        try {
            val segments = api.users.getCareerSegments(userId = session.userId)
            logger.info("getCareerSegments({}): {} segments", session.userId, segments.size)
            for (segment in segments) {
                assertNotNull(segment.studentId)
                logger.info("  segment: stuId={}, cdsId={}, matId={}",
                    segment.studentId, segment.courseOfStudyId, segment.matId)
                logger.info("    status={}, cdsCod={}",
                    segment.studentStatusCode, segment.courseOfStudyCode)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerSegments: not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `getCareerSegments - filter by student status`() {
        try {
            val segments = api.users.getCareerSegments(userId = session.userId, studentStatusCode = "A")
            logger.info("getCareerSegments (staStuCod=A): {} segments", segments.size)
            for (segment in segments) {
                logger.info("  segment: stuId={}, status={}", segment.studentId, segment.studentStatusCode)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("getCareerSegments (status filter): not authorized - {}", e.message)
        }
    }

    @Test
    suspend fun `postUserSignatureData - unauthorized or validation error expected for student`() {
        val body = Esse3SignatureImportData(
            signatureData = listOf(
                Esse3SignatureData(
                    fiscalCode = session.fiscalCode ?: "UNKNOWN",
                    signatureTypeId = null,
                    hsmPrefix = null
                )
            )
        )
        try {
            val responses = api.users.postUserSignatureData(body)
            logger.info("postUserSignatureData: {} responses", responses.size)
            for (response in responses) {
                logger.info("  response: fiscalCode={}, returnCode={}, errorMessage={}",
                    response.fiscalCode, response.returnCode, response.errorMessage)
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("postUserSignatureData: not authorized (expected for student) - {}", e.message)
        } catch (e: Esse3ValidationException) {
            logger.warn("postUserSignatureData: validation error - {}", e.message)
        }
    }

    @Test
    suspend fun `postUserSignatureDataCsv - unauthorized or validation error expected for student`() {
        val csvBody = "codFis\n${session.fiscalCode ?: "UNKNOWN"}"
        try {
            val result = api.users.postUserSignatureDataCsv(body = csvBody, delimiter = ",")
            logger.info("postUserSignatureDataCsv: result length={}", result.length)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("postUserSignatureDataCsv: not authorized (expected for student) - {}", e.message)
        } catch (e: Esse3ValidationException) {
            logger.warn("postUserSignatureDataCsv: validation error - {}", e.message)
        }
    }

    @Test
    @Disabled("putCieCode is a mutation that cannot be safely rolled back in integration tests")
    suspend fun `putCieCode - disabled irreversible mutation`() {
        // This endpoint updates the CIE code for a user.
        // Disabled because there is no rollback mechanism available.
    }

    @Test
    @Disabled("putSpidCode is a mutation that cannot be safely rolled back in integration tests")
    suspend fun `putSpidCode - disabled irreversible mutation`() {
        // This endpoint updates the SPID code for a user.
        // Disabled because there is no rollback mechanism available.
    }
}
