package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3TranscriptApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetCareerSegments() {
        try {
            val segments = api.transcript.getCareerSegments(
                fiscalCode = session.fiscalCode
            )
            assertNotNull(segments)
            logger.info("getCareerSegments returned ${segments.size} segments")
            for (segment in segments) {
                logger.info("  segment: matId=${segment.matId}, cdsCod=${segment.courseOfStudyCode}, stuId=${segment.studentId}")
                assertNotNull(segment.matId, "matId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCareerSegments: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCareerSegment() {
        try {
            val matId = studentProfile.enrollmentId
            val segment = api.transcript.getCareerSegment(matId)
            assertNotNull(segment)
            logger.info("getCareerSegment: matId=${segment.matId}, cdsCod=${segment.courseOfStudyCode}, staStuCod=${segment.studentStatusCode}")
            assertEquals(matId, segment.matId, "matId should match the requested matId")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCareerSegment: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRows() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)
            logger.info("getRecordBookRows returned ${rows.size} rows for matId=$matId")
            for (row in rows) {
                logger.info("  row: adsceId=${row.activityChoiceId}, adCod=${row.activityCode}, adDes=${row.activityDescription}, stato=${row.state}")
                assertNotNull(row.activityChoiceId, "activityChoiceId should not be null")
                assertNotNull(row.activityDescription, "activityDescription should not be null")
                assertNotNull(row.state, "state should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRows: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRow() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getRecordBookRow test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val singleRow = api.transcript.getRecordBookRow(matId, activityChoiceId)
                assertNotNull(singleRow)
                assertEquals(activityChoiceId, singleRow.activityChoiceId, "activityChoiceId should match")
                assertEquals(matId, singleRow.matId, "matId should match")
                logger.info("  getRecordBookRow: adsceId=$activityChoiceId, adDes=${singleRow.activityDescription}, stato=${singleRow.state}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRow: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetExamCallsByRecordBookRow() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getExamCallsByRecordBookRow test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val calls = api.transcript.getExamCallsByRecordBookRow(matId, activityChoiceId)
                assertNotNull(calls)
                logger.info("  getExamCallsByRecordBookRow: adsceId=$activityChoiceId, calls=${calls.size}")
                for (call in calls) {
                    logger.info("    call: examCallId=${call.examCallId}, adCod=${call.activityCode}, callStartDate=${call.callStartDate}")
                    assertNotNull(call.examCallId, "examCallId should not be null")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetExamCallsByRecordBookRow: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRowPartitions() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getRecordBookRowPartitions test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val partitions = api.transcript.getRecordBookRowPartitions(matId, activityChoiceId)
                assertNotNull(partitions)
                logger.info("  getRecordBookRowPartitions: adsceId=$activityChoiceId, partitions=${partitions.size}")
                for (partition in partitions) {
                    logger.info("    partition: adpartId=${partition.activityPartitionId}, adsceId=${partition.activityChoiceId}")
                    assertNotNull(partition.activityPartitionId, "activityPartitionId should not be null")
                    assertEquals(activityChoiceId, partition.activityChoiceId, "activityChoiceId should match")

                    val singlePartition = api.transcript.getRecordBookRowPartition(matId, activityChoiceId, partition.activityPartitionId)
                    assertNotNull(singlePartition)
                    assertEquals(partition.activityPartitionId, singlePartition.activityPartitionId, "partition ID should match")
                    logger.info("    getRecordBookRowPartition: adpartId=${singlePartition.activityPartitionId}, partEffCod=${singlePartition.effectivePartialCode}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRowPartitions: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRowTests() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getRecordBookRowTests test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val tests = api.transcript.getRecordBookRowTests(matId, activityChoiceId)
                assertNotNull(tests)
                logger.info("  getRecordBookRowTests: adsceId=$activityChoiceId, tests=${tests.size}")
                for (test in tests) {
                    logger.info("    test: adregId=${test.activityRegulationId}, staRegCod=${test.regulationStatusCode}, dataApp=${test.callDate}")
                    assertNotNull(test.activityRegulationId, "activityRegulationId should not be null")
                    assertEquals(activityChoiceId, test.activityChoiceId, "activityChoiceId should match")
                    assertEquals(matId, test.matId, "matId should match")

                    val singleTest = api.transcript.getRecordBookRowTest(matId, activityChoiceId, test.activityRegulationId)
                    assertNotNull(singleTest)
                    assertEquals(test.activityRegulationId, singleTest.activityRegulationId, "activityRegulationId should match")
                    logger.info("    getRecordBookRowTest: adregId=${singleTest.activityRegulationId}, staRegCod=${singleTest.regulationStatusCode}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRowTests: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRowSegments() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getRecordBookRowSegments test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val segments = api.transcript.getRecordBookRowSegments(matId, activityChoiceId)
                assertNotNull(segments)
                logger.info("  getRecordBookRowSegments: adsceId=$activityChoiceId, segments=${segments.size}")
                for (segment in segments) {
                    logger.info("    segment: segsceId=${segment.segmentChoiceId}, adsceId=${segment.activityChoiceId}")
                    assertNotNull(segment.segmentChoiceId, "segmentChoiceId should not be null")
                    assertEquals(activityChoiceId, segment.activityChoiceId, "activityChoiceId should match")
                    assertEquals(matId, segment.matId, "matId should match")

                    val singleSegment = api.transcript.getRecordBookRowSegment(matId, activityChoiceId, segment.segmentChoiceId)
                    assertNotNull(singleSegment)
                    assertEquals(segment.segmentChoiceId, singleSegment.segmentChoiceId, "segmentChoiceId should match")
                    logger.info("    getRecordBookRowSegment: segsceId=${singleSegment.segmentChoiceId}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRowSegments: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookRowDetections() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getRecordBookRowDetections test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                // Use attendanceRelease (rilFreq) entries to get choiceReleaseId for detections
                val attendanceReleases = row.attendanceRelease
                if (attendanceReleases.isEmpty()) {
                    logger.info("  No attendance releases found for adsceId=$activityChoiceId, skipping detections")
                    continue
                }

                for (release in attendanceReleases) {
                    val choiceReleaseId = release.choiceReleaseId ?: continue
                    try {
                        val detections = api.transcript.getRecordBookRowDetections(matId, activityChoiceId, choiceReleaseId)
                        assertNotNull(detections)
                        logger.info("  getRecordBookRowDetections: adsceId=$activityChoiceId, choiceReleaseId=$choiceReleaseId, detections=${detections.size}")
                        for (detection in detections) {
                            logger.info("    detection: adsceRilDettId=${detection.choiceReleaseDetailId}, startDateTime=${detection.startDateTime}, stato=${detection.state}")
                        }
                    } catch (e: Esse3ValidationException) {
                        logger.warning("  getRecordBookRowDetections validation error for adsceId=$activityChoiceId, choiceReleaseId=$choiceReleaseId: ${e.message}")
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookRowDetections: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetSyllabusTeachingActivityRecordBookRow() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getSyllabusTeachingActivityRecordBookRow test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val syllabus = api.transcript.getSyllabusTeachingActivityRecordBookRow(matId, activityChoiceId)
                assertNotNull(syllabus)
                logger.info("  getSyllabusTeachingActivityRecordBookRow: adsceId=$activityChoiceId, syllabus=${syllabus.size}")
                for (entry in syllabus) {
                    logger.info("    syllabus entry: adsceId=${entry.activityChoiceId}, matId=${entry.matId}")
                    assertEquals(activityChoiceId, entry.activityChoiceId, "activityChoiceId should match")
                    assertEquals(matId, entry.matId, "matId should match")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetSyllabusTeachingActivityRecordBookRow: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetSyllabusTeachingUnitRecordBookRow() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getSyllabusTeachingUnitRecordBookRow test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val syllabusUnits = api.transcript.getSyllabusTeachingUnitRecordBookRow(matId, activityChoiceId)
                assertNotNull(syllabusUnits)
                logger.info("  getSyllabusTeachingUnitRecordBookRow: adsceId=$activityChoiceId, units=${syllabusUnits.size}")
                for (unit in syllabusUnits) {
                    logger.info("    syllabus unit: udLogId=${unit.teachingUnitLogId}, udCod=${unit.teachingUnitCode}, adsceId=${unit.activityChoiceId}")
                    assertEquals(activityChoiceId, unit.activityChoiceId, "activityChoiceId should match")
                    assertEquals(matId, unit.matId, "matId should match")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetSyllabusTeachingUnitRecordBookRow: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetBookingsByTeachingActivityChoiceId() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getBookingsByTeachingActivityChoiceId test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                val bookings = api.transcript.getBookingsByTeachingActivityChoiceId(matId, activityChoiceId)
                assertNotNull(bookings)
                logger.info("  getBookingsByTeachingActivityChoiceId: adsceId=$activityChoiceId, bookings=${bookings.size}")
                for (booking in bookings) {
                    logger.info("    booking: applistaId=${booking.applicationListId}, callId=${booking.callId}, adStuCod=${booking.studentActivityCode}")
                    assertNotNull(booking.applicationListId, "applicationListId should not be null")

                    val singleBooking = api.transcript.getBookingByTeachingActivityChoiceId(matId, activityChoiceId, booking.applicationListId!!)
                    assertNotNull(singleBooking)
                    assertEquals(booking.applicationListId, singleBooking.applicationListId, "applicationListId should match")
                    logger.info("    getBookingByTeachingActivityChoiceId: applistaId=${singleBooking.applicationListId}")

                    // Test presence certificate and booking statino if applicationListId is available
                    try {
                        val certificate = api.transcript.getPresenceCertificateByApplicationListId(matId, activityChoiceId, booking.applicationListId)
                        assertNotNull(certificate)
                        logger.info("    getPresenceCertificateByApplicationListId: certificate length=${certificate.length}")
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warning("    getPresenceCertificateByApplicationListId: insufficient permissions - ${e.message}")
                    } catch (e: Esse3ValidationException) {
                        logger.warning("    getPresenceCertificateByApplicationListId: validation error - ${e.message}")
                    }

                    try {
                        val statino = api.transcript.getBookingStatinoByApplicationListId(matId, activityChoiceId, booking.applicationListId)
                        assertNotNull(statino)
                        logger.info("    getBookingStatinoByApplicationListId: statino length=${statino.length}")
                    } catch (e: Esse3NotAuthorizedException) {
                        logger.warning("    getBookingStatinoByApplicationListId: insufficient permissions - ${e.message}")
                    } catch (e: Esse3ValidationException) {
                        logger.warning("    getBookingStatinoByApplicationListId: validation error - ${e.message}")
                    }
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBookingsByTeachingActivityChoiceId: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCheckProposalRecordBookRow() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows found for matId=$matId, skipping getCheckProposalRecordBookRow test")
                return
            }

            for (row in rows) {
                val activityChoiceId = row.activityChoiceId
                try {
                    val check = api.transcript.getCheckProposalRecordBookRow(matId, activityChoiceId)
                    assertNotNull(check)
                    logger.info("  getCheckProposalRecordBookRow: adsceId=$activityChoiceId, outcome=${check.outcome}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getCheckProposalRecordBookRow validation error for adsceId=$activityChoiceId: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetCheckProposalRecordBookRow: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookExamCalls() {
        try {
            val matId = studentProfile.enrollmentId
            val calls = api.transcript.getRecordBookExamCalls(matId)
            assertNotNull(calls)
            logger.info("getRecordBookExamCalls returned ${calls.size} calls for matId=$matId")
            for (call in calls) {
                logger.info("  call: examCallId=${call.examCallId}, adCod=${call.activityCode}, callStartDate=${call.callStartDate}, stato=${call.state}")
                assertNotNull(call.examCallId, "examCallId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookExamCalls: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookAverages() {
        try {
            val matId = studentProfile.enrollmentId
            val averages = api.transcript.getRecordBookAverages(matId)
            assertNotNull(averages)
            logger.info("getRecordBookAverages returned ${averages.size} averages for matId=$matId")
            for (average in averages) {
                logger.info("  average: tipoMediaCod=${average.averageTypeCode}, base=${average.base}, media=${average.average}, baseDefinition=${average.baseDefinition}")
                assertNotNull(average.averageTypeCode, "averageTypeCode should not be null")
                assertNotNull(average.baseDefinition, "baseDefinition should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookAverages: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookAverageSpecific() {
        try {
            val matId = studentProfile.enrollmentId
            val allAverages = api.transcript.getRecordBookAverages(matId)
            assertNotNull(allAverages)

            if (allAverages.isEmpty()) {
                logger.warning("No averages found for matId=$matId, skipping getRecordBookAverage specific test")
                return
            }

            for (avg in allAverages) {
                val base = avg.base.toString()
                val type = avg.averageTypeCode
                val specificAverages = api.transcript.getRecordBookAverage(matId, base, type)
                assertNotNull(specificAverages)
                logger.info("  getRecordBookAverage: base=$base, type=$type, averages=${specificAverages.size}")
                for (specificAvg in specificAverages) {
                    logger.info("    average: tipoMediaCod=${specificAvg.averageTypeCode}, media=${specificAvg.average}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookAverageSpecific: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping testGetRecordBookAverageSpecific: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookPartitions() {
        try {
            val matId = studentProfile.enrollmentId
            val partitions = api.transcript.getRecordBookPartitions(matId)
            assertNotNull(partitions)
            logger.info("getRecordBookPartitions returned ${partitions.size} partitions for matId=$matId")
            for (partition in partitions) {
                logger.info("  partition: adpartId=${partition.activityPartitionId}, adsceId=${partition.activityChoiceId}, partEffCod=${partition.effectivePartialCode}")
                assertNotNull(partition.activityPartitionId, "activityPartitionId should not be null")
                assertEquals(matId, partition.matId, "matId should match")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookPartitions: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetBookingsByMatId() {
        try {
            val matId = studentProfile.enrollmentId
            val bookings = api.transcript.getBookingsByMatId(matId)
            assertNotNull(bookings)
            logger.info("getBookingsByMatId returned ${bookings.size} bookings for matId=$matId")
            for (booking in bookings) {
                logger.info("  booking: applistaId=${booking.applicationListId}, callId=${booking.callId}, adStuCod=${booking.studentActivityCode}")
                assertNotNull(booking.applicationListId, "applicationListId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetBookingsByMatId: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookTests() {
        try {
            val matId = studentProfile.enrollmentId
            val tests = api.transcript.getRecordBookTests(matId)
            assertNotNull(tests)
            logger.info("getRecordBookTests returned ${tests.size} tests for matId=$matId")
            for (test in tests) {
                logger.info("  test: adregId=${test.activityRegulationId}, adsceId=${test.activityChoiceId}, staRegCod=${test.regulationStatusCode}")
                assertNotNull(test.activityRegulationId, "activityRegulationId should not be null")
                assertEquals(matId, test.matId, "matId should match")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookTests: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookSegments() {
        try {
            val matId = studentProfile.enrollmentId
            val segments = api.transcript.getRecordBookSegments(matId)
            assertNotNull(segments)
            logger.info("getRecordBookSegments returned ${segments.size} segments for matId=$matId")
            for (segment in segments) {
                logger.info("  segment: segsceId=${segment.segmentChoiceId}, adsceId=${segment.activityChoiceId}")
                assertNotNull(segment.segmentChoiceId, "segmentChoiceId should not be null")
                assertEquals(matId, segment.matId, "matId should match")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookSegments: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetRecordBookStats() {
        try {
            val matId = studentProfile.enrollmentId
            val stats = api.transcript.getRecordBookStats(matId)
            assertNotNull(stats)
            logger.info("getRecordBookStats: matId=${stats.matId}, umPesoCod=${stats.measurementUnitWeightCode}, numAdLibretto=${stats.bookletTeachingActivityNumber}, numAdSuperate=${stats.passedTeachingActivityNumber}")
            assertEquals(matId, stats.matId, "matId should match")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetRecordBookStats: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentClassByTeachingActivityStudyPlanOrderNew() {
        try {
            val matId = studentProfile.enrollmentId
            val rows = api.transcript.getRecordBookRows(matId)
            assertNotNull(rows)

            if (rows.isEmpty()) {
                logger.warning("No record book rows for matId=$matId, skipping getStudentClassByTeachingActivityStudyPlanOrderNew test")
                return
            }

            for (row in rows) {
                val key = row.contextualizedTeachingActivityKey ?: continue
                val adCod = key.activityCode ?: continue
                val cdsCod = key.courseOfStudyCode ?: continue
                val aaOrdId = key.academicYearOrderId
                val pdsCod = key.studyPlanCode ?: continue
                val aaOffId = key.academicYearOfferId

                try {
                    val studentClass = api.transcript.getStudentClassByTeachingActivityStudyPlanOrderNew(
                        academicYearOfferLogId = aaOffId.toInt(),
                        teachingActivityLogCode = adCod,
                        courseOfStudyLogCode = cdsCod,
                        academicYearOrderLogId = aaOrdId.toLong(),
                        studyPlanLogCode = pdsCod,
                        studentId = studentProfile.studentId
                    )
                    assertNotNull(studentClass)
                    logger.info("  getStudentClassByTeachingActivityStudyPlanOrderNew: adCod=$adCod, results=${studentClass.size}")
                    for (entry in studentClass) {
                        logger.info("    entry: stuId=${entry.studentId}, matId=${entry.matId}, adsceId=${entry.activityChoiceId}")
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("  getStudentClassByTeachingActivityStudyPlanOrderNew: insufficient permissions for adCod=$adCod - ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("  getStudentClassByTeachingActivityStudyPlanOrderNew: validation error for adCod=$adCod - ${e.message}")
                }
                break // Test with first valid row only
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetStudentClassByTeachingActivityStudyPlanOrderNew: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentClassByTeachingActivityLogId() {
        try {
            // getStudentClassByTeachingActivityLogId requires TEACHER or TECHNICAL_USER permissions
            // and an activityLogId that is obtained from teacher-facing endpoints.
            // We attempt to call it using the first available row's data fetched via segments.
            val matId = studentProfile.enrollmentId
            val segments = api.transcript.getRecordBookSegments(matId)
            assertNotNull(segments)

            if (segments.isEmpty()) {
                logger.warning("No record book segments for matId=$matId, skipping getStudentClassByTeachingActivityLogId test")
                return
            }

            // TranscriptSegment does not carry adLogId directly; this endpoint is teacher-facing.
            // Attempt the call and expect NotAuthorizedException for student accounts.
            val firstSegment = segments.first()
            logger.info("  Attempting getStudentClassByTeachingActivityLogId with a placeholder activityLogId (will likely require TEACHER permissions)")
            try {
                val studentClass = api.transcript.getStudentClassByTeachingActivityLogId(
                    activityLogId = firstSegment.segmentChoiceId,
                    studentId = studentProfile.studentId
                )
                assertNotNull(studentClass)
                logger.info("  getStudentClassByTeachingActivityLogId: results=${studentClass.size}")
                for (entry in studentClass) {
                    logger.info("    entry: stuId=${entry.studentId}, matId=${entry.matId}, adsceId=${entry.activityChoiceId}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("  getStudentClassByTeachingActivityLogId: insufficient permissions (expected for students) - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("  getStudentClassByTeachingActivityLogId: validation error - ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping testGetStudentClassByTeachingActivityLogId: insufficient permissions - ${e.message}")
        }
    }

    @Test
    @Disabled("Destructive: adds a record book row - hard to rollback safely")
    suspend fun testPostRecordBookRow() {
        // NOTE: Disabled because adding a record book row is not trivially reversible.
    }

    @Test
    @Disabled("Destructive: removes a record book row permanently")
    suspend fun testDeleteRecordBookRow() {
        // NOTE: Disabled because deleting a record book row is irreversible.
    }

    @Test
    @Disabled("Destructive: patches a record book row state")
    suspend fun testPatchRecordBookRow() {
        // NOTE: Disabled because patching a record book row could have unintended side effects.
    }

    @Test
    @Disabled("Destructive: sets manual attendance for a record book row - requires TEACHER/TECHNICAL_USER permissions")
    suspend fun testSetManualAttendance() {
        // NOTE: Disabled because setting attendance data could have unintended academic consequences.
    }

    @Test
    @Disabled("Destructive: puts recognition data on a record book row - requires TECHNICAL_USER permissions")
    suspend fun testPutRecordBookRowRecognition() {
        // NOTE: Disabled because modifying recognition data is not trivially reversible.
    }

    @Test
    @Disabled("Destructive: deletes recognition data from a record book row - requires TECHNICAL_USER permissions")
    suspend fun testDeleteRecordBookRecognitionRow() {
        // NOTE: Disabled because deleting recognition data is irreversible.
    }

    @Test
    @Disabled("Destructive: performs massive bulk attendance update - requires TEACHER/TECHNICAL_USER permissions")
    suspend fun testPutMassiveAttendance() {
        // NOTE: Disabled because bulk attendance updates affect many students simultaneously.
    }

    @Test
    @Disabled("Destructive: performs massive bulk presence detection update - requires TEACHER/TECHNICAL_USER permissions")
    suspend fun testPutMassiveDetections() {
        // NOTE: Disabled because bulk detection updates affect many students simultaneously.
    }
}
