package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.logging.Logger

class Esse3LogisticsApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3LogisticsApiTest::class.java.name)

    @Test
    suspend fun testGetBuildings() {
        try {
            val buildings = api.logistics.getBuildings()
            logger.info("getBuildings: found ${buildings.size} buildings")
            assertTrue(buildings.isNotEmpty(), "Expected at least one building")
            for (building in buildings) {
                logger.info("  building: id=${building.buildingId}, code=${building.buildingCode}, desc=${building.buildingDescription}, municipality=${building.municipalityDescription}")
                assertNotNull(building.buildingCode, "buildingCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getBuildings not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetBuildingsWithFilter() {
        try {
            val buildings = api.logistics.getBuildings(filter = "Milano")
            logger.info("getBuildings (filter=Milano): found ${buildings.size} buildings")
            for (building in buildings) {
                assertNotNull(building.buildingCode, "buildingCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getBuildings (filter=Milano) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetBuildingById() {
        try {
            val buildings = api.logistics.getBuildings()
            if (buildings.isEmpty()) {
                logger.warning("No buildings found to test getBuilding")
                return
            }
            for (building in buildings) {
                val buildingId = building.buildingId ?: continue
                try {
                    val retrieved = api.logistics.getBuilding(buildingId)
                    logger.info("getBuilding($buildingId): code=${retrieved.buildingCode}, desc=${retrieved.buildingDescription}, street=${retrieved.street}")
                    assertEquals(buildingId, retrieved.buildingId, "buildingId should match")
                    assertNotNull(retrieved.buildingCode, "buildingCode should not be null")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getBuilding($buildingId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getBuilding($buildingId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getBuildings not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetClassroomsForBuilding() {
        try {
            val buildings = api.logistics.getBuildings()
            if (buildings.isEmpty()) {
                logger.warning("No buildings found to test getClassrooms")
                return
            }
            for (building in buildings) {
                val buildingId = building.buildingId ?: continue
                try {
                    val classrooms = api.logistics.getClassrooms(buildingId)
                    logger.info("getClassrooms(buildingId=$buildingId): found ${classrooms.size} classrooms")
                    for (classroom in classrooms) {
                        logger.info("  classroom: id=${classroom.classroomId}, code=${classroom.classroomCode}, desc=${classroom.classroomDescription}, capacity=${classroom.capacity}")
                        assertNotNull(classroom.classroomCode, "classroomCode should not be null")
                        assertEquals(buildingId, classroom.buildingId, "buildingId should match")

                        val classroomId = classroom.classroomId ?: continue
                        try {
                            val retrieved = api.logistics.getClassroom(classroomId, buildingId)
                            logger.info("    getClassroom($classroomId, $buildingId): found ${retrieved.size} entries")
                            for (r in retrieved) {
                                assertNotNull(r.classroomCode, "classroomCode should not be null in retrieved")
                            }
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("    getClassroom($classroomId, $buildingId) not authorized: ${e.message}")
                        } catch (e: Esse3ValidationException) {
                            logger.warning("    getClassroom($classroomId, $buildingId) validation error: ${e.message}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getClassrooms($buildingId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getClassrooms($buildingId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getBuildings not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAllClassrooms() {
        try {
            val classrooms = api.logistics.getAllClassrooms()
            logger.info("getAllClassrooms: found ${classrooms.size} classrooms")
            for (classroom in classrooms) {
                logger.info("  classroom: id=${classroom.classroomId}, code=${classroom.classroomCode}, building=${classroom.buildingCode}, capacity=${classroom.capacity}")
                assertNotNull(classroom.classroomCode, "classroomCode should not be null")
                assertNotNull(classroom.buildingCode, "buildingCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAllClassrooms not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAllClassroomsWithFilters() {
        try {
            val classrooms = api.logistics.getAllClassrooms(order = "aulaCod")
            logger.info("getAllClassrooms (ordered by aulaCod): found ${classrooms.size} classrooms")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAllClassrooms (ordered) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLogistics() {
        try {
            val logistics = api.logistics.getLogistics()
            logger.info("getLogistics: found ${logistics.size} entries")
            for (log in logistics) {
                val partitionKey = log.partitionKey
                logger.info("  log: adLogId=${partitionKey.activityLogId}, aaOffId=${partitionKey.academicYearOfferId}, partCod=${partitionKey.partialCode}")
                assertNotNull(partitionKey.activityLogId, "activityLogId should not be null")
                assertTrue(partitionKey.academicYearOfferId != null && partitionKey.academicYearOfferId > 0, "academicYearOfferId should be positive")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLogisticsWithAcademicYear() {
        try {
            val allLogistics = api.logistics.getLogistics(limit = 5)
            if (allLogistics.isEmpty()) {
                logger.warning("No logistics found to determine academicYearOfferId")
                return
            }
            val aaOffId = allLogistics.first().partitionKey.academicYearOfferId
            val logistics = api.logistics.getLogistics(academicYearOfferId = aaOffId)
            logger.info("getLogistics(aaOffId=$aaOffId): found ${logistics.size} entries")
            for (log in logistics) {
                assertEquals(aaOffId, log.partitionKey.academicYearOfferId, "academicYearOfferId should match")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics (with aaOffId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLogisticsWithPagination() {
        try {
            val logistics = api.logistics.getLogistics(start = 0, limit = 10)
            logger.info("getLogistics (paginated, limit=10): found ${logistics.size} entries")
            assertTrue(logistics.size <= 10, "Paginated result should have at most 10 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetFullLogistics() {
        try {
            val logistics = api.logistics.getLogistics(limit = 5)
            if (logistics.isEmpty()) {
                logger.warning("No logistics found to test getFullLogistics")
                return
            }
            for (log in logistics) {
                val activityLogId = log.partitionKey.activityLogId ?: continue
                try {
                    val fullLogistics = api.logistics.getFullLogistics(activityLogId)
                    logger.info("getFullLogistics($activityLogId): found ${fullLogistics.size} entries")
                    for (full in fullLogistics) {
                        logger.info("  full: partCod=${full.partitionKey?.partialCode}, siteDesc=${full.siteDescription}, UdLogCount=${full.teachingUnitLogWithDetails.size}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getFullLogistics($activityLogId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getFullLogistics($activityLogId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTeachingActivityLogWithSyllabus() {
        try {
            val logistics = api.logistics.getLogistics(limit = 3)
            if (logistics.isEmpty()) {
                logger.warning("No logistics found to test getTeachingActivityLogWithSyllabus")
                return
            }
            for (log in logistics) {
                val activityLogId = log.partitionKey.activityLogId ?: continue
                try {
                    val withSyllabus = api.logistics.getTeachingActivityLogWithSyllabus(activityLogId)
                    logger.info("getTeachingActivityLogWithSyllabus($activityLogId): found ${withSyllabus.size} entries")
                    for (s in withSyllabus) {
                        logger.info("  syllabus entry: partitionKey=${s.partitionKey?.activityLogId}, startDate=${s.startDate}")
                        val syllabusItems = s.syllabusTeachingActivity
                        logger.info("    syllabusAD count=${syllabusItems.size}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTeachingActivityLogWithSyllabus($activityLogId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTeachingActivityLogWithSyllabus($activityLogId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTeachingUnitLogWithDetails() {
        try {
            val logistics = api.logistics.getLogistics(limit = 3)
            if (logistics.isEmpty()) {
                logger.warning("No logistics found to test getTeachingUnitLogWithDetails")
                return
            }
            for (log in logistics) {
                val activityLogId = log.partitionKey.activityLogId ?: continue
                try {
                    val withDetails = api.logistics.getTeachingUnitLogWithDetails(activityLogId)
                    logger.info("getTeachingUnitLogWithDetails($activityLogId): found ${withDetails.size} entries")
                    for (d in withDetails) {
                        logger.info("  detail: udLogId=${d.teachingUnitLogId}, teachingLoad count=${d.teachingLoad.size}, syllabusUD count=${d.syllabusTeachingUnit.size}")
                        for (load in d.teachingLoad) {
                            logger.info("    load: lecturerId=${load.lecturerId}, hours=${load.hours}, fractionCharge=${load.fractionCharge}")
                            assertTrue(load.lecturerId > 0, "lecturerId should be positive")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTeachingUnitLogWithDetails($activityLogId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTeachingUnitLogWithDetails($activityLogId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetFullLogisticsByTeachingActivity() {
        try {
            val logistics = api.logistics.getLogistics(limit = 5)
            if (logistics.isEmpty()) {
                logger.warning("No logistics found to determine academicYearOfferId for getFullLogisticsByTeachingActivity")
                return
            }
            val aaOffId = logistics.first().partitionKey.academicYearOfferId ?: return
            try {
                val fullByAd = api.logistics.getFullLogisticsByTeachingActivity(aaOffId)
                logger.info("getFullLogisticsByTeachingActivity(aaOffId=$aaOffId): found ${fullByAd.size} entries")
                for (entry in fullByAd) {
                    logger.info("  entry: partCod=${entry.partitionKey?.partialCode}, siteDesc=${entry.siteDescription}, UdLogCount=${entry.teachingUnitLogWithDetails.size}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getFullLogisticsByTeachingActivity($aaOffId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getFullLogisticsByTeachingActivity($aaOffId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetFullLogisticsByOd() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getFullLogisticsByOd")
                return
            }
            val offer = offers.first()
            val aaOffId = offer.academicYearOfferId
            val cdsOffId = offer.courseOfStudyOfferId
            try {
                val fullByOd = api.logistics.getFullLogisticsByOd(aaOffId, cdsOffId)
                logger.info("getFullLogisticsByOd(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${fullByOd.size} entries")
                for (entry in fullByOd) {
                    logger.info("  entry: partCod=${entry.partitionKey?.partialCode}, siteDesc=${entry.siteDescription}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getFullLogisticsByOd($aaOffId, $cdsOffId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getFullLogisticsByOd($aaOffId, $cdsOffId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetFullLogisticsByOdStreamed() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getFullLogisticsByOdStreamed")
                return
            }
            val offer = offers.first()
            val aaOffId = offer.academicYearOfferId
            val cdsOffId = offer.courseOfStudyOfferId
            try {
                val streamed = api.logistics.getFullLogisticsByOdStreamed(aaOffId, cdsOffId)
                logger.info("getFullLogisticsByOdStreamed(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${streamed.size} entries")
                for (entry in streamed) {
                    logger.info("  entry: partCod=${entry.partitionKey?.partialCode}, siteDesc=${entry.siteDescription}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getFullLogisticsByOdStreamed($aaOffId, $cdsOffId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getFullLogisticsByOdStreamed($aaOffId, $cdsOffId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetEasystaffLogistics() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            if (offers.isEmpty()) {
                logger.warning("No offers found to determine academicYearOfferId for getEasystaffLogistics")
                return
            }
            val aaOffId = offers.first().academicYearOfferId
            try {
                val easystaffLogistics = api.logistics.getEasystaffLogistics(aaOffId)
                logger.info("getEasystaffLogistics(aaOffId=$aaOffId): found ${easystaffLogistics.size} entries")
                for (entry in easystaffLogistics) {
                    logger.info("  entry: adLogId=${entry.activityLogId}, fatPartCod=${entry.invoicePartialCode}, partCod=${entry.partialCode}")
                    logger.info("    lecturers=${entry.lecturers.size}, activities=${entry.activity.size}")
                    for (activity in entry.activity) {
                        logger.info("      activity: cdsCod=${activity.courseOfStudyCode}, adCod=${activity.activityCode}, adDes=${activity.activityDescription}")
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getEasystaffLogistics($aaOffId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getEasystaffLogistics($aaOffId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetEasystaffStructure() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            if (offers.isEmpty()) {
                logger.warning("No offers found to determine academicYearOfferId for getEasystaffStructure")
                return
            }
            val aaOffId = offers.first().academicYearOfferId
            try {
                val easystaffStructure = api.logistics.getEasystaffStructure(aaOffId)
                logger.info("getEasystaffStructure(aaOffId=$aaOffId): found ${easystaffStructure.size} entries")
                for (entry in easystaffStructure) {
                    logger.info("  entry: cdsCod=${entry.courseOfStudyCode}, cdsDes=${entry.courseOfStudyDescription}, dipCod=${entry.departmentCode}")
                    logger.info("    paths=${entry.paths.size}")
                    for (path in entry.paths) {
                        logger.info("      path: pdsCod=${path.studyPlanCode}, pdsDes=${path.studyPlanDescription}, sedi=${path.sites.size}")
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getEasystaffStructure($aaOffId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getEasystaffStructure($aaOffId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLogisticsByLecturer() {
        try {
            val lecturers = api.teachers.getLecturers(limit = 5)
            if (lecturers.isEmpty()) {
                logger.warning("No lecturers found to test getLogisticsByLecturer")
                return
            }
            for (lecturer in lecturers) {
                val lecturerId = lecturer.lecturerId ?: continue
                try {
                    val logisticsByLecturer = api.logistics.getLogisticsByLecturer(lecturerId = lecturerId)
                    logger.info("getLogisticsByLecturer(lecturerId=$lecturerId): found ${logisticsByLecturer.size} entries")
                    for (entry in logisticsByLecturer) {
                        logger.info("  entry: adLogId=${entry.activityLogId}, adCod=${entry.activityCode}, lecturerId=${entry.lecturerId}")
                        assertEquals(lecturerId, entry.lecturerId, "lecturerId should match")
                    }
                    if (logisticsByLecturer.isNotEmpty()) break
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getLogisticsByLecturer(lecturerId=$lecturerId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getLogisticsByLecturer(lecturerId=$lecturerId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLecturers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLogisticsByLecturerWithPagination() {
        try {
            val entries = api.logistics.getLogisticsByLecturer(start = 0, limit = 5)
            logger.info("getLogisticsByLecturer (paginated, limit=5): found ${entries.size} entries")
            assertTrue(entries.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getLogisticsByLecturer (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableCoverage() {
        try {
            val result = api.logistics.getCancellableCoverage()
            logger.info("getCancellableCoverage: retCode=${result.returnCode}, msg=${result.message}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCancellableCoverage not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCancellableCoverage validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableCoverageWithParams() {
        try {
            val logistics = api.logistics.getLogistics(limit = 3)
            if (logistics.isEmpty()) {
                logger.warning("No logistics found to test getCancellableCoverage with params")
                return
            }
            val log = logistics.first()
            val aaOffId = log.partitionKey.academicYearOfferId
            val result = api.logistics.getCancellableCoverage(academicYearOfferId = aaOffId)
            logger.info("getCancellableCoverage(aaOffId=$aaOffId): retCode=${result.returnCode}, msg=${result.message}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCancellableCoverage (with params) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCancellableCoverage (with params) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetDeletedLogistics() {
        try {
            val deletedLogistics = api.logistics.getDeletedLogistics()
            logger.info("getDeletedLogistics: found ${deletedLogistics.size} deleted entries")
            for (deleted in deletedLogistics) {
                logger.info("  deleted: adLogId=${deleted.activityLogId}, dataModLog=${deleted.logModificationDate}")
                assertTrue(deleted.activityLogId > 0, "activityLogId should be positive")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getDeletedLogistics not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetDeletedLogisticsWithPagination() {
        try {
            val deleted = api.logistics.getDeletedLogistics(start = 0, limit = 5)
            logger.info("getDeletedLogistics (paginated, limit=5): found ${deleted.size} entries")
            assertTrue(deleted.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getDeletedLogistics (paginated) not authorized: ${e.message}")
        }
    }

    @Disabled("putSyllabusTeachingActivity is a mutation operation that modifies syllabus data on a real activity log")
    @Test
    suspend fun testPutSyllabusTeachingActivity() {
        // Disabled: modifies real syllabus data in the ESSE3 system.
    }

    @Disabled("syncSystemLog is an admin operation that syncs the system log from an external source")
    @Test
    suspend fun testSyncSystemLog() {
        // Disabled: admin-only operation that triggers a synchronization from an external system.
    }
}
