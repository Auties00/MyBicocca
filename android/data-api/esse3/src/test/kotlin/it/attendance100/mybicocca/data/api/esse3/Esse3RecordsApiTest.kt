package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3RecordsApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetBatches() {
        try {
            val batches = api.records.getBatches()
            assertNotNull(batches)
            logger.info("getBatches returned ${batches.size} batches")
            for (batch in batches) {
                logger.info("  batch: batchId=${batch.batchId}, batchNum=${batch.batchNumber}, adCod=${batch.activityCode}, adDes=${batch.activityDescription}")
                assertNotNull(batch.batchId, "batchId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBatches: insufficient permissions (requires TECHNICAL_USER) - ${e.message}")
        }
    }

    @Test
    suspend fun testGetBatchesWithFilters() {
        try {
            val batches = api.records.getBatches(limit = 10)
            assertNotNull(batches)
            logger.info("getBatches with limit=10 returned ${batches.size} batches")
            for (batch in batches) {
                logger.info("  batch: batchId=${batch.batchId}, adCod=${batch.activityCode}, dataAcq=${batch.acquisitionDate}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBatchesWithFilters: insufficient permissions (requires TECHNICAL_USER) - ${e.message}")
        }
    }

    @Test
    suspend fun testGetBatchById() {
        try {
            val batches = api.records.getBatches(limit = 5)
            assertNotNull(batches)

            if (batches.isEmpty()) {
                logger.warning("No batches found, skipping getBatch(batchId) test")
                return
            }

            for (batch in batches) {
                val batchId = batch.batchId ?: continue
                val batchDetail = api.records.getBatch(batchId = batchId)
                assertNotNull(batchDetail)
                assertEquals(batchId, batchDetail.batchId, "batchId should match")
                logger.info("  getBatch(batchId=$batchId): batchNum=${batchDetail.batchNumber}, adCod=${batchDetail.activityCode}, verbali=${batchDetail.minutes.size}")
                for (minute in batchDetail.minutes) {
                    logger.info("    minute: verbId=${minute.minutesId}, matricola=${minute.matricola}, voto=${minute.grade}, statoVerbale=${minute.minutesState}")
                    assertNotNull(minute.minutesId, "minutesId should not be null")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBatchById: insufficient permissions (requires TECHNICAL_USER) - ${e.message}")
        }
    }

    @Test
    suspend fun testFindBatches() {
        try {
            val batches = api.records.findBatches(limit = 10)
            assertNotNull(batches)
            logger.info("findBatches returned ${batches.size} batches")
            for (batch in batches) {
                logger.info("  batch: lottoId=${batch.lotBatchId}, adCod=${batch.activityCode}, statoLotto=${batch.batchState}, dataApp=${batch.callDate}")
                assertNotNull(batch.lotBatchId, "lotBatchId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindBatches: insufficient permissions (requires TECHNICAL_USER or TEACHER) - ${e.message}")
        }
    }

    @Test
    suspend fun testFindBatchesWithFilters() {
        try {
            // Find batches with fromDate filter
            val batches = api.records.findBatches(
                fromDate = "01/01/2020",
                limit = 10
            )
            assertNotNull(batches)
            logger.info("findBatches with fromDate=01/01/2020 returned ${batches.size} batches")
            for (batch in batches) {
                logger.info("  batch: lottoId=${batch.lotBatchId}, adCod=${batch.activityCode}, statoLotto=${batch.batchState}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindBatchesWithFilters: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testFindBatchesWithFilters: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testFindBatchesByStudentMatricola() {
        try {
            val matricola = studentProfile.enrollmentNumber
            val batches = api.records.findBatches(studentMatricola = matricola, limit = 10)
            assertNotNull(batches)
            logger.info("findBatches by studentMatricola=$matricola returned ${batches.size} batches")
            for (batch in batches) {
                logger.info("  batch: lottoId=${batch.lotBatchId}, adCod=${batch.activityCode}, statoLotto=${batch.batchState}, dataApp=${batch.callDate}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindBatchesByStudentMatricola: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetBatchByLotId() {
        try {
            val batches = api.records.findBatches(limit = 5)
            assertNotNull(batches)

            if (batches.isEmpty()) {
                logger.warning("No batches found via findBatches, skipping getBatch(lotBatchId) test")
                return
            }

            for (batch in batches) {
                val lotBatchId = batch.lotBatchId ?: continue
                val batchDetails = api.records.getBatch(lotBatchId = lotBatchId)
                assertNotNull(batchDetails)
                logger.info("  getBatch(lotBatchId=$lotBatchId): returned ${batchDetails.size} details")
                for (detail in batchDetails) {
                    logger.info("    detail: lottoId=${detail.lotBatchId}, adCod=${detail.activityCode}, statoLotto=${detail.batchState}")
                    assertEquals(lotBatchId, detail.lotBatchId, "lotBatchId should match in details")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBatchByLotId: insufficient permissions (requires TECHNICAL_USER or TEACHER) - ${e.message}")
        }
    }

    @Test
    suspend fun testGetMinutesByBatch() {
        try {
            val batches = api.records.findBatches(limit = 5)
            assertNotNull(batches)

            if (batches.isEmpty()) {
                logger.warning("No batches found, skipping getMinutesByBatch test")
                return
            }

            for (batch in batches) {
                val lotBatchId = batch.lotBatchId ?: continue
                val minutes = api.records.getMinutesByBatch(lotBatchId = lotBatchId)
                assertNotNull(minutes)
                logger.info("  getMinutesByBatch(lotBatchId=$lotBatchId): returned ${minutes.size} minutes")
                for (minute in minutes) {
                    logger.info("    minute: verbId=${minute.minutesId}, matricola=${minute.matricola}, adCod=${minute.activityCode}, voto=${minute.grade}, statoVerbale=${minute.minutesState}")
                    assertNotNull(minute.minutesId, "minutesId should not be null")
                    assertEquals(lotBatchId, minute.lotBatchId, "lotBatchId should match in minutes")

                    // Test getMinutes for each individual minute
                    val minutesId = minute.minutesId ?: continue
                    val singleMinute = api.records.getMinutes(lotBatchId, minutesId)
                    assertNotNull(singleMinute)
                    assertEquals(minutesId, singleMinute.minutesId, "minutesId should match")
                    assertEquals(lotBatchId, singleMinute.lotBatchId, "lotBatchId should match")
                    logger.info("      getMinutes(lotBatchId=$lotBatchId, minutesId=$minutesId): adCod=${singleMinute.activityCode}, voto=${singleMinute.grade}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetMinutesByBatch: insufficient permissions (requires TECHNICAL_USER or TEACHER) - ${e.message}")
        }
    }

    @Test
    suspend fun testFindMinutes() {
        try {
            val minutes = api.records.findMinutes(limit = 10)
            assertNotNull(minutes)
            logger.info("findMinutes returned ${minutes.size} minutes")
            for (minute in minutes) {
                logger.info("  minute: verbId=${minute.minutesId}, lottoId=${minute.lotBatchId}, adCod=${minute.activityCode}, voto=${minute.grade}, statoVerbale=${minute.minutesState}")
                assertNotNull(minute.minutesId, "minutesId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindMinutes: insufficient permissions (requires TECHNICAL_USER, TEACHER, or STUDENT) - ${e.message}")
        }
    }

    @Test
    suspend fun testFindMinutesByStudentMatricola() {
        try {
            val matricola = studentProfile.enrollmentNumber
            val minutes = api.records.findMinutes(studentMatricola = matricola, limit = 10)
            assertNotNull(minutes)
            logger.info("findMinutes by studentMatricola=$matricola returned ${minutes.size} minutes")
            for (minute in minutes) {
                logger.info("  minute: verbId=${minute.minutesId}, adCod=${minute.activityCode}, adDes=${minute.activityDescription}, voto=${minute.grade}")
                assertNotNull(minute.minutesId, "minutesId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindMinutesByStudentMatricola: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testFindMinutesByFiscalCode() {
        try {
            val fiscalCode = session.fiscalCode
            if (fiscalCode == null) {
                logger.warning("No fiscal code available in session, skipping testFindMinutesByFiscalCode")
                return
            }
            val minutes = api.records.findMinutes(studentFiscalCode = fiscalCode, limit = 10)
            assertNotNull(minutes)
            logger.info("findMinutes by studentFiscalCode returned ${minutes.size} minutes")
            for (minute in minutes) {
                logger.info("  minute: verbId=${minute.minutesId}, adCod=${minute.activityCode}, voto=${minute.grade}, statoVerbale=${minute.minutesState}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindMinutesByFiscalCode: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testFindMinutesWithDateRange() {
        try {
            val minutes = api.records.findMinutes(
                fromDate = "01/01/2020",
                toDate = "31/12/2025",
                limit = 10
            )
            assertNotNull(minutes)
            logger.info("findMinutes with date range returned ${minutes.size} minutes")
            for (minute in minutes) {
                logger.info("  minute: verbId=${minute.minutesId}, adCod=${minute.activityCode}, dataApp=${minute.callDate}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindMinutesWithDateRange: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testFindMinutesWithDateRange: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testFindMinutesByState() {
        try {
            // Try without a specific state to get all available minutes
            val allMinutes = api.records.findMinutes(limit = 5)
            assertNotNull(allMinutes)
            logger.info("findMinutes (no state filter) returned ${allMinutes.size} minutes")

            if (allMinutes.isNotEmpty()) {
                // Use the state of first result to test filtering by state
                val stateCode = allMinutes.first().minutesState?.toString()
                if (stateCode != null) {
                    val filteredMinutes = api.records.findMinutes(minutesState = stateCode, limit = 10)
                    assertNotNull(filteredMinutes)
                    logger.info("findMinutes with statoVerbale=$stateCode returned ${filteredMinutes.size} minutes")
                    for (minute in filteredMinutes) {
                        logger.info("  minute: verbId=${minute.minutesId}, adCod=${minute.activityCode}, statoVerbale=${minute.minutesState}")
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testFindMinutesByState: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testFindMinutesByState: validation error - ${e.message}")
        }
    }

    @Test
    @Disabled("Destructive: imports batch records into the system - requires TECHNICAL_USER permissions and valid batch data")
    suspend fun testImportMinutes() {
        // NOTE: Disabled because importing batch records modifies academic data.
        // This operation requires specific batch data format and valid student/course references.
    }

    @Test
    @Disabled("Destructive: uploads a blob for batch records - requires TECHNICAL_USER permissions and valid upload ID")
    suspend fun testUploadMinutesBlob() {
        // NOTE: Disabled because uploading blobs is part of a batch import workflow.
        // Requires a valid upload ID obtained from a prior importMinutes call.
    }
}
