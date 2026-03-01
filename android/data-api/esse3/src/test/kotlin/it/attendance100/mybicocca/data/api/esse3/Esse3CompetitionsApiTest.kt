package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3CompetitionsApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3CompetitionsApiTest::class.java.name)

    // Known academic year IDs to probe (recent years for Bicocca)
    private val academicYearIds = listOf(2023L, 2022L, 2021L, 2020L)

    @Test
    suspend fun testGetCompetitionsNoFilter() {
        try {
            val competitions = api.competitions.getCompetitions()
            logger.info("getCompetitions (no filter): found ${competitions.size} competitions")
            for (competition in competitions) {
                logger.info(
                    "  competition: aaId=${competition.academicYearId}, testId=${competition.testId}, " +
                        "type=${competition.testTypeCode}, desc=${competition.competitionDescription}"
                )
                assertNotNull(competition.academicYearId, "academicYearId should not be null")
                assertNotNull(competition.testId, "testId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions (no filter) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCompetitionsByAcademicYear() {
        for (aaId in academicYearIds) {
            try {
                val competitions = api.competitions.getCompetitions(academicYearId = aaId)
                logger.info("getCompetitions(aaId=$aaId): found ${competitions.size} competitions")
                for (competition in competitions) {
                    logger.info(
                        "  competition: testId=${competition.testId}, " +
                            "desc=${competition.competitionDescription}, courseType=${competition.courseTypeCode}"
                    )
                    assertNotNull(competition.testId, "testId should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitions(aaId=$aaId) not authorized: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionTests() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionTests: ${e.message}")
            return
        }

        if (competitions.isEmpty()) {
            logger.warning("No competitions found, skipping testGetCompetitionTests")
            return
        }

        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val tests = api.competitions.getCompetitionTests(aaId, testId)
                logger.info("getCompetitionTests(aaId=$aaId, testId=$testId): found ${tests.size} tests")
                for (test in tests) {
                    logger.info(
                        "    test: proveConcId=${test.competitionTestsId}, desc=${test.testDescription}, " +
                            "date=${test.testDate}, subjects=${test.subjects.size}"
                    )
                    assertNotNull(test.academicYearId, "academicYearId should not be null")
                    assertNotNull(test.testId, "testId should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionTests($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionTests($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionTestsWithPagination() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionTestsWithPagination: ${e.message}")
            return
        }

        val first = competitions.firstOrNull { it.academicYearId != null && it.testId != null }
            ?: run {
                logger.warning("No suitable competition found for testGetCompetitionTestsWithPagination")
                return
            }

        val aaId = first.academicYearId!!
        val testId = first.testId!!
        try {
            val tests = api.competitions.getCompetitionTests(aaId, testId, start = 0, limit = 5)
            logger.info("getCompetitionTests($aaId, $testId, paginated): found ${tests.size} tests (max 5)")
            assertTrue(tests.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitionTests($aaId, $testId) paginated not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCompetitionTests($aaId, $testId) paginated validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCompetition() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetition: ${e.message}")
            return
        }

        if (competitions.isEmpty()) {
            logger.warning("No competitions found, skipping testGetCompetition")
            return
        }

        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val details = api.competitions.getCompetition(aaId, testId)
                logger.info("getCompetition(aaId=$aaId, testId=$testId): found ${details.size} entries")
                for (detail in details) {
                    logger.info(
                        "  detail: testType=${detail.testTypeCode}, desc=${detail.competitionDescription}, " +
                            "testDetails=${detail.testDetail.size}, langPrefs=${detail.languagePreferences.size}"
                    )
                    for (testDetail in detail.testDetail) {
                        logger.info(
                            "    testDetail: dettTestId=${testDetail.testDetailId}, cdsId=${testDetail.courseOfStudyId}, " +
                                "totalSeats=${testDetail.totalSeatsNumber}, freeSeats=${testDetail.freeSeatsNumber}"
                        )
                    }
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetition($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetition($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionWithPagination() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionWithPagination: ${e.message}")
            return
        }

        val first = competitions.firstOrNull { it.academicYearId != null && it.testId != null }
            ?: run {
                logger.warning("No suitable competition found for testGetCompetitionWithPagination")
                return
            }

        val aaId = first.academicYearId!!
        val testId = first.testId!!
        try {
            val details = api.competitions.getCompetition(aaId, testId, start = 0, limit = 3)
            logger.info("getCompetition($aaId, $testId, paginated): found ${details.size} entries (max 3)")
            assertTrue(details.size <= 3, "Paginated result should have at most 3 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetition($aaId, $testId) paginated not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCompetition($aaId, $testId) paginated validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCompetitionEnrolled() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionEnrolled: ${e.message}")
            return
        }

        if (competitions.isEmpty()) {
            logger.warning("No competitions found, skipping testGetCompetitionEnrolled")
            return
        }

        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val enrolled = api.competitions.getCompetitionEnrolled(aaId, testId)
                logger.info("getCompetitionEnrolled(aaId=$aaId, testId=$testId): found ${enrolled.size} enrolled")
                for (entry in enrolled) {
                    logger.info(
                        "  enrolled: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "desc=${entry.competitionDescription}, taxPagFlg=${entry.taxPaymentFlag}"
                    )
                    assertNotNull(entry.academicYearId, "academicYearId should not be null")
                    assertNotNull(entry.testId, "testId should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionEnrolledWithPersonIdAndPagination() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionEnrolledWithPersonIdAndPagination: ${e.message}")
            return
        }

        val personId = studentProfile.personId
        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val enrolled = api.competitions.getCompetitionEnrolled(aaId, testId, personId = personId, start = 0, limit = 10)
                logger.info(
                    "getCompetitionEnrolled($aaId, $testId, persId=$personId, paginated): " +
                        "found ${enrolled.size} entries (max 10)"
                )
                assertTrue(enrolled.size <= 10, "Paginated result should have at most 10 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId, persId=$personId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId, persId=$personId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionEnrolledDetail() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionEnrolledDetail: ${e.message}")
            return
        }

        for (competition in competitions.take(5)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val enrolled = api.competitions.getCompetitionEnrolled(aaId, testId)
                val firstEnrolled = enrolled.firstOrNull { it.personId != null && it.positionId != null }
                    ?: continue

                val persId = firstEnrolled.personId!!
                val posId = firstEnrolled.positionId!!
                logger.info("getCompetitionEnrolledDetail(persId=$persId, posId=$posId)")
                try {
                    val detail = api.competitions.getCompetitionEnrolledDetail(persId, posId)
                    logger.info("  getCompetitionEnrolledDetail: found ${detail.size} entries")
                    for (entry in detail) {
                        logger.info(
                            "    detail: persId=${entry.personId}, posId=${entry.positionId}, " +
                                "testType=${entry.testTypeCode}, testDetails=${entry.testDetail.size}, " +
                                "languages=${entry.competitionLanguage.size}"
                        )
                        assertNotNull(entry.personId, "personId should not be null")
                        assertNotNull(entry.positionId, "positionId should not be null")
                    }
                    return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCompetitionEnrolledDetail($persId, $posId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCompetitionEnrolledDetail($persId, $posId) validation error: ${e.message}")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionEnrolled($aaId, $testId) validation error: ${e.message}")
            }
        }
        logger.warning("No enrolled entry found to test getCompetitionEnrolledDetail")
    }

    @Test
    suspend fun testGetCompetitionRankingByGraduatoria() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionRankingByGraduatoria: ${e.message}")
            return
        }

        if (competitions.isEmpty()) {
            logger.warning("No competitions found, skipping testGetCompetitionRankingByGraduatoria")
            return
        }

        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val ranking = api.competitions.getCompetitionRanking(aaId, testId)
                logger.info(
                    "getCompetitionRanking (graduatoria) (aaId=$aaId, testId=$testId): " +
                        "found ${ranking.size} ranking entries"
                )
                for (entry in ranking) {
                    logger.info(
                        "  ranking: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "position=${entry.position}, points=${entry.points}, state=${entry.stateCode}"
                    )
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionRanking (graduatoria) ($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionRanking (graduatoria) ($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionRankingByGraduatoriaWithPersonId() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionRankingByGraduatoriaWithPersonId: ${e.message}")
            return
        }

        val personId = studentProfile.personId
        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val ranking = api.competitions.getCompetitionRanking(aaId, testId, personId = personId)
                logger.info(
                    "getCompetitionRanking (graduatoria, persId=$personId) ($aaId, $testId): " +
                        "found ${ranking.size} ranking entries"
                )
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionRanking (graduatoria, persId=$personId) ($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionRanking (graduatoria, persId=$personId) ($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionRankingByClassifica() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionRankingByClassifica: ${e.message}")
            return
        }

        if (competitions.isEmpty()) {
            logger.warning("No competitions found, skipping testGetCompetitionRankingByClassifica")
            return
        }

        for (competition in competitions.take(3)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                // This overload returns List<Esse3CompetitionRanking> (classificaConcorso endpoint)
                val ranking = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId,
                    personId = null,
                    order = null,
                    start = null,
                    limit = null
                )
                logger.info(
                    "getCompetitionRanking (classifica) (aaId=$aaId, testId=$testId): " +
                        "found ${ranking.size} ranking entries"
                )
                for (entry in ranking) {
                    logger.info(
                        "  classifica entry: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "points=${entry.points}, outcome=${entry.outcomeCode}, subjects=${entry.originTestOutcomes.size}"
                    )
                    assertNotNull(entry.academicYearId, "academicYearId should not be null")
                    assertNotNull(entry.testId, "testId should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionRanking (classifica) ($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionRanking (classifica) ($aaId, $testId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCompetitionRankingByClassificaWithPagination() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionRankingByClassificaWithPagination: ${e.message}")
            return
        }

        val first = competitions.firstOrNull { it.academicYearId != null && it.testId != null }
            ?: run {
                logger.warning("No suitable competition found for testGetCompetitionRankingByClassificaWithPagination")
                return
            }

        val aaId = first.academicYearId!!
        val testId = first.testId!!
        try {
            val ranking = api.competitions.getCompetitionRanking(
                academicYearId = aaId,
                testId = testId,
                personId = null,
                order = null,
                start = 0,
                limit = 5
            )
            logger.info("getCompetitionRanking (classifica, paginated) ($aaId, $testId): found ${ranking.size} entries (max 5)")
            assertTrue(ranking.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitionRanking (classifica, paginated) ($aaId, $testId) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCompetitionRanking (classifica, paginated) ($aaId, $testId) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCompetitionRankingDetail() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetCompetitionRankingDetail: ${e.message}")
            return
        }

        for (competition in competitions.take(5)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val tests = api.competitions.getCompetitionTests(aaId, testId)
                val firstTest = tests.firstOrNull { it.competitionTestsId != null } ?: continue
                val testDetailId = firstTest.competitionTestsId!!

                logger.info("getCompetitionRankingDetail(aaId=$aaId, testId=$testId, testDetailId=$testDetailId)")
                val ranking = api.competitions.getCompetitionRankingDetail(aaId, testId, testDetailId)
                logger.info("  found ${ranking.size} ranking detail entries")
                for (entry in ranking) {
                    logger.info(
                        "  entry: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "position=${entry.position}, points=${entry.points}"
                    )
                }
                return
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getCompetitionRankingDetail for competition ($aaId, $testId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getCompetitionRankingDetail for competition ($aaId, $testId) validation error: ${e.message}")
            }
        }
        logger.warning("No test with competitionTestsId found to test getCompetitionRankingDetail")
    }

    @Test
    suspend fun testGetPersonRankingByGraduatoria() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetPersonRankingByGraduatoria: ${e.message}")
            return
        }

        val personId = studentProfile.personId
        for (competition in competitions.take(5)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val enrolled = api.competitions.getCompetitionEnrolled(aaId, testId, personId = personId)
                val firstEnrolled = enrolled.firstOrNull { it.positionId != null } ?: continue
                val posId = firstEnrolled.positionId!!

                logger.info("getPersonRanking (graduatoria) (persId=$personId, posId=$posId)")
                // This overload returns List<Esse3CompetitionRankingList>
                val ranking = api.competitions.getPersonRanking(personId, posId)
                logger.info("  found ${ranking.size} ranking entries")
                for (entry in ranking) {
                    logger.info(
                        "  entry: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "position=${entry.position}, points=${entry.points}"
                    )
                }
                return
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getPersonRanking (graduatoria) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getPersonRanking (graduatoria) validation error: ${e.message}")
            }
        }
        logger.warning("No competition enrollment found for student to test getPersonRanking (graduatoria)")
    }

    @Test
    suspend fun testGetPersonRankingByClassifica() {
        val competitions = try {
            api.competitions.getCompetitions()
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCompetitions not authorized, skipping testGetPersonRankingByClassifica: ${e.message}")
            return
        }

        val personId = studentProfile.personId
        for (competition in competitions.take(5)) {
            val aaId = competition.academicYearId ?: continue
            val testId = competition.testId ?: continue
            try {
                val enrolled = api.competitions.getCompetitionEnrolled(aaId, testId, personId = personId)
                val firstEnrolled = enrolled.firstOrNull { it.positionId != null } ?: continue
                val posId = firstEnrolled.positionId!!

                logger.info("getPersonRanking (classifica) (persId=$personId, posId=$posId)")
                // This overload returns List<Esse3PersonRanking>
                val ranking = api.competitions.getPersonRanking(personId, posId, order = null)
                logger.info("  found ${ranking.size} ranking entries")
                for (entry in ranking) {
                    logger.info(
                        "  entry: persId=${entry.personId}, posId=${entry.positionId}, " +
                            "points=${entry.points}, outcome=${entry.outcomeCode}, subjects=${entry.originTestOutcome.size}"
                    )
                    assertNotNull(entry.personId, "personId should not be null")
                    assertNotNull(entry.positionId, "positionId should not be null")
                }
                return
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getPersonRanking (classifica) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getPersonRanking (classifica) validation error: ${e.message}")
            }
        }
        logger.warning("No competition enrollment found for student to test getPersonRanking (classifica)")
    }

    @Test
    @Disabled("Admin write op: putCompetitionRanking modifies competition ranking data and cannot be safely reversed")
    suspend fun testPutCompetitionRanking() {
        // Disabled: modifies competition ranking records.
    }

    @Test
    @Disabled("Admin write op: putCompetitionRankingFile uploads a ranking file and cannot be safely reversed")
    suspend fun testPutCompetitionRankingFile() {
        // Disabled: uploads ranking file data to the server.
    }

    @Test
    @Disabled("Admin write op: putCompetitionRankingsFile uploads competition rankings file and cannot be safely reversed")
    suspend fun testPutCompetitionRankingsFile() {
        // Disabled: uploads competition rankings file to the server.
    }

    @Test
    @Disabled("Admin write op: putRankingTestDetail modifies graduation test details and cannot be safely reversed")
    suspend fun testPutRankingTestDetail() {
        // Disabled: modifies ranking test detail records.
    }

    @Test
    @Disabled("Admin write op: putRankingTestDetailFile uploads ranking test detail file and cannot be safely reversed")
    suspend fun testPutRankingTestDetailFile() {
        // Disabled: uploads ranking test detail file data.
    }

    @Test
    @Disabled("Destructive: putCompetitionEnrollment modifies enrollment records and cannot be safely reversed")
    suspend fun testPutCompetitionEnrollment() {
        // Disabled: modifies competition enrollment, which changes system state.
    }
}
