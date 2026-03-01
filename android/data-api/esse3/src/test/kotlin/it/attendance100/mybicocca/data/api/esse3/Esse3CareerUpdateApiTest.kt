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

class Esse3CareerUpdateApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3CareerUpdateApiTest::class.java.name)

    @Test
    suspend fun testGetCareerUpdateHeaders() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            logger.info("getCareerUpdateHeaders: found ${headers.size} headers")
            for (header in headers) {
                logger.info(
                    "  header: careerUpdateId=${header.careerUpdateId}, procedureCode=${header.procedureCode}, " +
                        "state=${header.state}, courseOfStudyCode=${header.courseOfStudyCode}, " +
                        "studyPlanCode=${header.studyPlanCode}, parameters=${header.parameters.size}"
                )
                assertNotNull(header.careerUpdateId, "careerUpdateId should not be null")
                assertNotNull(header.procedureCode, "procedureCode should not be null")
                for (param in header.parameters) {
                    logger.info(
                        "    param: parameterCode=${param.parameterCode}, parameterDescription=${param.parameterDescription}, " +
                            "alphanumericValue=${param.alphanumericValue}, numericValue=${param.numericValue}"
                    )
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCareerUpdateHeader() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            if (headers.isEmpty()) {
                logger.warning("No career update headers available — skipping testGetCareerUpdateHeader")
                return
            }
            for (header in headers) {
                val careerUpdateId = header.careerUpdateId ?: continue
                try {
                    val fetched = api.careerUpdate.getCareerUpdateHeader(careerUpdateId)
                    logger.info(
                        "getCareerUpdateHeader($careerUpdateId): procedureCode=${fetched.procedureCode}, " +
                            "state=${fetched.state}, courseOfStudyCode=${fetched.courseOfStudyCode}, " +
                            "parameters=${fetched.parameters.size}"
                    )
                    assertEquals(careerUpdateId, fetched.careerUpdateId, "Returned careerUpdateId should match requested id")
                    assertNotNull(fetched.procedureCode, "procedureCode should not be null")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCareerUpdateHeader($careerUpdateId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCareerUpdateHeader($careerUpdateId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPreviewDetails() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            if (headers.isEmpty()) {
                logger.warning("No career update headers available — skipping testGetPreviewDetails")
                return
            }
            for (header in headers) {
                val careerUpdateId = header.careerUpdateId ?: continue
                try {
                    val details = api.careerUpdate.getPreviewDetails(careerUpdateId)
                    logger.info("getPreviewDetails($careerUpdateId): found ${details.size} detail rows")
                    for (detail in details) {
                        logger.info(
                            "  detail: careerUpdateDetailId=${detail.careerUpdateDetailId}, " +
                                "studentId=${detail.studentId}, matId=${detail.matId}, " +
                                "matricola=${detail.matricola}, name=${detail.name}, surname=${detail.surname}, " +
                                "activityCode=${detail.activityCode}, activityDescription=${detail.activityDescription}, " +
                                "processFlag=${detail.processFlag}, errorMessage=${detail.errorMessage}"
                        )
                        assertNotNull(detail.careerUpdateId, "careerUpdateId in detail should not be null")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPreviewDetails($careerUpdateId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPreviewDetails($careerUpdateId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPreviewDetailsWithPagination() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            val firstHeader = headers.firstOrNull { it.careerUpdateId != null }
            if (firstHeader == null) {
                logger.warning("No career update headers available — skipping testGetPreviewDetailsWithPagination")
                return
            }
            val careerUpdateId = firstHeader.careerUpdateId!!
            try {
                val details = api.careerUpdate.getPreviewDetails(careerUpdateId, start = 0, limit = 5)
                logger.info("getPreviewDetails($careerUpdateId, paginated): found ${details.size} rows (max 5)")
                assertTrue(details.size <= 5, "Paginated result should have at most 5 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getPreviewDetails($careerUpdateId, paginated) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getPreviewDetails($careerUpdateId, paginated) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPreviewDetail() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            if (headers.isEmpty()) {
                logger.warning("No career update headers available — skipping testGetPreviewDetail")
                return
            }
            for (header in headers) {
                val careerUpdateId = header.careerUpdateId ?: continue
                try {
                    val details = api.careerUpdate.getPreviewDetails(careerUpdateId)
                    if (details.isEmpty()) {
                        logger.info("getPreviewDetails($careerUpdateId): no detail rows, skipping getPreviewDetail")
                        continue
                    }
                    for (detail in details) {
                        val detailId = detail.careerUpdateDetailId ?: continue
                        try {
                            val singleDetail = api.careerUpdate.getPreviewDetail(careerUpdateId, detailId)
                            logger.info(
                                "getPreviewDetail($careerUpdateId, $detailId): studentId=${singleDetail.studentId}, " +
                                    "activityCode=${singleDetail.activityCode}, processFlag=${singleDetail.processFlag}"
                            )
                            assertEquals(careerUpdateId, singleDetail.careerUpdateId, "careerUpdateId should match")
                            assertEquals(detailId, singleDetail.careerUpdateDetailId, "careerUpdateDetailId should match")
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("getPreviewDetail($careerUpdateId, $detailId) not authorized: ${e.message}")
                        } catch (e: Esse3ValidationException) {
                            logger.warning("getPreviewDetail($careerUpdateId, $detailId) validation error: ${e.message}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPreviewDetails($careerUpdateId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPreviewDetails($careerUpdateId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetUpdateCareersLog() {
        try {
            val headers = api.careerUpdate.getCareerUpdateHeaders()
            if (headers.isEmpty()) {
                logger.warning("No career update headers available — skipping testGetUpdateCareersLog")
                return
            }
            for (header in headers) {
                val careerUpdateId = header.careerUpdateId ?: continue
                try {
                    val logEntries = api.careerUpdate.getUpdateCareersLog(careerUpdateId)
                    logger.info("getUpdateCareersLog($careerUpdateId): found ${logEntries.size} log entries")
                    for (entry in logEntries) {
                        logger.info(
                            "  log entry: careerUpdateId=${entry.careerUpdateId}, " +
                                "logTypeId=${entry.logTypeId}, logDescription=${entry.logDescription}"
                        )
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getUpdateCareersLog($careerUpdateId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getUpdateCareersLog($careerUpdateId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCareerUpdateHeaders not authorized: ${e.message}")
        }
    }

    @Disabled("Destructive: executes career update which modifies student career data and cannot be reversed")
    @Test
    suspend fun testExecuteUpdateCareers() {
        // Disabled: executeUpdateCareers runs a permanent career update procedure.
        // Execution modifies student career records and cannot be safely rolled back.
    }

    @Disabled("Destructive: deletes a career update preview record permanently")
    @Test
    suspend fun testDeletePreview() {
        // Disabled: deletePreview removes the career update preview from the system.
        // Cannot be recovered after deletion.
    }

    @Disabled("Mutates career data: setProcessingDetailFlag changes the elabora flag on a career update detail row")
    @Test
    suspend fun testSetProcessingDetailFlag() {
        // Disabled: modifies career update processing state on the server.
    }

    @Disabled("Destructive: postAddTeachingActivityOffer creates a teaching activity offer career update. Cannot be safely reversed.")
    @Test
    suspend fun testPostAddTeachingActivityOffer() {
        // Disabled: creates a career update record in the system.
    }

    @Disabled("Destructive: putAddTeachingActivityOfferByStudent creates a teaching activity offer career update for a specific student. Cannot be safely reversed.")
    @Test
    suspend fun testPutAddTeachingActivityOfferByStudent() {
        // Disabled: creates career update records for a student.
    }

    @Disabled("Destructive: postAttendance creates an attendance career update. Cannot be safely reversed.")
    @Test
    suspend fun testPostAttendance() {
        // Disabled: creates attendance update records in the system.
    }

    @Disabled("Destructive: putAttendanceByStudent creates an attendance career update for a specific student. Cannot be safely reversed.")
    @Test
    suspend fun testPutAttendanceByStudent() {
        // Disabled: creates attendance update records for a student.
    }

    @Disabled("Destructive: postRemoveAttendance creates a remove-attendance career update. Cannot be safely reversed.")
    @Test
    suspend fun testPostRemoveAttendance() {
        // Disabled: creates remove-attendance update records in the system.
    }

    @Disabled("Destructive: putRemoveAttendanceByStudent creates a remove-attendance career update for a specific student. Cannot be safely reversed.")
    @Test
    suspend fun testPutRemoveAttendanceByStudent() {
        // Disabled: creates remove-attendance update records for a student.
    }

    @Disabled("Destructive: postSEG creates a segmentation career update. Cannot be safely reversed.")
    @Test
    suspend fun testPostSEG() {
        // Disabled: creates segment update records in the system.
    }

    @Disabled("Destructive: putSEGByStudent creates a segmentation career update for a specific student. Cannot be safely reversed.")
    @Test
    suspend fun testPutSEGByStudent() {
        // Disabled: creates segment update records for a student.
    }

    @Disabled("Destructive: postSubstitution creates a substitution career update. Cannot be safely reversed.")
    @Test
    suspend fun testPostSubstitution() {
        // Disabled: creates substitution update records in the system.
    }

    @Disabled("Destructive: putSubstitutionByStudent creates a substitution career update for a specific student. Cannot be safely reversed.")
    @Test
    suspend fun testPutSubstitutionByStudent() {
        // Disabled: creates substitution update records for a student.
    }
}
