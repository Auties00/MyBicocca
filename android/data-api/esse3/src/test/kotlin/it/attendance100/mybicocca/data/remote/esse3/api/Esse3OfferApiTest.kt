package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ActivitiesCountPlansFilters
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UpdateContextualizedActivity
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3OfferApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3OfferApiTest::class.java.name)
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
    fun testGetGroupedTeachingActivities() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetGroupedTeachingActivities: calling with cohortYear=2024")
            val result = api.offer.getGroupedTeachingActivities(cohortYear = 2024)
            logger.info("testGetGroupedTeachingActivities: result size=${result.size}")
            for (group in result) {
                logger.info("testGetGroupedTeachingActivities: activityRaggruppamentoOfferId=${group.activityRaggruppamentoOfferId}, courseOfStudyCode=${group.courseOfStudyCode}, activityCode=${group.activityCode}, cohortYear=${group.cohortYear}, childActivities=${group.childActivities.size}")
            }

            if (result.isNotEmpty()) {
                val first = result.first()
                if (first.courseOfStudyCode != null) {
                    logger.info("testGetGroupedTeachingActivities: calling with courseOfStudyCode=${first.courseOfStudyCode}")
                    val filteredByCds = api.offer.getGroupedTeachingActivities(
                        cohortYear = 2024,
                        courseOfStudyCode = first.courseOfStudyCode
                    )
                    logger.info("testGetGroupedTeachingActivities: filtered by courseOfStudyCode size=${filteredByCds.size}")
                }

                if (first.activityCode != null) {
                    logger.info("testGetGroupedTeachingActivities: calling with activityCode=${first.activityCode}")
                    val filteredByAd = api.offer.getGroupedTeachingActivities(
                        cohortYear = 2024,
                        activityCode = first.activityCode
                    )
                    logger.info("testGetGroupedTeachingActivities: filtered by activityCode size=${filteredByAd.size}")
                }
            }
        } else {
            logger.info("testGetGroupedTeachingActivities: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getGroupedTeachingActivities(cohortYear = 2024)
            }
            logger.info("testGetGroupedTeachingActivities: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetGenericTeachingActivities() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetGenericTeachingActivities: calling with defaults")
            val defaultResult = api.offer.getGenericTeachingActivities()
            logger.info("testGetGenericTeachingActivities: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getGenericTeachingActivities() default should return non-empty list")
            for (activity in defaultResult.take(5)) {
                logger.info("testGetGenericTeachingActivities: activityId=${activity.activityId}, activityCode=${activity.activityCode}, activityDescription=${activity.activityDescription}, offerExistsFlag=${activity.offerExistsFlag}")
            }

            logger.info("testGetGenericTeachingActivities: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getGenericTeachingActivities(start = 0, limit = 5)
            logger.info("testGetGenericTeachingActivities: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            val first = defaultResult.first()
            logger.info("testGetGenericTeachingActivities: calling with activityId=${first.activityId}")
            val filteredById = api.offer.getGenericTeachingActivities(activityId = first.activityId)
            logger.info("testGetGenericTeachingActivities: filtered by activityId size=${filteredById.size}")
            assertTrue(filteredById.isNotEmpty(), "Filtered by activityId should return non-empty list")

            if (first.activityCode != null) {
                logger.info("testGetGenericTeachingActivities: calling with activityCode=${first.activityCode}")
                val filteredByCode = api.offer.getGenericTeachingActivities(activityCode = first.activityCode)
                logger.info("testGetGenericTeachingActivities: filtered by activityCode size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by activityCode should return non-empty list")
            }

            if (first.activityDescription != null) {
                logger.info("testGetGenericTeachingActivities: calling with activityDescription=${first.activityDescription}")
                val filteredByDesc = api.offer.getGenericTeachingActivities(activityDescription = first.activityDescription)
                logger.info("testGetGenericTeachingActivities: filtered by activityDescription size=${filteredByDesc.size}")
                assertTrue(filteredByDesc.isNotEmpty(), "Filtered by activityDescription should return non-empty list")
            }
        } else {
            logger.info("testGetGenericTeachingActivities: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getGenericTeachingActivities()
            }
            logger.info("testGetGenericTeachingActivities: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPartialDomicile() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPartialDomicile: calling with defaults")
            val defaultResult = api.offer.getPartialDomicile()
            logger.info("testGetPartialDomicile: default result size=${defaultResult.size}")
            for (partition in defaultResult.take(5)) {
                logger.info("testGetPartialDomicile: invoicePartialCode=${partition.invoicePartialCode}, invoicePartialDescription=${partition.invoicePartialDescription}, invoiceType=${partition.invoiceType}, partitionDomain size=${partition.partitionDomain.size}")
            }

            logger.info("testGetPartialDomicile: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getPartialDomicile(start = 0, limit = 5)
            logger.info("testGetPartialDomicile: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                if (first.invoicePartialCode != null) {
                    logger.info("testGetPartialDomicile: calling with invoicePartialCode=${first.invoicePartialCode}")
                    val filteredByCode = api.offer.getPartialDomicile(invoicePartialCode = first.invoicePartialCode)
                    logger.info("testGetPartialDomicile: filtered by invoicePartialCode size=${filteredByCode.size}")
                    assertTrue(filteredByCode.isNotEmpty(), "Filtered by invoicePartialCode should return non-empty list")
                }

                if (first.invoicePartialDescription != null) {
                    logger.info("testGetPartialDomicile: calling with invoicePartialDescription=${first.invoicePartialDescription}")
                    val filteredByDesc = api.offer.getPartialDomicile(invoicePartialDescription = first.invoicePartialDescription)
                    logger.info("testGetPartialDomicile: filtered by invoicePartialDescription size=${filteredByDesc.size}")
                    assertTrue(filteredByDesc.isNotEmpty(), "Filtered by invoicePartialDescription should return non-empty list")
                }
            }
        } else {
            logger.info("testGetPartialDomicile: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getPartialDomicile()
            }
            logger.info("testGetPartialDomicile: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInvoicePartial() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInvoicePartial: calling with defaults")
            val defaultResult = api.offer.getInvoicePartial()
            logger.info("testGetInvoicePartial: default result size=${defaultResult.size}")
            for (factor in defaultResult.take(5)) {
                logger.info("testGetInvoicePartial: invoicePartialCode=${factor.invoicePartialCode}, invoicePartialDescription=${factor.invoicePartialDescription}, invoiceType=${factor.invoiceType}")
            }

            logger.info("testGetInvoicePartial: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getInvoicePartial(start = 0, limit = 5)
            logger.info("testGetInvoicePartial: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                logger.info("testGetInvoicePartial: calling with invoicePartialCode=${first.invoicePartialCode}")
                val filteredByCode = api.offer.getInvoicePartial(invoicePartialCode = first.invoicePartialCode)
                logger.info("testGetInvoicePartial: filtered by invoicePartialCode size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by invoicePartialCode should return non-empty list")

                if (first.invoicePartialDescription != null) {
                    logger.info("testGetInvoicePartial: calling with invoicePartialDescription=${first.invoicePartialDescription}")
                    val filteredByDesc = api.offer.getInvoicePartial(invoicePartialDescription = first.invoicePartialDescription)
                    logger.info("testGetInvoicePartial: filtered by invoicePartialDescription size=${filteredByDesc.size}")
                    assertTrue(filteredByDesc.isNotEmpty(), "Filtered by invoicePartialDescription should return non-empty list")
                }
            }
        } else {
            logger.info("testGetInvoicePartial: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getInvoicePartial()
            }
            logger.info("testGetInvoicePartial: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetOffers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetOffers: calling with defaults")
            val defaultResult = api.offer.getOffers()
            logger.info("testGetOffers: default result size=${defaultResult.size}")
            assertTrue(defaultResult.isNotEmpty(), "getOffers() default should return non-empty list")
            for (offer in defaultResult.take(5)) {
                logger.info("testGetOffers: academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}, courseOfStudyCode=${offer.courseOfStudyCode}, courseOfStudyDescription=${offer.courseOfStudyDescription}, courseTypeCode=${offer.courseTypeCode}, departmentCode=${offer.departmentCode}")
            }

            logger.info("testGetOffers: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getOffers(start = 0, limit = 5)
            logger.info("testGetOffers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            val first = defaultResult.first()
            logger.info("testGetOffers: calling with academicYearOfferId=${first.academicYearOfferId}")
            val filteredByYear = api.offer.getOffers(academicYearOfferId = first.academicYearOfferId)
            logger.info("testGetOffers: filtered by academicYearOfferId size=${filteredByYear.size}")
            assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearOfferId should return non-empty list")

            if (first.courseOfStudyCode != null) {
                logger.info("testGetOffers: calling with courseOfStudyCode=${first.courseOfStudyCode}")
                val filteredByCds = api.offer.getOffers(courseOfStudyCode = first.courseOfStudyCode)
                logger.info("testGetOffers: filtered by courseOfStudyCode size=${filteredByCds.size}")
                assertTrue(filteredByCds.isNotEmpty(), "Filtered by courseOfStudyCode should return non-empty list")
            }

            if (first.departmentCode != null) {
                logger.info("testGetOffers: calling with departmentCode=${first.departmentCode}")
                val filteredByDept = api.offer.getOffers(departmentCode = first.departmentCode)
                logger.info("testGetOffers: filtered by departmentCode size=${filteredByDept.size}")
                assertTrue(filteredByDept.isNotEmpty(), "Filtered by departmentCode should return non-empty list")
            }

            if (first.courseTypesCode != null) {
                logger.info("testGetOffers: calling with courseTypesCode=${first.courseTypesCode}")
                val filteredByType = api.offer.getOffers(courseTypesCode = first.courseTypesCode)
                logger.info("testGetOffers: filtered by courseTypesCode size=${filteredByType.size}")
                assertTrue(filteredByType.isNotEmpty(), "Filtered by courseTypesCode should return non-empty list")
            }
        } else {
            logger.info("testGetOffers: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getOffers()
            }
            logger.info("testGetOffers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCancellableOfferTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getCancellableOfferTeachingActivity")
            val offer = offers.first()

            val activities = api.offer.getTeachingActivityOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 1
            )

            if (activities.isNotEmpty()) {
                val activity = activities.first()
                val key = activity.contextualizedTeachingActivityKey
                logger.info("testGetCancellableOfferTeachingActivity: calling with activityFunctionId=${key.activityFunctionId}, academicYearOfferId=${key.academicYearOfferId}, courseOfStudyCode=${key.courseOfStudyCode}, activityCode=${key.activityCode}")
                val result = api.offer.getCancellableOfferTeachingActivity(
                    activityFunctionId = key.activityFunctionId,
                    academicYearOfferId = key.academicYearOfferId.toInt(),
                    courseOfStudyCode = key.courseOfStudyCode,
                    academicYearOrderId = key.academicYearOrderId,
                    studyPlanCode = key.studyPlanCode,
                    activityCode = key.activityCode
                )
                logger.info("testGetCancellableOfferTeachingActivity: returnCode=${result.returnCode}, message=${result.message}")
                assertNotNull(result, "getCancellableOfferTeachingActivity result should not be null")
            } else {
                logger.info("testGetCancellableOfferTeachingActivity: no activities found for offer aaOffId=${offer.academicYearOfferId}, cdsOffId=${offer.courseOfStudyOfferId}")
            }
        } else {
            logger.info("testGetCancellableOfferTeachingActivity: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getCancellableOfferTeachingActivity()
            }
            logger.info("testGetCancellableOfferTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutActivityPlanCount() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutActivityPlanCount: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutActivityPlanCount: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.putActivityPlanCount(
                    body = Esse3ActivitiesCountPlansFilters(
                        courseOfStudyCode = "TEST",
                        academicYearOrderId = 2024,
                        studyPlanCode = "TEST",
                        academicYearOfferId = 2024,
                        activityCode = "TEST"
                    )
                )
            }
            logger.info("testPutActivityPlanCount: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTeachingActivityOffers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getTeachingActivityOffers")
            val offer = offers.first()

            logger.info("testGetTeachingActivityOffers: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getTeachingActivityOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetTeachingActivityOffers: default result size=${defaultResult.size}")
            for (activity in defaultResult.take(5)) {
                val key = activity.contextualizedTeachingActivityKey
                logger.info("testGetTeachingActivityOffers: activityId=${key.activityId}, activityCode=${key.activityCode}, activityDescription=${key.activityDescription}, courseOfStudyCode=${key.courseOfStudyCode}, studyPlanCode=${key.studyPlanCode}")
            }

            logger.info("testGetTeachingActivityOffers: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getTeachingActivityOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 5
            )
            logger.info("testGetTeachingActivityOffers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                val key = first.contextualizedTeachingActivityKey

                if (key.activityCode != null) {
                    logger.info("testGetTeachingActivityOffers: calling with activityCode=${key.activityCode}")
                    val filteredByCode = api.offer.getTeachingActivityOffers(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        activityCode = key.activityCode
                    )
                    logger.info("testGetTeachingActivityOffers: filtered by activityCode size=${filteredByCode.size}")
                    assertTrue(filteredByCode.isNotEmpty(), "Filtered by activityCode should return non-empty list")
                }

                logger.info("testGetTeachingActivityOffers: calling with activityId=${key.activityId}")
                val filteredById = api.offer.getTeachingActivityOffers(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    activityId = key.activityId
                )
                logger.info("testGetTeachingActivityOffers: filtered by activityId size=${filteredById.size}")
                assertTrue(filteredById.isNotEmpty(), "Filtered by activityId should return non-empty list")

                logger.info("testGetTeachingActivityOffers: calling with academicYearOrderId=${key.academicYearOrderId}")
                val filteredByAaOrd = api.offer.getTeachingActivityOffers(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    academicYearOrderId = key.academicYearOrderId
                )
                logger.info("testGetTeachingActivityOffers: filtered by academicYearOrderId size=${filteredByAaOrd.size}")
                assertTrue(filteredByAaOrd.isNotEmpty(), "Filtered by academicYearOrderId should return non-empty list")

                logger.info("testGetTeachingActivityOffers: calling with studyPlanId=${key.studyPlanId}")
                val filteredByPds = api.offer.getTeachingActivityOffers(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    studyPlanId = key.studyPlanId
                )
                logger.info("testGetTeachingActivityOffers: filtered by studyPlanId size=${filteredByPds.size}")
                assertTrue(filteredByPds.isNotEmpty(), "Filtered by studyPlanId should return non-empty list")
            }
        } else {
            logger.info("testGetTeachingActivityOffers: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getTeachingActivityOffers(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetTeachingActivityOffers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCancellableTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getCancellableTeachingActivity")
            val offer = offers.first()

            logger.info("testGetCancellableTeachingActivity: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getCancellableTeachingActivity(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetCancellableTeachingActivity: default result size=${defaultResult.size}")
            for (deletable in defaultResult.take(5)) {
                logger.info("testGetCancellableTeachingActivity: academicYearOfferId=${deletable.academicYearOfferId}, courseOfStudyCode=${deletable.courseOfStudyCode}, activityCode=${deletable.activityCode}, deletable=${deletable.deletable}")
            }

            logger.info("testGetCancellableTeachingActivity: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getCancellableTeachingActivity(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 5
            )
            logger.info("testGetCancellableTeachingActivity: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                logger.info("testGetCancellableTeachingActivity: calling with activityCode=${first.activityCode}")
                val filteredByCode = api.offer.getCancellableTeachingActivity(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    activityCode = first.activityCode
                )
                logger.info("testGetCancellableTeachingActivity: filtered by activityCode size=${filteredByCode.size}")
                assertTrue(filteredByCode.isNotEmpty(), "Filtered by activityCode should return non-empty list")
            }
        } else {
            logger.info("testGetCancellableTeachingActivity: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getCancellableTeachingActivity(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetCancellableTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPatchContextualizedTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPatchContextualizedTeachingActivity: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPatchContextualizedTeachingActivity: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.patchContextualizedTeachingActivity(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1,
                    academicYearOrderOfferId = 1,
                    studyPlanOfferId = 1,
                    activityOfferId = 1,
                    body = Esse3UpdateContextualizedActivity(
                        moodleCourseUrl = "https://example.com/test"
                    )
                )
            }
            logger.info("testPatchContextualizedTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetLecturersByTeachingUnit() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getLecturersByTeachingUnit")
            val offer = offers.first()

            logger.info("testGetLecturersByTeachingUnit: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getLecturersByTeachingUnit(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetLecturersByTeachingUnit: default result size=${defaultResult.size}")
            for (teacher in defaultResult.take(5)) {
                logger.info("testGetLecturersByTeachingUnit: lecturerId=${teacher.lecturerId}, lecturerMatricola=${teacher.lecturerMatricola}, lecturerName=${teacher.lecturerName}, lecturerSurname=${teacher.lecturerSurname}, coverageTypeCode=${teacher.coverageTypeCode}")
            }

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()

                if (first.lecturerSurname != null) {
                    logger.info("testGetLecturersByTeachingUnit: calling with lecturerSurname=${first.lecturerSurname}")
                    val filteredBySurname = api.offer.getLecturersByTeachingUnit(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        lecturerSurname = first.lecturerSurname
                    )
                    logger.info("testGetLecturersByTeachingUnit: filtered by lecturerSurname size=${filteredBySurname.size}")
                    assertTrue(filteredBySurname.isNotEmpty(), "Filtered by lecturerSurname should return non-empty list")
                }

                if (first.lecturerMatricola != null) {
                    logger.info("testGetLecturersByTeachingUnit: calling with lecturerMatricola=${first.lecturerMatricola}")
                    val filteredByMatricola = api.offer.getLecturersByTeachingUnit(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        lecturerMatricola = first.lecturerMatricola
                    )
                    logger.info("testGetLecturersByTeachingUnit: filtered by lecturerMatricola size=${filteredByMatricola.size}")
                    assertTrue(filteredByMatricola.isNotEmpty(), "Filtered by lecturerMatricola should return non-empty list")
                }

                val udKey = first.contextualizedTeachingUnitKey
                if (udKey.teachingUnitCode != null) {
                    logger.info("testGetLecturersByTeachingUnit: calling with teachingUnitCode=${udKey.teachingUnitCode}")
                    val filteredByUd = api.offer.getLecturersByTeachingUnit(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        teachingUnitCode = udKey.teachingUnitCode
                    )
                    logger.info("testGetLecturersByTeachingUnit: filtered by teachingUnitCode size=${filteredByUd.size}")
                    assertTrue(filteredByUd.isNotEmpty(), "Filtered by teachingUnitCode should return non-empty list")
                }

                val adKey = udKey.contextualizedTeachingActivityKey
                if (adKey.activityCode != null) {
                    logger.info("testGetLecturersByTeachingUnit: calling with activityCode=${adKey.activityCode}")
                    val filteredByAd = api.offer.getLecturersByTeachingUnit(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        activityCode = adKey.activityCode
                    )
                    logger.info("testGetLecturersByTeachingUnit: filtered by activityCode size=${filteredByAd.size}")
                    assertTrue(filteredByAd.isNotEmpty(), "Filtered by activityCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetLecturersByTeachingUnit: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getLecturersByTeachingUnit(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetLecturersByTeachingUnit: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTeachingUnitOffers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getTeachingUnitOffers")
            val offer = offers.first()

            logger.info("testGetTeachingUnitOffers: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getTeachingUnitOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetTeachingUnitOffers: default result size=${defaultResult.size}")
            for (unit in defaultResult.take(5)) {
                val key = unit.contextualizedTeachingUnitKey
                logger.info("testGetTeachingUnitOffers: teachingUnitId=${key.teachingUnitId}, teachingUnitCode=${key.teachingUnitCode}, teachingUnitDescription=${key.teachingUnitDescription}, teachingUnitTypeCode=${unit.teachingUnitTypeCode}")
            }

            logger.info("testGetTeachingUnitOffers: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getTeachingUnitOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 5
            )
            logger.info("testGetTeachingUnitOffers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                val key = first.contextualizedTeachingUnitKey
                val adKey = key.contextualizedTeachingActivityKey

                if (adKey.activityCode != null) {
                    logger.info("testGetTeachingUnitOffers: calling with activityCode=${adKey.activityCode}")
                    val filteredByAd = api.offer.getTeachingUnitOffers(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        activityCode = adKey.activityCode
                    )
                    logger.info("testGetTeachingUnitOffers: filtered by activityCode size=${filteredByAd.size}")
                    assertTrue(filteredByAd.isNotEmpty(), "Filtered by activityCode should return non-empty list")
                }

                logger.info("testGetTeachingUnitOffers: calling with activityId=${adKey.activityId}")
                val filteredById = api.offer.getTeachingUnitOffers(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    activityId = adKey.activityId
                )
                logger.info("testGetTeachingUnitOffers: filtered by activityId size=${filteredById.size}")
                assertTrue(filteredById.isNotEmpty(), "Filtered by activityId should return non-empty list")

                logger.info("testGetTeachingUnitOffers: calling with studyPlanId=${adKey.studyPlanId}")
                val filteredByPds = api.offer.getTeachingUnitOffers(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    studyPlanId = adKey.studyPlanId
                )
                logger.info("testGetTeachingUnitOffers: filtered by studyPlanId size=${filteredByPds.size}")
                assertTrue(filteredByPds.isNotEmpty(), "Filtered by studyPlanId should return non-empty list")
            }
        } else {
            logger.info("testGetTeachingUnitOffers: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getTeachingUnitOffers(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetTeachingUnitOffers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetCancellableTeachingUnit() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getCancellableTeachingUnit")
            val offer = offers.first()

            logger.info("testGetCancellableTeachingUnit: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getCancellableTeachingUnit(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetCancellableTeachingUnit: default result size=${defaultResult.size}")
            for (deletable in defaultResult.take(5)) {
                logger.info("testGetCancellableTeachingUnit: academicYearOfferId=${deletable.academicYearOfferId}, courseOfStudyCode=${deletable.courseOfStudyCode}, activityCode=${deletable.activityCode}, teachingUnitCode=${deletable.teachingUnitCode}, deletable=${deletable.deletable}")
            }

            logger.info("testGetCancellableTeachingUnit: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getCancellableTeachingUnit(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 5
            )
            logger.info("testGetCancellableTeachingUnit: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                logger.info("testGetCancellableTeachingUnit: calling with activityCode=${first.activityCode}")
                val filteredByAd = api.offer.getCancellableTeachingUnit(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    activityCode = first.activityCode
                )
                logger.info("testGetCancellableTeachingUnit: filtered by activityCode size=${filteredByAd.size}")
                assertTrue(filteredByAd.isNotEmpty(), "Filtered by activityCode should return non-empty list")

                if (first.teachingUnitCode != null) {
                    logger.info("testGetCancellableTeachingUnit: calling with teachingUnitCode=${first.teachingUnitCode}")
                    val filteredByUd = api.offer.getCancellableTeachingUnit(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        teachingUnitCode = first.teachingUnitCode
                    )
                    logger.info("testGetCancellableTeachingUnit: filtered by teachingUnitCode size=${filteredByUd.size}")
                    assertTrue(filteredByUd.isNotEmpty(), "Filtered by teachingUnitCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetCancellableTeachingUnit: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getCancellableTeachingUnit(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetCancellableTeachingUnit: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetOfferedSEG() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getOfferedSEG")
            val offer = offers.first()

            logger.info("testGetOfferedSEG: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val defaultResult = api.offer.getOfferedSEG(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetOfferedSEG: default result size=${defaultResult.size}")
            for (segment in defaultResult.take(5)) {
                val key = segment.contextualizedSecretKey
                logger.info("testGetOfferedSEG: segmentId=${key.segmentId}, sectorCode=${segment.sectorCode}, weight=${segment.weight}, teachingActivityTypeCode=${segment.teachingActivityTypeCode}")
            }

            logger.info("testGetOfferedSEG: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getOfferedSEG(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 5
            )
            logger.info("testGetOfferedSEG: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                val udKey = first.contextualizedSecretKey.contextualizedTeachingUnitKey
                val adKey = udKey.contextualizedTeachingActivityKey

                if (adKey.activityCode != null) {
                    logger.info("testGetOfferedSEG: calling with activityCode=${adKey.activityCode}")
                    val filteredByAd = api.offer.getOfferedSEG(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        activityCode = adKey.activityCode
                    )
                    logger.info("testGetOfferedSEG: filtered by activityCode size=${filteredByAd.size}")
                    assertTrue(filteredByAd.isNotEmpty(), "Filtered by activityCode should return non-empty list")
                }

                if (udKey.teachingUnitCode != null) {
                    logger.info("testGetOfferedSEG: calling with teachingUnitCode=${udKey.teachingUnitCode}")
                    val filteredByUd = api.offer.getOfferedSEG(
                        academicYearOfferId = offer.academicYearOfferId,
                        courseOfStudyOfferId = offer.courseOfStudyOfferId,
                        teachingUnitCode = udKey.teachingUnitCode
                    )
                    logger.info("testGetOfferedSEG: filtered by teachingUnitCode size=${filteredByUd.size}")
                    assertTrue(filteredByUd.isNotEmpty(), "Filtered by teachingUnitCode should return non-empty list")
                }

                logger.info("testGetOfferedSEG: calling with studyPlanId=${adKey.studyPlanId}")
                val filteredByPds = api.offer.getOfferedSEG(
                    academicYearOfferId = offer.academicYearOfferId,
                    courseOfStudyOfferId = offer.courseOfStudyOfferId,
                    studyPlanId = adKey.studyPlanId
                )
                logger.info("testGetOfferedSEG: filtered by studyPlanId size=${filteredByPds.size}")
                assertTrue(filteredByPds.isNotEmpty(), "Filtered by studyPlanId should return non-empty list")
            }
        } else {
            logger.info("testGetOfferedSEG: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getOfferedSEG(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetOfferedSEG: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetContextualizedTeachingActivity() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getContextualizedTeachingActivity")
            val offer = offers.first()

            val activities = api.offer.getTeachingActivityOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId,
                start = 0,
                limit = 1
            )

            if (activities.isNotEmpty()) {
                val activity = activities.first()
                val key = activity.contextualizedTeachingActivityKey

                logger.info("testGetContextualizedTeachingActivity: calling with academicYearOfferId=${key.academicYearOfferId}, courseOfStudyId=${key.courseOfStudyId}, academicYearOrderId=${key.academicYearOrderId}, studyPlanId=${key.studyPlanId}, activityId=${key.activityId}")
                val result = api.offer.getContextualizedTeachingActivity(
                    academicYearOfferId = key.academicYearOfferId.toInt(),
                    courseOfStudyOfferId = key.courseOfStudyId,
                    academicYearOrderOfferId = key.academicYearOrderId.toLong(),
                    studyPlanOfferId = key.studyPlanId,
                    activityOfferId = key.activityId
                )
                logger.info("testGetContextualizedTeachingActivity: result activityCode=${result.contextualizedTeachingActivityKey.activityCode}, activityDescription=${result.contextualizedTeachingActivityKey.activityDescription}")
                assertNotNull(result.contextualizedTeachingActivityKey, "contextualizedTeachingActivityKey should not be null")
                logger.info("testGetContextualizedTeachingActivity: graduationTypeCode=${result.graduationTypeCode}, evaluationTypeCode=${result.evaluationTypeCode}, teachingLanguageDescription=${result.teachingLanguageDescription}")
            } else {
                logger.info("testGetContextualizedTeachingActivity: no activities found for offer aaOffId=${offer.academicYearOfferId}, cdsOffId=${offer.courseOfStudyOfferId}, skipping detailed test")
            }
        } else {
            logger.info("testGetContextualizedTeachingActivity: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getContextualizedTeachingActivity(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1,
                    academicYearOrderOfferId = 1,
                    studyPlanOfferId = 1,
                    activityOfferId = 1
                )
            }
            logger.info("testGetContextualizedTeachingActivity: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetDeletedOffers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetDeletedOffers: calling with defaults")
            val defaultResult = api.offer.getDeletedOffers()
            logger.info("testGetDeletedOffers: default result size=${defaultResult.size}")
            for (deleted in defaultResult.take(5)) {
                logger.info("testGetDeletedOffers: courseOfStudyId=${deleted.courseOfStudyId}, academicYearId=${deleted.academicYearId}, odModificationDate=${deleted.odModificationDate}")
            }

            logger.info("testGetDeletedOffers: calling with pagination start=0, limit=5")
            val paginatedResult = api.offer.getDeletedOffers(start = 0, limit = 5)
            logger.info("testGetDeletedOffers: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val first = defaultResult.first()
                if (first.odModificationDate != null) {
                    logger.info("testGetDeletedOffers: calling with odModificationDate=${first.odModificationDate}")
                    val filteredByDate = api.offer.getDeletedOffers(odModificationDate = first.odModificationDate)
                    logger.info("testGetDeletedOffers: filtered by odModificationDate size=${filteredByDate.size}")
                    assertTrue(filteredByDate.isNotEmpty(), "Filtered by odModificationDate should return non-empty list")
                }
            }
        } else {
            logger.info("testGetDeletedOffers: user lacks ANY permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getDeletedOffers()
            }
            logger.info("testGetDeletedOffers: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetFullOffers() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val offers = api.offer.getOffers(start = 0, limit = 1)
            assertTrue(offers.isNotEmpty(), "Need at least one offer to test getFullOffers")
            val offer = offers.first()

            logger.info("testGetFullOffers: calling with academicYearOfferId=${offer.academicYearOfferId}, courseOfStudyOfferId=${offer.courseOfStudyOfferId}")
            val result = api.offer.getFullOffers(
                academicYearOfferId = offer.academicYearOfferId,
                courseOfStudyOfferId = offer.courseOfStudyOfferId
            )
            logger.info("testGetFullOffers: academicYearOfferId=${result.academicYearOfferId}, courseOfStudyOfferId=${result.courseOfStudyOfferId}, courseOfStudyCode=${result.courseOfStudyCode}, courseOfStudyDescription=${result.courseOfStudyDescription}")
            logger.info("testGetFullOffers: courseTypeCode=${result.courseTypeCode}, departmentCode=${result.departmentCode}, departmentDescription=${result.departmentDescription}")
            logger.info("testGetFullOffers: teachingActivityContestWithDetails size=${result.teachingActivityContestWithDetails.size}")
            assertNotNull(result.academicYearOfferId, "academicYearOfferId should not be null")
            assertNotNull(result.courseOfStudyOfferId, "courseOfStudyOfferId should not be null")

            for (activity in result.teachingActivityContestWithDetails.take(3)) {
                val key = activity.contextualizedTeachingActivityKey
                logger.info("testGetFullOffers: activity activityCode=${key.activityCode}, activityDescription=${key.activityDescription}, teachingUnitContestWithDetails=${activity.teachingUnitContestWithDetails.size}, teachingLanguages=${activity.teachingLanguages.size}")
            }
        } else {
            logger.info("testGetFullOffers: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.offer.getFullOffers(
                    academicYearOfferId = 2024,
                    courseOfStudyOfferId = 1
                )
            }
            logger.info("testGetFullOffers: Esse3Exception thrown as expected")
        }
    }
}
