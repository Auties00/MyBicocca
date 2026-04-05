package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Esse3CompetitionsApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3CompetitionsApiTest::class.java.name)
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
    fun testGetCompetitions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCompetitions: calling getCompetitions() with defaults")
            val defaultResult = api.competitions.getCompetitions()
            logger.info("testGetCompetitions: default result size=${defaultResult.size}")

            for (competition in defaultResult) {
                logger.info("testGetCompetitions: competition aaId=${competition.academicYearId}, testId=${competition.testId}, description=${competition.competitionDescription}, testTypeCode=${competition.testTypeCode}")
            }

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.academicYearId != null) {
                    logger.info("testGetCompetitions: calling getCompetitions() with academicYearId=${first.academicYearId}")
                    val filteredByYear = api.competitions.getCompetitions(academicYearId = first.academicYearId)
                    logger.info("testGetCompetitions: filtered by academicYearId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearId should return non-empty list")
                }

                logger.info("testGetCompetitions: calling getCompetitions() with filter param")
                val filteredResult = api.competitions.getCompetitions(filter = "aaId")
                logger.info("testGetCompetitions: filtered result size=${filteredResult.size}")
            }
        } else {
            logger.info("testGetCompetitions: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitions()
            }
            logger.info("testGetCompetitions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionRankingList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            logger.info("testGetCompetitionRankingList: fetched ${competitions.size} competitions to find path params")
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                logger.info("testGetCompetitionRankingList: calling getCompetitionRanking() with aaId=$aaId, testId=$testId (defaults)")
                val defaultResult = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId
                )
                logger.info("testGetCompetitionRankingList: default result size=${defaultResult.size}")

                for (ranking in defaultResult) {
                    logger.info("testGetCompetitionRankingList: ranking testDetailId=${ranking.testDetailId}, personId=${ranking.personId}, positionId=${ranking.positionId}, stateCode=${ranking.stateCode}, position=${ranking.position}, points=${ranking.points}")
                }

                logger.info("testGetCompetitionRankingList: calling getCompetitionRanking() with pagination start=0, limit=5")
                val paginatedResult = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetCompetitionRankingList: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

                if (defaultResult.isNotEmpty()) {
                    val firstRanking = defaultResult.first()
                    if (firstRanking.personId != null) {
                        logger.info("testGetCompetitionRankingList: calling getCompetitionRanking() with personId=${firstRanking.personId}")
                        val filteredResult = api.competitions.getCompetitionRanking(
                            academicYearId = aaId,
                            testId = testId,
                            personId = firstRanking.personId
                        )
                        logger.info("testGetCompetitionRankingList: filtered by personId result size=${filteredResult.size}")
                        assertTrue(filteredResult.isNotEmpty(), "Filtered by personId should return non-empty list")
                    }
                }
            } else {
                logger.info("testGetCompetitionRankingList: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionRankingList: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionRanking(
                    academicYearId = 2024,
                    testId = 1
                )
            }
            logger.info("testGetCompetitionRankingList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionRankingDetail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                val competitionDetails = api.competitions.getCompetition(
                    academicYearId = aaId,
                    testId = testId
                )
                val testDetail = competitionDetails.flatMap { it.testDetail }.firstOrNull { it.testDetailId != null }

                if (testDetail != null) {
                    val testDetailId = testDetail.testDetailId!!
                    logger.info("testGetCompetitionRankingDetail: calling getCompetitionRankingDetail() with aaId=$aaId, testId=$testId, testDetailId=$testDetailId")
                    val defaultResult = api.competitions.getCompetitionRankingDetail(
                        academicYearId = aaId,
                        testId = testId,
                        testDetailId = testDetailId
                    )
                    logger.info("testGetCompetitionRankingDetail: default result size=${defaultResult.size}")

                    for (ranking in defaultResult) {
                        logger.info("testGetCompetitionRankingDetail: ranking personId=${ranking.personId}, positionId=${ranking.positionId}, stateCode=${ranking.stateCode}, position=${ranking.position}, points=${ranking.points}")
                    }

                    logger.info("testGetCompetitionRankingDetail: calling with pagination start=0, limit=5")
                    val paginatedResult = api.competitions.getCompetitionRankingDetail(
                        academicYearId = aaId,
                        testId = testId,
                        testDetailId = testDetailId,
                        start = 0,
                        limit = 5
                    )
                    logger.info("testGetCompetitionRankingDetail: paginated result size=${paginatedResult.size}")
                    assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
                } else {
                    logger.info("testGetCompetitionRankingDetail: no testDetail found with valid testDetailId, skipping")
                }
            } else {
                logger.info("testGetCompetitionRankingDetail: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionRankingDetail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionRankingDetail(
                    academicYearId = 2024,
                    testId = 1,
                    testDetailId = 1
                )
            }
            logger.info("testGetCompetitionRankingDetail: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonRankingList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                val enrolled = api.competitions.getCompetitionEnrolled(
                    academicYearId = aaId,
                    testId = testId
                )
                val enrolledEntry = enrolled.firstOrNull { it.personId != null && it.positionId != null }

                if (enrolledEntry != null) {
                    val personId = enrolledEntry.personId!!
                    val positionId = enrolledEntry.positionId!!
                    logger.info("testGetPersonRankingList: calling getPersonRanking(personId=$personId, positionId=$positionId) for ranking list")
                    val result: List<it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRankingList> = api.competitions.getPersonRanking(
                        personId = personId,
                        positionId = positionId
                    )
                    logger.info("testGetPersonRankingList: result size=${result.size}")

                    for (ranking in result) {
                        logger.info("testGetPersonRankingList: ranking aaId=${ranking.academicYearId}, testId=${ranking.testId}, personId=${ranking.personId}, stateCode=${ranking.stateCode}, position=${ranking.position}, points=${ranking.points}")
                    }
                } else {
                    logger.info("testGetPersonRankingList: no enrolled entry found with valid personId and positionId, skipping")
                }
            } else {
                logger.info("testGetPersonRankingList: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetPersonRankingList: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getPersonRanking(
                    personId = studentProfile.personId,
                    positionId = 1L
                )
            }
            logger.info("testGetPersonRankingList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetition() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                logger.info("testGetCompetition: calling getCompetition() with aaId=$aaId, testId=$testId (defaults)")
                val defaultResult = api.competitions.getCompetition(
                    academicYearId = aaId,
                    testId = testId
                )
                logger.info("testGetCompetition: default result size=${defaultResult.size}")

                for (comp in defaultResult) {
                    logger.info("testGetCompetition: competition aaId=${comp.academicYearId}, testId=${comp.testId}, description=${comp.competitionDescription}, testTypeCode=${comp.testTypeCode}, testMode=${comp.testMode}")
                    logger.info("testGetCompetition: testDetail size=${comp.testDetail.size}, languagePreferences size=${comp.languagePreferences.size}, scholarshipPreferences size=${comp.scholarshipPreferences.size}")
                }

                logger.info("testGetCompetition: calling getCompetition() with pagination start=0, limit=5")
                val paginatedResult = api.competitions.getCompetition(
                    academicYearId = aaId,
                    testId = testId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetCompetition: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            } else {
                logger.info("testGetCompetition: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetition: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetition(
                    academicYearId = 2024,
                    testId = 1
                )
            }
            logger.info("testGetCompetition: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCompetitionRanking() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCompetitionRanking: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCompetitionRanking: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putCompetitionRanking(
                    academicYearId = 2024,
                    testId = 1,
                    competitionTestsId = 1,
                    body = emptyList(),
                    fileTypology = "CSV"
                )
            }
            logger.info("testPutCompetitionRanking: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCompetitionRankingFile() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCompetitionRankingFile: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCompetitionRankingFile: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putCompetitionRankingFile(
                    academicYearId = 2024,
                    testId = 1,
                    competitionTestsId = 1,
                    body = kotlinx.serialization.json.JsonObject(emptyMap()),
                    fileTypology = "CSV"
                )
            }
            logger.info("testPutCompetitionRankingFile: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionRankingClassifica() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                logger.info("testGetCompetitionRankingClassifica: calling getCompetitionRanking() classificaConcorso with aaId=$aaId, testId=$testId (defaults)")
                val defaultResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRanking> = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId,
                    personId = null,
                    order = null,
                    start = null,
                    limit = null
                )
                logger.info("testGetCompetitionRankingClassifica: default result size=${defaultResult.size}")

                for (ranking in defaultResult) {
                    logger.info("testGetCompetitionRankingClassifica: ranking classificationDetailId=${ranking.classificationDetailId}, personId=${ranking.personId}, positionId=${ranking.positionId}, outcomeCode=${ranking.outcomeCode}, points=${ranking.points}")
                }

                logger.info("testGetCompetitionRankingClassifica: calling with pagination start=0, limit=5")
                val paginatedResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRanking> = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId,
                    personId = null,
                    order = null,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetCompetitionRankingClassifica: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

                logger.info("testGetCompetitionRankingClassifica: calling with order=asc")
                val orderedResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRanking> = api.competitions.getCompetitionRanking(
                    academicYearId = aaId,
                    testId = testId,
                    personId = null,
                    order = "asc",
                    start = null,
                    limit = null
                )
                logger.info("testGetCompetitionRankingClassifica: ordered result size=${orderedResult.size}")

                if (defaultResult.isNotEmpty()) {
                    val firstRanking = defaultResult.first()
                    if (firstRanking.personId != null) {
                        logger.info("testGetCompetitionRankingClassifica: calling with personId=${firstRanking.personId}")
                        val filteredResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRanking> = api.competitions.getCompetitionRanking(
                            academicYearId = aaId,
                            testId = testId,
                            personId = firstRanking.personId,
                            order = null,
                            start = null,
                            limit = null
                        )
                        logger.info("testGetCompetitionRankingClassifica: filtered by personId result size=${filteredResult.size}")
                        assertTrue(filteredResult.isNotEmpty(), "Filtered by personId should return non-empty list")
                    }
                }
            } else {
                logger.info("testGetCompetitionRankingClassifica: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionRankingClassifica: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionRanking(
                    academicYearId = 2024,
                    testId = 1,
                    personId = null,
                    order = null,
                    start = null,
                    limit = null
                )
            }
            logger.info("testGetCompetitionRankingClassifica: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCompetitionRankingsFile() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCompetitionRankingsFile: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCompetitionRankingsFile: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putCompetitionRankingsFile(
                    academicYearId = 2024,
                    testId = 1,
                    body = kotlinx.serialization.json.JsonObject(emptyMap())
                )
            }
            logger.info("testPutCompetitionRankingsFile: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutRankingTestDetail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutRankingTestDetail: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutRankingTestDetail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putRankingTestDetail(
                    academicYearId = 2024,
                    testId = 1,
                    testDetailId = 1,
                    body = emptyList()
                )
            }
            logger.info("testPutRankingTestDetail: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutRankingTestDetailFile() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutRankingTestDetailFile: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutRankingTestDetailFile: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putRankingTestDetailFile(
                    academicYearId = 2024,
                    testId = 1,
                    testDetailId = 1,
                    body = kotlinx.serialization.json.JsonObject(emptyMap())
                )
            }
            logger.info("testPutRankingTestDetailFile: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionEnrolled() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                logger.info("testGetCompetitionEnrolled: calling getCompetitionEnrolled() with aaId=$aaId, testId=$testId (defaults)")
                val defaultResult = api.competitions.getCompetitionEnrolled(
                    academicYearId = aaId,
                    testId = testId
                )
                logger.info("testGetCompetitionEnrolled: default result size=${defaultResult.size}")

                for (enrolled in defaultResult) {
                    logger.info("testGetCompetitionEnrolled: enrolled personId=${enrolled.personId}, positionId=${enrolled.positionId}, competitionDescription=${enrolled.competitionDescription}, taxPaymentFlag=${enrolled.taxPaymentFlag}")
                }

                logger.info("testGetCompetitionEnrolled: calling with pagination start=0, limit=5")
                val paginatedResult = api.competitions.getCompetitionEnrolled(
                    academicYearId = aaId,
                    testId = testId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetCompetitionEnrolled: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

                if (defaultResult.isNotEmpty()) {
                    val firstEnrolled = defaultResult.first()
                    if (firstEnrolled.personId != null) {
                        logger.info("testGetCompetitionEnrolled: calling with personId=${firstEnrolled.personId}")
                        val filteredResult = api.competitions.getCompetitionEnrolled(
                            academicYearId = aaId,
                            testId = testId,
                            personId = firstEnrolled.personId
                        )
                        logger.info("testGetCompetitionEnrolled: filtered by personId result size=${filteredResult.size}")
                        assertTrue(filteredResult.isNotEmpty(), "Filtered by personId should return non-empty list")
                    }
                }
            } else {
                logger.info("testGetCompetitionEnrolled: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionEnrolled: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionEnrolled(
                    academicYearId = 2024,
                    testId = 1
                )
            }
            logger.info("testGetCompetitionEnrolled: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCompetitionEnrollment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCompetitionEnrollment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCompetitionEnrollment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.putCompetitionEnrollment(
                    academicYearId = 2024,
                    testId = 1,
                    personId = studentProfile.personId,
                    body = it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionAdmissionInsert()
                )
            }
            logger.info("testPutCompetitionEnrollment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionTests() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                logger.info("testGetCompetitionTests: calling getCompetitionTests() with aaId=$aaId, testId=$testId (defaults)")
                val defaultResult = api.competitions.getCompetitionTests(
                    academicYearId = aaId,
                    testId = testId
                )
                logger.info("testGetCompetitionTests: default result size=${defaultResult.size}")

                for (test in defaultResult) {
                    logger.info("testGetCompetitionTests: test competitionTestsId=${test.competitionTestsId}, description=${test.testDescription}, testDate=${test.testDate}, schedule=${test.schedule}, subjects size=${test.subjects.size}")
                }

                logger.info("testGetCompetitionTests: calling with pagination start=0, limit=5")
                val paginatedResult = api.competitions.getCompetitionTests(
                    academicYearId = aaId,
                    testId = testId,
                    start = 0,
                    limit = 5
                )
                logger.info("testGetCompetitionTests: paginated result size=${paginatedResult.size}")
                assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            } else {
                logger.info("testGetCompetitionTests: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionTests: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionTests(
                    academicYearId = 2024,
                    testId = 1
                )
            }
            logger.info("testGetCompetitionTests: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonRankingClassifica() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                val enrolled = api.competitions.getCompetitionEnrolled(
                    academicYearId = aaId,
                    testId = testId
                )
                val enrolledEntry = enrolled.firstOrNull { it.personId != null && it.positionId != null }

                if (enrolledEntry != null) {
                    val personId = enrolledEntry.personId!!
                    val positionId = enrolledEntry.positionId!!

                    logger.info("testGetPersonRankingClassifica: calling getPersonRanking(personId=$personId, positionId=$positionId) for classifica (defaults)")
                    val defaultResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3PersonRanking> = api.competitions.getPersonRanking(
                        personId = personId,
                        positionId = positionId,
                        order = null
                    )
                    logger.info("testGetPersonRankingClassifica: default result size=${defaultResult.size}")

                    for (ranking in defaultResult) {
                        logger.info("testGetPersonRankingClassifica: ranking personId=${ranking.personId}, positionId=${ranking.positionId}, outcomeCode=${ranking.outcomeCode}, points=${ranking.points}, judgment=${ranking.judgment}, originTestOutcome size=${ranking.originTestOutcome.size}")
                    }

                    logger.info("testGetPersonRankingClassifica: calling with order=asc")
                    val orderedResult: List<it.attendance100.mybicocca.data.dto.esse3.Esse3PersonRanking> = api.competitions.getPersonRanking(
                        personId = personId,
                        positionId = positionId,
                        order = "asc"
                    )
                    logger.info("testGetPersonRankingClassifica: ordered result size=${orderedResult.size}")
                } else {
                    logger.info("testGetPersonRankingClassifica: no enrolled entry found with valid personId and positionId, skipping")
                }
            } else {
                logger.info("testGetPersonRankingClassifica: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetPersonRankingClassifica: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getPersonRanking(
                    personId = studentProfile.personId,
                    positionId = 1L,
                    order = null
                )
            }
            logger.info("testGetPersonRankingClassifica: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCompetitionEnrolledDetail() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val competitions = api.competitions.getCompetitions()
            val competition = competitions.firstOrNull { it.academicYearId != null && it.testId != null }

            if (competition != null) {
                val aaId = competition.academicYearId!!
                val testId = competition.testId!!

                val enrolled = api.competitions.getCompetitionEnrolled(
                    academicYearId = aaId,
                    testId = testId
                )
                val enrolledEntry = enrolled.firstOrNull { it.personId != null && it.positionId != null }

                if (enrolledEntry != null) {
                    val personId = enrolledEntry.personId!!
                    val positionId = enrolledEntry.positionId!!

                    logger.info("testGetCompetitionEnrolledDetail: calling getCompetitionEnrolledDetail(personId=$personId, positionId=$positionId)")
                    val result = api.competitions.getCompetitionEnrolledDetail(
                        personId = personId,
                        positionId = positionId
                    )
                    logger.info("testGetCompetitionEnrolledDetail: result size=${result.size}")

                    for (detail in result) {
                        logger.info("testGetCompetitionEnrolledDetail: detail personId=${detail.personId}, positionId=${detail.positionId}, competitionDescription=${detail.competitionDescription}, testTypeCode=${detail.testTypeCode}, taxPaymentFlag=${detail.taxPaymentFlag}")
                        logger.info("testGetCompetitionEnrolledDetail: languages size=${detail.competitionLanguage.size}, siteDetails size=${detail.siteDetail.size}, pdsDetails size=${detail.pdsDetail.size}, scholarships size=${detail.scholarships.size}, testDetails size=${detail.testDetail.size}")
                    }
                } else {
                    logger.info("testGetCompetitionEnrolledDetail: no enrolled entry found with valid personId and positionId, skipping")
                }
            } else {
                logger.info("testGetCompetitionEnrolledDetail: no competition found with valid aaId and testId, skipping")
            }
        } else {
            logger.info("testGetCompetitionEnrolledDetail: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.competitions.getCompetitionEnrolledDetail(
                    personId = studentProfile.personId,
                    positionId = 1L
                )
            }
            logger.info("testGetCompetitionEnrolledDetail: Esse3Exception thrown as expected")
        }
    }
}
