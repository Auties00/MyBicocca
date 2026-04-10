package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3CareerUpdateApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3CareerUpdateApiTest::class.java.name)
        private lateinit var userPermissions: Set<Esse3PermissionLevel>
    }

    @BeforeAll
    fun setUp() = runTest {
        logger.info("Logging in to derive user permissions")
        val loginResult = api.auth.login()
        logger.info("Login successful: userId=${loginResult.user.userId}, groupId=${loginResult.user.groupId}")

        val permissions = mutableSetOf(
            Esse3PermissionLevel.ANY,
            Esse3PermissionLevel.AUTHENTICATED_USER,
            Esse3PermissionLevel.fromGroupId(loginResult.user.groupId)
        )
        for (profile in loginResult.profiles) {
            val description = profile.description
            if (description != null) {
                permissions.add(Esse3PermissionLevel.fromProfileName(description))
            }
        }
        userPermissions = permissions
        logger.info("Derived user permissions: $userPermissions")
    }

    @Test
    fun testGetCareerUpdateHeaders() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerUpdateHeaders: calling getCareerUpdateHeaders() with defaults")
            val result = api.careerUpdate.getCareerUpdateHeaders()
            logger.info("testGetCareerUpdateHeaders: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getCareerUpdateHeaders() should return non-empty list")
            for (header in result) {
                logger.info("testGetCareerUpdateHeaders: careerUpdateId=${header.careerUpdateId}, procedureCode=${header.procedureCode}, state=${header.state}")
                assertNotNull(header.careerUpdateId, "careerUpdateId should not be null")
            }
        } else {
            logger.info("testGetCareerUpdateHeaders: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.getCareerUpdateHeaders()
            }
            logger.info("testGetCareerUpdateHeaders: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCareerUpdateHeader() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCareerUpdateHeader: fetching headers to obtain a valid careerUpdateId")
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            assertTrue(headers.isNotEmpty(), "Need at least one header to test getCareerUpdateHeader")
            val careerUpdateId = headers.first().careerUpdateId
            assertNotNull(careerUpdateId, "careerUpdateId should not be null")

            logger.info("testGetCareerUpdateHeader: calling getCareerUpdateHeader(careerUpdateId=$careerUpdateId)")
            val result = api.careerUpdate.getCareerUpdateHeader(careerUpdateId)
            logger.info("testGetCareerUpdateHeader: careerUpdateId=${result.careerUpdateId}, procedureCode=${result.procedureCode}, state=${result.state}, courseOfStudyDescription=${result.courseOfStudyDescription}")
            assertNotNull(result.careerUpdateId, "careerUpdateId should not be null in response")
        } else {
            logger.info("testGetCareerUpdateHeader: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.getCareerUpdateHeader(1L)
            }
            logger.info("testGetCareerUpdateHeader: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPreviewDetails() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPreviewDetails: fetching headers to obtain a valid careerUpdateId")
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            assertTrue(headers.isNotEmpty(), "Need at least one header to test getPreviewDetails")
            val careerUpdateId = headers.first().careerUpdateId
            assertNotNull(careerUpdateId, "careerUpdateId should not be null")

            logger.info("testGetPreviewDetails: calling getPreviewDetails(careerUpdateId=$careerUpdateId) with defaults")
            val defaultResult = api.careerUpdate.getPreviewDetails(careerUpdateId)
            logger.info("testGetPreviewDetails: default result size=${defaultResult.size}")
            for (row in defaultResult) {
                logger.info("testGetPreviewDetails: careerUpdateDetailId=${row.careerUpdateDetailId}, studentId=${row.studentId}, matricola=${row.matricola}, activityCode=${row.activityCode}")
            }

            logger.info("testGetPreviewDetails: calling getPreviewDetails with pagination start=0, limit=5")
            val paginatedResult = api.careerUpdate.getPreviewDetails(careerUpdateId, start = 0, limit = 5)
            logger.info("testGetPreviewDetails: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetPreviewDetails: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.getPreviewDetails(1L)
            }
            logger.info("testGetPreviewDetails: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPreviewDetail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPreviewDetail: fetching headers to obtain a valid careerUpdateId")
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            assertTrue(headers.isNotEmpty(), "Need at least one header to test getPreviewDetail")
            val careerUpdateId = headers.first().careerUpdateId
            assertNotNull(careerUpdateId, "careerUpdateId should not be null")

            logger.info("testGetPreviewDetail: fetching preview details to obtain a valid careerUpdateDetailId")
            val details = api.careerUpdate.getPreviewDetails(careerUpdateId)
            assertTrue(details.isNotEmpty(), "Need at least one detail to test getPreviewDetail")
            val careerUpdateDetailId = details.first().careerUpdateDetailId
            assertNotNull(careerUpdateDetailId, "careerUpdateDetailId should not be null")

            logger.info("testGetPreviewDetail: calling getPreviewDetail(careerUpdateId=$careerUpdateId, careerUpdateDetailId=$careerUpdateDetailId)")
            val result = api.careerUpdate.getPreviewDetail(careerUpdateId, careerUpdateDetailId)
            logger.info("testGetPreviewDetail: careerUpdateDetailId=${result.careerUpdateDetailId}, studentId=${result.studentId}, name=${result.name}, surname=${result.surname}, activityDescription=${result.activityDescription}")
            assertNotNull(result.careerUpdateDetailId, "careerUpdateDetailId should not be null in response")
        } else {
            logger.info("testGetPreviewDetail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.getPreviewDetail(1L, 1L)
            }
            logger.info("testGetPreviewDetail: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetUpdateCareersLog() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetUpdateCareersLog: fetching headers to obtain a valid careerUpdateId")
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            assertTrue(headers.isNotEmpty(), "Need at least one header to test getUpdateCareersLog")
            val careerUpdateId = headers.first().careerUpdateId
            assertNotNull(careerUpdateId, "careerUpdateId should not be null")

            logger.info("testGetUpdateCareersLog: calling getUpdateCareersLog(careerUpdateId=$careerUpdateId)")
            val result = api.careerUpdate.getUpdateCareersLog(careerUpdateId)
            logger.info("testGetUpdateCareersLog: result size=${result.size}")
            for (log in result) {
                logger.info("testGetUpdateCareersLog: careerUpdateId=${log.careerUpdateId}, logTypeId=${log.logTypeId}, logDescription=${log.logDescription}")
            }
        } else {
            logger.info("testGetUpdateCareersLog: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.getUpdateCareersLog(1L)
            }
            logger.info("testGetUpdateCareersLog: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostAddTeachingActivityOffer() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAddTeachingActivityOffer: skipped, irreversible mutating operation")
        } else {
            logger.info("testPostAddTeachingActivityOffer: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.postAddTeachingActivityOffer(
                    it.attendance100.mybicocca.data.dto.esse3.Esse3ProcedureUpdateActivityOfferParameters(
                        forceFreeTeachingActivitiesFlag = false,
                        activityIdToUpdate = 0L
                    )
                )
            }
            logger.info("testPostAddTeachingActivityOffer: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutAddTeachingActivityOfferByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutAddTeachingActivityOfferByStudent: skipped, irreversible mutating operation")
        } else {
            logger.info("testPutAddTeachingActivityOfferByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.putAddTeachingActivityOfferByStudent(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3ProcedureUpdateActivityOfferParameters(
                        forceFreeTeachingActivitiesFlag = false,
                        activityIdToUpdate = 0L
                    )
                )
            }
            logger.info("testPutAddTeachingActivityOfferByStudent: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostAttendance() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAttendance: skipped, irreversible mutating operation")
        } else {
            logger.info("testPostAttendance: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.postAttendance(
                    it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceProcedureParameters(
                        forceStudentsX = false,
                        forceAttendanceDate = false,
                        forceStateFFlag = false,
                        forceUnplanned = false
                    )
                )
            }
            logger.info("testPostAttendance: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutAttendanceByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutAttendanceByStudent: skipped, irreversible mutating operation")
        } else {
            logger.info("testPutAttendanceByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.putAttendanceByStudent(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3AttendanceProcedureParameters(
                        forceStudentsX = false,
                        forceAttendanceDate = false,
                        forceStateFFlag = false,
                        forceUnplanned = false
                    )
                )
            }
            logger.info("testPutAttendanceByStudent: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostRemoveAttendance() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostRemoveAttendance: skipped, irreversible mutating operation")
        } else {
            logger.info("testPostRemoveAttendance: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.postRemoveAttendance(
                    it.attendance100.mybicocca.data.dto.esse3.Esse3RemoveAttendanceProcedureParameters(
                        forceStudentsX = false
                    )
                )
            }
            logger.info("testPostRemoveAttendance: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutRemoveAttendanceByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutRemoveAttendanceByStudent: skipped, irreversible mutating operation")
        } else {
            logger.info("testPutRemoveAttendanceByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.putRemoveAttendanceByStudent(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3RemoveAttendanceProcedureParameters(
                        forceStudentsX = false
                    )
                )
            }
            logger.info("testPutRemoveAttendanceByStudent: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostSEG() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostSEG: skipped, irreversible mutating operation")
        } else {
            logger.info("testPostSEG: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.postSEG(
                    it.attendance100.mybicocca.data.dto.esse3.Esse3SegmentProcedureParameters(
                        forceStateFSFlag = false,
                        forceStudentsX = false,
                        forceFreeTeachingActivitiesFlag = false
                    )
                )
            }
            logger.info("testPostSEG: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutSEGByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutSEGByStudent: skipped, irreversible mutating operation")
        } else {
            logger.info("testPutSEGByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.putSEGByStudent(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3SegmentProcedureParameters(
                        forceStateFSFlag = false,
                        forceStudentsX = false,
                        forceFreeTeachingActivitiesFlag = false
                    )
                )
            }
            logger.info("testPutSEGByStudent: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPostSubstitution() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostSubstitution: skipped, irreversible mutating operation")
        } else {
            logger.info("testPostSubstitution: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.postSubstitution(
                    it.attendance100.mybicocca.data.dto.esse3.Esse3SubstitutionProcedureParameters(
                        forceStateSFlag = false
                    )
                )
            }
            logger.info("testPostSubstitution: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testPutSubstitutionByStudent() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutSubstitutionByStudent: skipped, irreversible mutating operation")
        } else {
            logger.info("testPutSubstitutionByStudent: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.putSubstitutionByStudent(
                    matId = studentProfile.matId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3SubstitutionProcedureParameters(
                        forceStateSFlag = false
                    )
                )
            }
            logger.info("testPutSubstitutionByStudent: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testDeletePreview() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testDeletePreview: skipped, irreversible mutating operation")
        } else {
            logger.info("testDeletePreview: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.deletePreview(1L)
            }
            logger.info("testDeletePreview: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testSetProcessingDetailFlag() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testSetProcessingDetailFlag: skipped, irreversible mutating operation")
        } else {
            logger.info("testSetProcessingDetailFlag: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.setProcessingDetailFlag(
                    careerUpdateId = 1L,
                    careerUpdateDetailId = 1L,
                    body = kotlinx.serialization.json.JsonObject(emptyMap())
                )
            }
            logger.info("testSetProcessingDetailFlag: Esse3Exception thrown as expected")
        }
    }

    @Disabled("Irreversible mutating operation")
    @Test
    fun testExecuteUpdateCareers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testExecuteUpdateCareers: skipped, irreversible mutating operation")
        } else {
            logger.info("testExecuteUpdateCareers: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.careerUpdate.executeUpdateCareers(1L)
            }
            logger.info("testExecuteUpdateCareers: Esse3Exception thrown as expected")
        }
    }
}
