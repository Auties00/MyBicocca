package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3StructureApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3StructureApiTest::class.java.name)
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
    fun testGetDisciplinaryAreas() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDisciplinaryAreas: calling getDisciplinaryAreas() with defaults")
            val defaultResult = api.structure.getDisciplinaryAreas()
            logger.info("testGetDisciplinaryAreas: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getDisciplinaryAreas() should return non-empty list")
            for (area in defaultResult) {
                logger.info("testGetDisciplinaryAreas: code=${area.disciplinaryAreaCode}, description=${area.disciplinaryAreaDescription}, descriptionEng=${area.disciplinaryAreaDescriptionEnglish}")
            }

            val firstArea = defaultResult.first()
            logger.info("testGetDisciplinaryAreas: calling getDisciplinaryAreas() with disciplinaryAreaDescription filter")
            if (firstArea.disciplinaryAreaDescription != null) {
                val filteredResult = api.structure.getDisciplinaryAreas(
                    disciplinaryAreaDescription = firstArea.disciplinaryAreaDescription
                )
                logger.info("testGetDisciplinaryAreas: filtered by description result size=${filteredResult.size}")
                assertTrue(filteredResult.isNotEmpty(), "Filtered by description should return non-empty list")
            }

            logger.info("testGetDisciplinaryAreas: calling getDisciplinaryAreas() with order param")
            val orderedResult = api.structure.getDisciplinaryAreas(order = "areaDiscDes")
            logger.info("testGetDisciplinaryAreas: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should return non-empty list")
        } else {
            logger.info("testGetDisciplinaryAreas: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getDisciplinaryAreas()
            }
            logger.info("testGetDisciplinaryAreas: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDisciplinaryArea() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDisciplinaryArea: fetching all disciplinary areas first to get a valid code")
            val allAreas = api.structure.getDisciplinaryAreas()
            assertTrue(allAreas.isNotEmpty(), "Need at least one disciplinary area to test single fetch")
            val areaCode = allAreas.first().disciplinaryAreaCode
            logger.info("testGetDisciplinaryArea: calling getDisciplinaryArea() with code=$areaCode")

            val result = api.structure.getDisciplinaryArea(disciplinaryAreaCode = areaCode)
            logger.info("testGetDisciplinaryArea: result code=${result.disciplinaryAreaCode}, description=${result.disciplinaryAreaDescription}, descriptionEng=${result.disciplinaryAreaDescriptionEnglish}")
            assertNotNull(result.disciplinaryAreaCode, "disciplinaryAreaCode should not be null")
            assertTrue(result.disciplinaryAreaCode == areaCode, "Returned area code should match requested code")
        } else {
            logger.info("testGetDisciplinaryArea: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getDisciplinaryArea(disciplinaryAreaCode = "01")
            }
            logger.info("testGetDisciplinaryArea: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCoursesOfStudy() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with defaults")
            val defaultResult = api.structure.getCoursesOfStudy()
            logger.info("testGetCoursesOfStudy: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCoursesOfStudy() should return non-empty list")
            val first = defaultResult.first()
            logger.info("testGetCoursesOfStudy: first courseOfStudyId=${first.courseOfStudyId}, code=${first.courseOfStudyCode}, description=${first.courseOfStudyDescription}, courseTypeCode=${first.courseTypeCode}")

            logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with pagination start=0, limit=5")
            val paginatedResult = api.structure.getCoursesOfStudy(start = 0, limit = 5)
            logger.info("testGetCoursesOfStudy: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            if (first.courseOfStudyCode != null) {
                logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with courseOfStudyCode=${first.courseOfStudyCode}")
                val filteredByCode = api.structure.getCoursesOfStudy(courseOfStudyCode = first.courseOfStudyCode)
                logger.info("testGetCoursesOfStudy: filtered by code result size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by courseOfStudyCode should return non-empty list")
            }

            if (first.courseTypeCode != null) {
                logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with courseTypeCode=${first.courseTypeCode}")
                val filteredByType = api.structure.getCoursesOfStudy(courseTypeCode = first.courseTypeCode)
                logger.info("testGetCoursesOfStudy: filtered by courseTypeCode result size=${filteredByType.size}")
                assertTrue(filteredByType.isNotEmpty(), "Filtered by courseTypeCode should return non-empty list")
            }

            logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with order param")
            val orderedResult = api.structure.getCoursesOfStudy(order = "cdsDes", start = 0, limit = 5)
            logger.info("testGetCoursesOfStudy: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should return non-empty list")

            logger.info("testGetCoursesOfStudy: calling getCoursesOfStudy() with orderActiveFlag=1")
            val activeResult = api.structure.getCoursesOfStudy(orderActiveFlag = 1, start = 0, limit = 5)
            logger.info("testGetCoursesOfStudy: active orders result size=${activeResult.size}")
        } else {
            logger.info("testGetCoursesOfStudy: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCoursesOfStudy()
            }
            logger.info("testGetCoursesOfStudy: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseCharacteristics() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val courses = api.structure.getCoursesOfStudy(start = 0, limit = 1)
            assertTrue(courses.isNotEmpty(), "Need at least one course to test course characteristics")
            val courseCode = courses.first().courseOfStudyCode!!
            logger.info("testGetCourseCharacteristics: calling getCourseCharacteristics() with courseOfStudyCode=$courseCode, aaStart=2024, aaEnd=2025")

            val result = api.structure.getCourseCharacteristics(
                courseOfStudyCode = courseCode,
                academicYearStart = 2024,
                academicYearEnd = 2025
            )
            logger.info("testGetCourseCharacteristics: result size=${result.size}")
            for (char in result) {
                logger.info("testGetCourseCharacteristics: courseOfStudyId=${char.courseOfStudyId}, characteristicId=${char.characteristicId}, title=${char.title}")
            }
        } else {
            logger.info("testGetCourseCharacteristics: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseCharacteristics(
                    courseOfStudyCode = "F1601Q",
                    academicYearStart = 2024,
                    academicYearEnd = 2025
                )
            }
            logger.info("testGetCourseCharacteristics: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCoursePositions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val courses = api.structure.getCoursesOfStudy(start = 0, limit = 1)
            assertTrue(courses.isNotEmpty(), "Need at least one course to test course positions")
            val courseCode = courses.first().courseOfStudyCode!!
            logger.info("testGetCoursePositions: calling getCoursePositions() with courseOfStudyCode=$courseCode, aaStart=2024, aaEnd=2025")

            val result = api.structure.getCoursePositions(
                courseOfStudyCode = courseCode,
                academicYearStart = 2024,
                academicYearEnd = 2025
            )
            logger.info("testGetCoursePositions: result size=${result.size}")
            for (pos in result) {
                logger.info("testGetCoursePositions: courseOfStudyId=${pos.courseOfStudyId}, positionDescription=${pos.positionDescription}, name=${pos.positionName} ${pos.positionSurname}")
            }
        } else {
            logger.info("testGetCoursePositions: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCoursePositions(
                    courseOfStudyCode = "F1601Q",
                    academicYearStart = 2024,
                    academicYearEnd = 2025
                )
            }
            logger.info("testGetCoursePositions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseDeadlines() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val courses = api.structure.getCoursesOfStudy(start = 0, limit = 1)
            assertTrue(courses.isNotEmpty(), "Need at least one course to test course deadlines")
            val courseCode = courses.first().courseOfStudyCode!!
            logger.info("testGetCourseDeadlines: calling getCourseDeadlines() with courseOfStudyCode=$courseCode, aaId=2024")

            val result = api.structure.getCourseDeadlines(
                courseOfStudyCode = courseCode,
                academicYearId = 2024
            )
            logger.info("testGetCourseDeadlines: result size=${result.size}")
            for (deadline in result) {
                logger.info("testGetCourseDeadlines: courseOfStudyId=${deadline.courseOfStudyId}, expirationCode=${deadline.expirationCode}, description=${deadline.expirationDescription}, from=${deadline.dateFrom}, to=${deadline.dateTo}")
            }
        } else {
            logger.info("testGetCourseDeadlines: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseDeadlines(
                    courseOfStudyCode = "F1601Q",
                    academicYearId = 2024
                )
            }
            logger.info("testGetCourseDeadlines: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseTaxes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val courses = api.structure.getCoursesOfStudy(start = 0, limit = 1)
            assertTrue(courses.isNotEmpty(), "Need at least one course to test course taxes")
            val courseCode = courses.first().courseOfStudyCode!!
            logger.info("testGetCourseTaxes: calling getCourseTaxes() with courseOfStudyCode=$courseCode, aaId=2024")

            val result = api.structure.getCourseTaxes(
                courseOfStudyCode = courseCode,
                academicYearId = 2024
            )
            logger.info("testGetCourseTaxes: result size=${result.size}")
            for (tax in result) {
                logger.info("testGetCourseTaxes: courseOfStudyId=${tax.courseOfStudyId}, typology=${tax.typology}, amount=${tax.amount}, expiration=${tax.expiration}")
            }
        } else {
            logger.info("testGetCourseTaxes: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseTaxes(
                    courseOfStudyCode = "F1601Q",
                    academicYearId = 2024
                )
            }
            logger.info("testGetCourseTaxes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseAccessTitles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courses = api.structure.getCoursesOfStudy(start = 0, limit = 1)
            assertTrue(courses.isNotEmpty(), "Need at least one course to test access titles")
            val courseCode = courses.first().courseOfStudyCode!!
            logger.info("testGetCourseAccessTitles: calling getCourseAccessTitles() with courseOfStudyCode=$courseCode, typologyCode=AMM")

            val result = api.structure.getCourseAccessTitles(
                courseOfStudyCode = courseCode,
                typologyCode = "AMM"
            )
            logger.info("testGetCourseAccessTitles: result size=${result.size}")
            for (title in result) {
                logger.info("testGetCourseAccessTitles: courseOfStudyCode=${title.courseOfStudyCode}, typologyCode=${title.typologyCode}, typologyDescription=${title.typologyDescription}, mandatoryTitles=${title.mandatoryTitles.size}, optionalCombinations=${title.optionalCombinations.size}")
            }

            logger.info("testGetCourseAccessTitles: calling getCourseAccessTitles() with typologyCode=IMM")
            val immResult = api.structure.getCourseAccessTitles(
                courseOfStudyCode = courseCode,
                typologyCode = "IMM"
            )
            logger.info("testGetCourseAccessTitles: IMM result size=${immResult.size}")
        } else {
            logger.info("testGetCourseAccessTitles: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseAccessTitles(courseOfStudyCode = "F1601Q", typologyCode = "AMM")
            }
            logger.info("testGetCourseAccessTitles: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseOfStudy() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            logger.info("testGetCourseOfStudy: calling getCourseOfStudy() with courseOfStudyId=$courseOfStudyId")

            val result = api.structure.getCourseOfStudy(courseOfStudyId = courseOfStudyId)
            logger.info("testGetCourseOfStudy: courseOfStudyId=${result.courseOfStudyId}, code=${result.courseOfStudyCode}, description=${result.courseOfStudyDescription}, courseTypeCode=${result.courseTypeCode}")
            assertNotNull(result.courseOfStudyId, "courseOfStudyId should not be null")
            assertTrue(result.courseOfStudyId == courseOfStudyId, "Returned courseOfStudyId should match requested id")

            logger.info("testGetCourseOfStudy: calling getCourseOfStudy() with optionalFields param")
            val resultWithOptFields = api.structure.getCourseOfStudy(
                courseOfStudyId = courseOfStudyId,
                optionalFields = "struttureCorso"
            )
            logger.info("testGetCourseOfStudy: result with optionalFields courseStructures size=${resultWithOptFields.courseStructures.size}")
        } else {
            logger.info("testGetCourseOfStudy: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseOfStudy(courseOfStudyId = 1)
            }
            logger.info("testGetCourseOfStudy: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudyOrders() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            logger.info("testGetStudyOrders: calling getStudyOrders() with courseOfStudyId=$courseOfStudyId")

            val result = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            logger.info("testGetStudyOrders: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getStudyOrders() should return non-empty list")
            for (order in result) {
                logger.info("testGetStudyOrders: courseOfStudyId=${order.courseOfStudyId}, aaOrdId=${order.academicYearOrderId}, orderCode=${order.courseOfStudyOrderCode}, orderDescription=${order.courseOfStudyOrderDescription}, stateCode=${order.stateCode}")
            }

            logger.info("testGetStudyOrders: calling getStudyOrders() with optionalFields param")
            val resultWithOptFields = api.structure.getStudyOrders(
                courseOfStudyId = courseOfStudyId,
                optionalFields = "settoriDottorato"
            )
            logger.info("testGetStudyOrders: result with optionalFields size=${resultWithOptFields.size}")
        } else {
            logger.info("testGetStudyOrders: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getStudyOrders(courseOfStudyId = 1)
            }
            logger.info("testGetStudyOrders: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudyOrder() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            logger.info("testGetStudyOrder: fetching study orders first to get a valid aaOrdId")
            val orders = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            assertTrue(orders.isNotEmpty(), "Need at least one study order to test single fetch")
            val aaOrdId = orders.first().academicYearOrderId
            logger.info("testGetStudyOrder: calling getStudyOrder() with courseOfStudyId=$courseOfStudyId, aaOrdId=$aaOrdId")

            val result = api.structure.getStudyOrder(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId
            )
            logger.info("testGetStudyOrder: courseOfStudyId=${result.courseOfStudyId}, aaOrdId=${result.academicYearOrderId}, orderCode=${result.courseOfStudyOrderCode}, orderDescription=${result.courseOfStudyOrderDescription}, stateCode=${result.stateCode}, durationYears=${result.durationYears}")
            assertNotNull(result.courseOfStudyId, "courseOfStudyId should not be null")
            assertTrue(result.academicYearOrderId == aaOrdId, "Returned aaOrdId should match requested value")

            logger.info("testGetStudyOrder: calling getStudyOrder() with optionalFields param")
            val resultWithOptFields = api.structure.getStudyOrder(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId,
                optionalFields = "settoriDottorato"
            )
            logger.info("testGetStudyOrder: result with optionalFields phdSectors size=${resultWithOptFields.phdSectors.size}")
        } else {
            logger.info("testGetStudyOrder: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getStudyOrder(courseOfStudyId = 1, academicYearOrderId = 2024)
            }
            logger.info("testGetStudyOrder: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPaths() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            val orders = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            assertTrue(orders.isNotEmpty(), "Need at least one study order to test paths")
            val aaOrdId = orders.first().academicYearOrderId
            logger.info("testGetPaths: calling getPaths() with courseOfStudyId=$courseOfStudyId, aaOrdId=$aaOrdId")

            val result = api.structure.getPaths(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId
            )
            logger.info("testGetPaths: result size=${result.size}")
            assertTrue(result.isNotEmpty(), "getPaths() should return non-empty list")
            for (path in result) {
                logger.info("testGetPaths: courseOfStudyId=${path.courseOfStudyId}, aaOrdId=${path.academicYearOrderId}, studyPlanId=${path.studyPlanId}, code=${path.studyPlanCode}, description=${path.studyPlanDescription}, stateCode=${path.stateCode}")
            }
        } else {
            logger.info("testGetPaths: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getPaths(courseOfStudyId = 1, academicYearOrderId = 2024)
            }
            logger.info("testGetPaths: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPath() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            val orders = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            assertTrue(orders.isNotEmpty(), "Need at least one study order to test path")
            val aaOrdId = orders.first().academicYearOrderId
            val paths = api.structure.getPaths(courseOfStudyId = courseOfStudyId, academicYearOrderId = aaOrdId)
            assertTrue(paths.isNotEmpty(), "Need at least one path to test single path fetch")
            val studyPlanId = paths.first().studyPlanId
            logger.info("testGetPath: calling getPath() with courseOfStudyId=$courseOfStudyId, aaOrdId=$aaOrdId, studyPlanId=$studyPlanId")

            val result = api.structure.getPath(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId,
                studyPlanId = studyPlanId
            )
            logger.info("testGetPath: courseOfStudyId=${result.courseOfStudyId}, aaOrdId=${result.academicYearOrderId}, studyPlanId=${result.studyPlanId}, code=${result.studyPlanCode}, description=${result.studyPlanDescription}, stateCode=${result.stateCode}")
            assertNotNull(result.studyPlanId, "studyPlanId should not be null")
            assertTrue(result.studyPlanId == studyPlanId, "Returned studyPlanId should match requested value")
        } else {
            logger.info("testGetPath: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getPath(courseOfStudyId = 1, academicYearOrderId = 2024, studyPlanId = 1)
            }
            logger.info("testGetPath: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAccessTitlesStudyPlanOrder() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            val orders = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            assertTrue(orders.isNotEmpty(), "Need at least one study order")
            val aaOrdId = orders.first().academicYearOrderId
            val paths = api.structure.getPaths(courseOfStudyId = courseOfStudyId, academicYearOrderId = aaOrdId)
            assertTrue(paths.isNotEmpty(), "Need at least one path")
            val studyPlanId = paths.first().studyPlanId
            logger.info("testGetAccessTitlesStudyPlanOrder: calling getAccessTitlesStudyPlanOrder() with courseOfStudyId=$courseOfStudyId, aaOrdId=$aaOrdId, studyPlanId=$studyPlanId, typologyCode=AMM")

            val result = api.structure.getAccessTitlesStudyPlanOrder(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId,
                studyPlanId = studyPlanId,
                typologyCode = "AMM"
            )
            logger.info("testGetAccessTitlesStudyPlanOrder: result size=${result.size}")
            for (title in result) {
                logger.info("testGetAccessTitlesStudyPlanOrder: courseOfStudyId=${title.courseOfStudyId}, typologyCode=${title.typologyCode}, typologyDescription=${title.typologyDescription}, mandatoryTitles=${title.mandatoryTitles.size}")
            }
        } else {
            logger.info("testGetAccessTitlesStudyPlanOrder: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getAccessTitlesStudyPlanOrder(
                    courseOfStudyId = 1,
                    academicYearOrderId = 2024,
                    studyPlanId = 1,
                    typologyCode = "AMM"
                )
            }
            logger.info("testGetAccessTitlesStudyPlanOrder: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAccessTitlesOrder() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            val orders = api.structure.getStudyOrders(courseOfStudyId = courseOfStudyId)
            assertTrue(orders.isNotEmpty(), "Need at least one study order")
            val aaOrdId = orders.first().academicYearOrderId
            logger.info("testGetAccessTitlesOrder: calling getAccessTitlesOrder() with courseOfStudyId=$courseOfStudyId, aaOrdId=$aaOrdId, typologyCode=AMM")

            val result = api.structure.getAccessTitlesOrder(
                courseOfStudyId = courseOfStudyId,
                academicYearOrderId = aaOrdId,
                typologyCode = "AMM"
            )
            logger.info("testGetAccessTitlesOrder: result size=${result.size}")
            for (title in result) {
                logger.info("testGetAccessTitlesOrder: courseOfStudyId=${title.courseOfStudyId}, typologyCode=${title.typologyCode}, typologyDescription=${title.typologyDescription}, mandatoryTitles=${title.mandatoryTitles.size}, optionalCombinations=${title.optionalCombinations.size}")
            }
        } else {
            logger.info("testGetAccessTitlesOrder: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getAccessTitlesOrder(
                    courseOfStudyId = 1,
                    academicYearOrderId = 2024,
                    typologyCode = "AMM"
                )
            }
            logger.info("testGetAccessTitlesOrder: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAccessTitles() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            logger.info("testGetAccessTitles: calling getAccessTitles() with courseOfStudyId=$courseOfStudyId, typologyCode=AMM")

            val result = api.structure.getAccessTitles(
                courseOfStudyId = courseOfStudyId,
                typologyCode = "AMM"
            )
            logger.info("testGetAccessTitles: result size=${result.size}")
            for (title in result) {
                logger.info("testGetAccessTitles: courseOfStudyId=${title.courseOfStudyId}, typologyCode=${title.typologyCode}, typologyDescription=${title.typologyDescription}, mandatoryTitles=${title.mandatoryTitles.size}, optionalCombinations=${title.optionalCombinations.size}")
            }

            logger.info("testGetAccessTitles: calling getAccessTitles() with typologyCode=IMM")
            val immResult = api.structure.getAccessTitles(
                courseOfStudyId = courseOfStudyId,
                typologyCode = "IMM"
            )
            logger.info("testGetAccessTitles: IMM result size=${immResult.size}")
        } else {
            logger.info("testGetAccessTitles: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getAccessTitles(courseOfStudyId = 1, typologyCode = "AMM")
            }
            logger.info("testGetAccessTitles: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDeletedCoursesOfStudy() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDeletedCoursesOfStudy: calling getDeletedCoursesOfStudy() with defaults")
            val defaultResult = api.structure.getDeletedCoursesOfStudy()
            logger.info("testGetDeletedCoursesOfStudy: default result size=${defaultResult.size}")
            for (deleted in defaultResult) {
                logger.info("testGetDeletedCoursesOfStudy: courseOfStudyId=${deleted.courseOfStudyId}, modificationDate=${deleted.courseOfStudyModificationDate}")
            }

            logger.info("testGetDeletedCoursesOfStudy: calling getDeletedCoursesOfStudy() with pagination start=0, limit=5")
            val paginatedResult = api.structure.getDeletedCoursesOfStudy(start = 0, limit = 5)
            logger.info("testGetDeletedCoursesOfStudy: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetDeletedCoursesOfStudy: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getDeletedCoursesOfStudy()
            }
            logger.info("testGetDeletedCoursesOfStudy: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullCourseOfStudy() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val courseOfStudyId = studentProfile.degreeCourseId
            logger.info("testGetFullCourseOfStudy: calling getFullCourseOfStudy() with courseOfStudyId=$courseOfStudyId")

            val result = api.structure.getFullCourseOfStudy(courseOfStudyId = courseOfStudyId)
            logger.info("testGetFullCourseOfStudy: courseOfStudyId=${result.courseOfStudyId}, code=${result.courseOfStudyCode}, description=${result.courseOfStudyDescription}, studyOrdersWithPaths=${result.studyOrdersWithPaths.size}, courseStructures=${result.courseStructures.size}")
            assertNotNull(result.courseOfStudyId, "courseOfStudyId should not be null")

            logger.info("testGetFullCourseOfStudy: calling getFullCourseOfStudy() with orderActiveFlag=1")
            val activeResult = api.structure.getFullCourseOfStudy(
                courseOfStudyId = courseOfStudyId,
                orderActiveFlag = 1
            )
            logger.info("testGetFullCourseOfStudy: active result courseOfStudyId=${activeResult.courseOfStudyId}")
        } else {
            logger.info("testGetFullCourseOfStudy: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getFullCourseOfStudy(courseOfStudyId = studentProfile.degreeCourseId)
            }
            logger.info("testGetFullCourseOfStudy: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetExternalEntities() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetExternalEntities: calling getExternalEntities() with defaults")
            val defaultResult = api.structure.getExternalEntities()
            logger.info("testGetExternalEntities: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getExternalEntities() should return non-empty list")
            val first = defaultResult.first()
            logger.info("testGetExternalEntities: first entityId=${first.entityId}, entityCode=${first.entityCode}, description=${first.description}, entityTypeCode=${first.entityTypeCode}")

            logger.info("testGetExternalEntities: calling getExternalEntities() with pagination start=0, limit=5")
            val paginatedResult = api.structure.getExternalEntities(start = 0, limit = 5)
            logger.info("testGetExternalEntities: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
            assertTrue(paginatedResult.isNotEmpty(), "Paginated result should not be empty")

            logger.info("testGetExternalEntities: calling getExternalEntities() with entityTypeCode=${first.entityTypeCode}")
            val filteredByType = api.structure.getExternalEntities(
                entityTypeCode = first.entityTypeCode,
                start = 0,
                limit = 5
            )
            logger.info("testGetExternalEntities: filtered by entityTypeCode result size=${filteredByType.size}")
            assertTrue(filteredByType.isNotEmpty(), "Filtered by entityTypeCode should return non-empty list")

            logger.info("testGetExternalEntities: calling getExternalEntities() with entityId=${first.entityId.toInt()}")
            val filteredById = api.structure.getExternalEntities(entityId = first.entityId.toInt())
            logger.info("testGetExternalEntities: filtered by entityId result size=${filteredById.size}")
            assertTrue(filteredById.isNotEmpty(), "Filtered by entityId should return non-empty list")
        } else {
            logger.info("testGetExternalEntities: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getExternalEntities()
            }
            logger.info("testGetExternalEntities: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetSites() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetSites: calling getSites() with defaults")
            val defaultResult = api.structure.getSites()
            logger.info("testGetSites: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getSites() should return non-empty list")
            for (site in defaultResult) {
                logger.info("testGetSites: siteId=${site.siteId}, description=${site.siteDescription}, city=${site.city}, province=${site.province}")
            }

            logger.info("testGetSites: calling getSites() with courseOfStudyId=${studentProfile.degreeCourseId}")
            val filteredByCourse = api.structure.getSites(courseOfStudyId = studentProfile.degreeCourseId)
            logger.info("testGetSites: filtered by courseOfStudyId result size=${filteredByCourse.size}")

            val firstSite = defaultResult.first()
            if (firstSite.siteDescription != null) {
                logger.info("testGetSites: calling getSites() with siteDescription=${firstSite.siteDescription}")
                val filteredByDescription = api.structure.getSites(siteDescription = firstSite.siteDescription)
                logger.info("testGetSites: filtered by description result size=${filteredByDescription.size}")
                assertTrue(filteredByDescription.isNotEmpty(), "Filtered by description should return non-empty list")
            }

            logger.info("testGetSites: calling getSites() with order param")
            val orderedResult = api.structure.getSites(order = "sedeDes")
            logger.info("testGetSites: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should return non-empty list")
        } else {
            logger.info("testGetSites: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getSites()
            }
            logger.info("testGetSites: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetSite() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetSite: fetching all sites first to get a valid siteId")
            val allSites = api.structure.getSites()
            assertTrue(allSites.isNotEmpty(), "Need at least one site to test single fetch")
            val siteId = allSites.first().siteId
            logger.info("testGetSite: calling getSite() with siteId=$siteId")

            val result = api.structure.getSite(siteId = siteId)
            logger.info("testGetSite: siteId=${result.siteId}, description=${result.siteDescription}, descriptionEng=${result.siteDescriptionEnglish}, city=${result.city}, province=${result.province}, postalCode=${result.postalCode}, street=${result.street}")
            assertNotNull(result.siteId, "siteId should not be null")
            assertTrue(result.siteId == siteId, "Returned siteId should match requested id")
        } else {
            logger.info("testGetSite: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getSite(siteId = 1)
            }
            logger.info("testGetSite: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDidacticStructures() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDidacticStructures: calling getDidacticStructures() with defaults")
            val defaultResult = api.structure.getDidacticStructures()
            logger.info("testGetDidacticStructures: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getDidacticStructures() should return non-empty list")
            for (structure in defaultResult) {
                logger.info("testGetDidacticStructures: facultyId=${structure.facultyId}, code=${structure.facultyCode}, description=${structure.facultyDescription}, siteType=${structure.siteType}, city=${structure.city}")
            }

            logger.info("testGetDidacticStructures: calling getDidacticStructures() with order param")
            val orderedResult = api.structure.getDidacticStructures(order = "facDes")
            logger.info("testGetDidacticStructures: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should return non-empty list")

            val firstStructure = defaultResult.first()
            if (firstStructure.siteType != null) {
                logger.info("testGetDidacticStructures: calling getDidacticStructures() with siteType=${firstStructure.siteType}")
                val filteredByType = api.structure.getDidacticStructures(siteType = firstStructure.siteType)
                logger.info("testGetDidacticStructures: filtered by siteType result size=${filteredByType.size}")
                assertTrue(filteredByType.isNotEmpty(), "Filtered by siteType should return non-empty list")
            }

            if (firstStructure.facultyCode != null) {
                logger.info("testGetDidacticStructures: calling getDidacticStructures() with facultyCode=${firstStructure.facultyCode}")
                val filteredByCode = api.structure.getDidacticStructures(facultyCode = firstStructure.facultyCode)
                logger.info("testGetDidacticStructures: filtered by facultyCode result size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by facultyCode should return non-empty list")
            }

            logger.info("testGetDidacticStructures: calling getDidacticStructures() with optionalFields param")
            val resultWithOptFields = api.structure.getDidacticStructures(optionalFields = "sediStruttura")
            logger.info("testGetDidacticStructures: result with optionalFields size=${resultWithOptFields.size}")
        } else {
            logger.info("testGetDidacticStructures: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getDidacticStructures()
            }
            logger.info("testGetDidacticStructures: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDidacticStructureById() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDidacticStructureById: fetching all structures first to get a valid facultyId")
            val allStructures = api.structure.getDidacticStructures()
            assertTrue(allStructures.isNotEmpty(), "Need at least one structure to test single fetch")
            val facultyId = allStructures.first().facultyId
            logger.info("testGetDidacticStructureById: calling getDidacticStructures() with facultyId=$facultyId")

            val result = api.structure.getDidacticStructures(facultyId = facultyId)
            logger.info("testGetDidacticStructureById: facultyId=${result.facultyId}, code=${result.facultyCode}, description=${result.facultyDescription}, descriptionEng=${result.facultyDescriptionEnglish}, siteType=${result.siteType}, city=${result.city}")
            assertNotNull(result.facultyId, "facultyId should not be null")
            assertTrue(result.facultyId == facultyId, "Returned facultyId should match requested id")

            logger.info("testGetDidacticStructureById: calling getDidacticStructures() with facultyId=$facultyId and optionalFields")
            val resultWithOptFields = api.structure.getDidacticStructures(
                facultyId = facultyId,
                optionalFields = "sediStruttura"
            )
            logger.info("testGetDidacticStructureById: result with optionalFields structureSites size=${resultWithOptFields.structureSites.size}")
        } else {
            logger.info("testGetDidacticStructureById: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getDidacticStructures(facultyId = 1)
            }
            logger.info("testGetDidacticStructureById: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCourseTypes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetCourseTypes: calling getCourseTypes() with defaults")
            val defaultResult = api.structure.getCourseTypes()
            logger.info("testGetCourseTypes: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getCourseTypes() should return non-empty list")
            for (type in defaultResult) {
                logger.info("testGetCourseTypes: courseTypeCode=${type.courseTypeCode}, description=${type.courseTypeDescription}, descriptionEng=${type.courseTypeDescriptionEnglish}, durationYears=${type.durationYears}, phdFlag=${type.phdFlag}")
            }

            val firstType = defaultResult.first()
            logger.info("testGetCourseTypes: calling getCourseTypes() with courseTypeCode=${firstType.courseTypeCode}")
            val filteredByCode = api.structure.getCourseTypes(courseTypeCode = firstType.courseTypeCode)
            logger.info("testGetCourseTypes: filtered by courseTypeCode result size=${filteredByCode.size}")
            assertTrue(filteredByCode.isNotEmpty(), "Filtered by courseTypeCode should return non-empty list")

            logger.info("testGetCourseTypes: calling getCourseTypes() with order param")
            val orderedResult = api.structure.getCourseTypes(order = "tipoCorsoDes")
            logger.info("testGetCourseTypes: ordered result size=${orderedResult.size}")
            assertTrue(orderedResult.isNotEmpty(), "Ordered result should return non-empty list")

            logger.info("testGetCourseTypes: calling getCourseTypes() with phdFlag=0")
            val nonPhdResult = api.structure.getCourseTypes(phdFlag = 0)
            logger.info("testGetCourseTypes: non-PhD result size=${nonPhdResult.size}")

            if (firstType.tcGroupCode != null) {
                logger.info("testGetCourseTypes: calling getCourseTypes() with tcGroupCode=${firstType.tcGroupCode}")
                val filteredByGroup = api.structure.getCourseTypes(tcGroupCode = firstType.tcGroupCode)
                logger.info("testGetCourseTypes: filtered by tcGroupCode result size=${filteredByGroup.size}")
                assertTrue(filteredByGroup.isNotEmpty(), "Filtered by tcGroupCode should return non-empty list")
            }
        } else {
            logger.info("testGetCourseTypes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.structure.getCourseTypes()
            }
            logger.info("testGetCourseTypes: Esse3Exception thrown as expected")
        }
    }
}
