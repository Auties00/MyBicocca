package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class Esse3TeacherReportingApiTest : Esse3ApiTestBase() {

    private val logger = LoggerFactory.getLogger(Esse3TeacherReportingApiTest::class.java)

    @Test
    suspend fun filterLecturerDiary_noFilter() {
        try {
            val diaries = api.teacherReporting.filterLecturerDiary(limit = 20)
            assertNotNull(diaries)
            logger.info("filterLecturerDiary (no filter): got ${diaries.size} results")
            for (diary in diaries) {
                assertNotNull(diary.diaryId)
                assertNotNull(diary.academicYearId)
                logger.info("  diary id=${diary.diaryId} aaId=${diary.academicYearId} lecturer=${diary.surname} ${diary.name} state=${diary.diaryStateCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerDiary_byLecturerId() {
        // First get a list of lecturers to find a valid lecturerId
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val diaries = api.teacherReporting.filterLecturerDiary(lecturerId = lecturerId, limit = 10)
                assertNotNull(diaries)
                logger.info("filterLecturerDiary (lecturerId=$lecturerId): ${diaries.size} results")
                for (diary in diaries) {
                    assertNotNull(diary.diaryId)
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("filterLecturerDiary(lecturerId=$lecturerId) not authorized: ${e.message}")
            }
            break
        }
    }

    @Test
    suspend fun filterLecturerDiary_byAcademicYear() {
        // Academic year IDs are typically year-based (e.g., 2024 for 2024/2025)
        val currentAcademicYearId = 2024
        try {
            val diaries = api.teacherReporting.filterLecturerDiary(academicYearId = currentAcademicYearId, limit = 10)
            assertNotNull(diaries)
            logger.info("filterLecturerDiary (aaId=$currentAcademicYearId): ${diaries.size} results")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary(aaId=$currentAcademicYearId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerDiary_withPagination() {
        try {
            val firstPage = api.teacherReporting.filterLecturerDiary(start = 0, limit = 5)
            val secondPage = api.teacherReporting.filterLecturerDiary(start = 5, limit = 5)
            assertNotNull(firstPage)
            assertNotNull(secondPage)
            logger.info("filterLecturerDiary page1=${firstPage.size}, page2=${secondPage.size}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary pagination not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerDiary_bySurname() {
        try {
            val diaries = api.teacherReporting.filterLecturerDiary(surname = "Rossi", limit = 10)
            assertNotNull(diaries)
            logger.info("filterLecturerDiary (surname=Rossi): ${diaries.size} results")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary (surname=Rossi) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getLecturerDiary() {
        try {
            val diaries = api.teacherReporting.filterLecturerDiary(limit = 5)
            assertNotNull(diaries)

            if (diaries.isEmpty()) {
                logger.info("getLecturerDiary: no diaries available to query, skipping")
                return
            }

            for (diary in diaries) {
                val diaryId = diary.diaryId
                try {
                    val diaryDetail = api.teacherReporting.getLecturerDiary(diaryId)
                    assertNotNull(diaryDetail)
                    logger.info("getLecturerDiary($diaryId): ${diaryDetail.size} entries")
                    for (detail in diaryDetail) {
                        assertNotNull(detail.diaryId)
                        assertNotNull(detail.academicYearId)
                        logger.info("  detail id=${detail.diaryId} state=${detail.diaryStateCode} hours=${detail.detailedHours}")
                        for (activity in detail.activity) {
                            assertNotNull(activity.diaryDetailId)
                            logger.info("    activity id=${activity.diaryDetailId} type=${activity.activityTypeCode} date=${activity.date}")
                        }
                        for (annualActivity in detail.annualActivities) {
                            assertNotNull(annualActivity.academicYearDiaryDetailId)
                            logger.info("    annualActivity id=${annualActivity.academicYearDiaryDetailId} hours=${annualActivity.hours}")
                        }
                        for (note in detail.notes) {
                            assertNotNull(note.diaryNoteId)
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("getLecturerDiary($diaryId) not authorized: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary (for getLecturerDiary test) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerRegisters_noFilter() {
        try {
            val registers = api.teacherReporting.filterLecturerRegisters(limit = 20)
            assertNotNull(registers)
            logger.info("filterLecturerRegisters (no filter): got ${registers.size} results")
            for (register in registers) {
                assertNotNull(register.registrationId)
                assertNotNull(register.academicYearOfferId)
                assertNotNull(register.lecturerId)
                logger.info("  register id=${register.registrationId} aaOffId=${register.academicYearOfferId} lecturer=${register.surname} ${register.name} state=${register.regulationStateCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerRegisters_byLecturerId() {
        val lecturers = api.teachers.getLecturers(limit = 5)
        assertNotNull(lecturers)

        for (lecturer in lecturers) {
            val lecturerId = lecturer.lecturerId ?: continue
            try {
                val registers = api.teacherReporting.filterLecturerRegisters(lecturerId = lecturerId, limit = 10)
                assertNotNull(registers)
                logger.info("filterLecturerRegisters (lecturerId=$lecturerId): ${registers.size} results")
                for (register in registers) {
                    assertNotNull(register.registrationId)
                    logger.info("  register id=${register.registrationId}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("filterLecturerRegisters(lecturerId=$lecturerId) not authorized: ${e.message}")
            }
            break
        }
    }

    @Test
    suspend fun filterLecturerRegisters_byAcademicYearOffer() {
        val currentAcademicYearOfferId = 2024
        try {
            val registers = api.teacherReporting.filterLecturerRegisters(
                academicYearOfferId = currentAcademicYearOfferId,
                limit = 10
            )
            assertNotNull(registers)
            logger.info("filterLecturerRegisters (aaOffId=$currentAcademicYearOfferId): ${registers.size} results")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters(aaOffId=$currentAcademicYearOfferId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerRegisters_bySurname() {
        try {
            val registers = api.teacherReporting.filterLecturerRegisters(surname = "Rossi", limit = 10)
            assertNotNull(registers)
            logger.info("filterLecturerRegisters (surname=Rossi): ${registers.size} results")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters (surname=Rossi) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerRegisters_withPagination() {
        try {
            val firstPage = api.teacherReporting.filterLecturerRegisters(start = 0, limit = 5)
            val secondPage = api.teacherReporting.filterLecturerRegisters(start = 5, limit = 5)
            assertNotNull(firstPage)
            assertNotNull(secondPage)
            logger.info("filterLecturerRegisters page1=${firstPage.size}, page2=${secondPage.size}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters pagination not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getLecturerRegister() {
        try {
            val registers = api.teacherReporting.filterLecturerRegisters(limit = 5)
            assertNotNull(registers)

            if (registers.isEmpty()) {
                logger.info("getLecturerRegister: no registers available to query, skipping")
                return
            }

            for (register in registers) {
                val registrationId = register.registrationId
                try {
                    val registerDetail = api.teacherReporting.getLecturerRegister(registrationId)
                    assertNotNull(registerDetail)
                    logger.info("getLecturerRegister($registrationId): ${registerDetail.size} entries")
                    for (detail in registerDetail) {
                        assertNotNull(detail.registrationId)
                        assertNotNull(detail.academicYearOfferId)
                        assertNotNull(detail.lecturerId)
                        logger.info("  detail id=${detail.registrationId} state=${detail.regulationStateCode} totalHours=${detail.totalDidacticHours}")
                        for (logEntry in detail.logistics) {
                            logger.info("    logistic cdsId=${logEntry.courseOfStudyId} adId=${logEntry.activityId}")
                        }
                        for (activity in detail.activity) {
                            assertNotNull(activity.ruleDetailId)
                            logger.info("    activity id=${activity.ruleDetailId} type=${activity.activityTypeCode} date=${activity.date} hours=${activity.academicHours}")
                        }
                        for (coverage in detail.coverageIdentifiers) {
                            logger.info("    coverage id=${coverage.coverageId}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("getLecturerRegister($registrationId) not authorized: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters (for getLecturerRegister test) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun getLecturerRegister_withFields() {
        try {
            val registers = api.teacherReporting.filterLecturerRegisters(limit = 1)
            assertNotNull(registers)

            if (registers.isEmpty()) {
                logger.info("getLecturerRegister_withFields: no registers available, skipping")
                return
            }

            val registrationId = registers.first().registrationId
            try {
                val registerDetail = api.teacherReporting.getLecturerRegister(
                    registrationId,
                    fields = "regId,aaOffId,docenteId,cognome,nome,statoRegCod"
                )
                assertNotNull(registerDetail)
                logger.info("getLecturerRegister($registrationId, fields=...): ${registerDetail.size} entries")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warn("getLecturerRegister($registrationId, fields) not authorized: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters (for getLecturerRegister_withFields) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerDiary_andGetDiaryDetails_iterateAll() {
        try {
            val allDiaries = api.teacherReporting.filterLecturerDiary(limit = 50)
            assertNotNull(allDiaries)
            logger.info("filterLecturerDiary (all): ${allDiaries.size} total diaries")

            var processed = 0
            for (diary in allDiaries) {
                val diaryId = diary.diaryId
                assertNotNull(diary.academicYearId)

                try {
                    val details = api.teacherReporting.getLecturerDiary(diaryId)
                    assertNotNull(details)
                    logger.info("  getLecturerDiary($diaryId): ${details.size} detail entries, activities=${details.sumOf { it.activity.size }}")
                    processed++
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("  getLecturerDiary($diaryId) not authorized: ${e.message}")
                }

                if (processed >= 5) break  // Limit deep iteration to avoid excessive network calls
            }
            logger.info("Processed $processed diary detail calls")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerDiary (iterated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun filterLecturerRegisters_andGetRegisterDetails_iterateAll() {
        try {
            val allRegisters = api.teacherReporting.filterLecturerRegisters(limit = 50)
            assertNotNull(allRegisters)
            logger.info("filterLecturerRegisters (all): ${allRegisters.size} total registers")

            var processed = 0
            for (register in allRegisters) {
                val registrationId = register.registrationId
                assertNotNull(register.academicYearOfferId)
                assertNotNull(register.lecturerId)

                try {
                    val details = api.teacherReporting.getLecturerRegister(registrationId)
                    assertNotNull(details)
                    logger.info("  getLecturerRegister($registrationId): ${details.size} detail entries, activities=${details.sumOf { it.activity.size }}")
                    processed++
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warn("  getLecturerRegister($registrationId) not authorized: ${e.message}")
                }

                if (processed >= 5) break  // Limit deep iteration to avoid excessive network calls
            }
            logger.info("Processed $processed register detail calls")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warn("filterLecturerRegisters (iterated) not authorized: ${e.message}")
        }
    }
}
