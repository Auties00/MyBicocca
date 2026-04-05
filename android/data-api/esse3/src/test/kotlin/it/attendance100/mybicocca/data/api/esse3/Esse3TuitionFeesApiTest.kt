package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3CollectionData
import it.attendance100.mybicocca.data.dto.esse3.Esse3ExemptionData
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoiceAttachmentMetadata
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoiceInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3InvoicePaymentData
import it.attendance100.mybicocca.data.dto.esse3.Esse3PagoPATransaction
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3RejectExemption
import it.attendance100.mybicocca.data.dto.esse3.Esse3ScholarshipOutcomeData
import it.attendance100.mybicocca.data.dto.esse3.Esse3SpgTransactionStatusData
import it.attendance100.mybicocca.data.dto.esse3.Esse3Cart
import it.attendance100.mybicocca.data.exception.esse3.Esse3Exception
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Esse3TuitionFeesApiTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3TuitionFeesApiTest::class.java.name)
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
    @Disabled("Irreversible mutating operation")
    fun testPostAcquireScholarshipOutcomeApplications() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAcquireScholarshipOutcomeApplications: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostAcquireScholarshipOutcomeApplications: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postAcquireScholarshipOutcomeApplications(
                    body = Esse3ScholarshipOutcomeData(
                        academicYear = 2025,
                        requestType = 1,
                        scholarshipData = emptyList()
                    )
                )
            }
            logger.info("testPostAcquireScholarshipOutcomeApplications: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostAcquireExemptions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostAcquireExemptions: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostAcquireExemptions: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postAcquireExemptions(
                    body = listOf(
                        Esse3ExemptionData(
                            academicYear = 2025,
                            fiscalCode = session.fiscalCode ?: "UNKNOWN",
                            exemption = "TEST"
                        )
                    )
                )
            }
            logger.info("testPostAcquireExemptions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPersonChargesList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId

            logger.info("testGetPersonChargesList: calling getPersonChargesList(personId=$personId) with defaults")
            val defaultResult = api.tuitionFees.getPersonChargesList(personId = personId)
            logger.info("testGetPersonChargesList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getPersonChargesList() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetPersonChargesList: invoiceId=${item.invoiceId}, taxCode=${item.taxCode}, taxDescription=${item.taxDescription}, itemAmount=${item.itemAmount}, academicYearId=${item.academicYearId}, paidFlag=${item.paidFlag}")
            }

            logger.info("testGetPersonChargesList: calling getPersonChargesList() with pagination start=0, limit=5")
            val paginatedResult = api.tuitionFees.getPersonChargesList(
                personId = personId,
                start = 0,
                limit = 5
            )
            logger.info("testGetPersonChargesList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()

                if (firstItem.academicYearId != null) {
                    logger.info("testGetPersonChargesList: calling getPersonChargesList() with academicYearId=${firstItem.academicYearId}")
                    val filteredByYear = api.tuitionFees.getPersonChargesList(
                        personId = personId,
                        academicYearId = firstItem.academicYearId
                    )
                    logger.info("testGetPersonChargesList: filtered by academicYearId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearId should return non-empty list")
                }

                if (firstItem.invoiceId != null) {
                    logger.info("testGetPersonChargesList: calling getPersonChargesList() with invoiceId=${firstItem.invoiceId}")
                    val filteredByInvoice = api.tuitionFees.getPersonChargesList(
                        personId = personId,
                        invoiceId = firstItem.invoiceId
                    )
                    logger.info("testGetPersonChargesList: filtered by invoiceId result size=${filteredByInvoice.size}")
                    assertTrue(filteredByInvoice.isNotEmpty(), "Filtered by invoiceId should return non-empty list")
                }

                logger.info("testGetPersonChargesList: calling getPersonChargesList() with paidFlag=0")
                val unpaidResult = api.tuitionFees.getPersonChargesList(
                    personId = personId,
                    paidFlag = 0
                )
                logger.info("testGetPersonChargesList: unpaid result size=${unpaidResult.size}")

                logger.info("testGetPersonChargesList: calling getPersonChargesList() with canceledFlag=0")
                val notCanceledResult = api.tuitionFees.getPersonChargesList(
                    personId = personId,
                    canceledFlag = 0
                )
                logger.info("testGetPersonChargesList: not canceled result size=${notCanceledResult.size}")
            }

            logger.info("testGetPersonChargesList: calling getPersonChargesList() with order=aaId")
            val orderedResult = api.tuitionFees.getPersonChargesList(
                personId = personId,
                order = "aaId"
            )
            logger.info("testGetPersonChargesList: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getPersonChargesList() with order should not be null")
        } else {
            logger.info("testGetPersonChargesList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getPersonChargesList(personId = studentProfile.personId)
            }
            logger.info("testGetPersonChargesList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentChargesList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId

            logger.info("testGetStudentChargesList: calling getStudentChargesList() with defaults")
            val defaultResult = api.tuitionFees.getStudentChargesList()
            logger.info("testGetStudentChargesList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getStudentChargesList() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetStudentChargesList: invoiceId=${item.invoiceId}, taxCode=${item.taxCode}, taxDescription=${item.taxDescription}, itemAmount=${item.itemAmount}, academicYearId=${item.academicYearId}, studentId=${item.studentId}")
            }

            logger.info("testGetStudentChargesList: calling getStudentChargesList() with studentId=$studentId")
            val resultWithStudentId = api.tuitionFees.getStudentChargesList(studentId = studentId)
            logger.info("testGetStudentChargesList: result with studentId size=${resultWithStudentId.size}")
            assertNotNull(resultWithStudentId, "getStudentChargesList() with studentId should not be null")

            logger.info("testGetStudentChargesList: calling getStudentChargesList() with pagination start=0, limit=5")
            val paginatedResult = api.tuitionFees.getStudentChargesList(
                start = 0,
                limit = 5
            )
            logger.info("testGetStudentChargesList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()

                if (firstItem.academicYearId != null) {
                    logger.info("testGetStudentChargesList: calling getStudentChargesList() with academicYearId=${firstItem.academicYearId}")
                    val filteredByYear = api.tuitionFees.getStudentChargesList(
                        academicYearId = firstItem.academicYearId
                    )
                    logger.info("testGetStudentChargesList: filtered by academicYearId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearId should return non-empty list")
                }

                if (firstItem.invoiceId != null) {
                    logger.info("testGetStudentChargesList: calling getStudentChargesList() with invoiceId=${firstItem.invoiceId}")
                    val filteredByInvoice = api.tuitionFees.getStudentChargesList(
                        invoiceId = firstItem.invoiceId
                    )
                    logger.info("testGetStudentChargesList: filtered by invoiceId result size=${filteredByInvoice.size}")
                    assertTrue(filteredByInvoice.isNotEmpty(), "Filtered by invoiceId should return non-empty list")
                }

                logger.info("testGetStudentChargesList: calling getStudentChargesList() with paidFlag=0")
                val unpaidResult = api.tuitionFees.getStudentChargesList(paidFlag = 0)
                logger.info("testGetStudentChargesList: unpaid result size=${unpaidResult.size}")

                logger.info("testGetStudentChargesList: calling getStudentChargesList() with canceledFlag=0")
                val notCanceledResult = api.tuitionFees.getStudentChargesList(canceledFlag = 0)
                logger.info("testGetStudentChargesList: not canceled result size=${notCanceledResult.size}")

                logger.info("testGetStudentChargesList: calling getStudentChargesList() with expiredFlag=0")
                val notExpiredResult = api.tuitionFees.getStudentChargesList(expiredFlag = 0)
                logger.info("testGetStudentChargesList: not expired result size=${notExpiredResult.size}")

                if (firstItem.taxTypeCode != null) {
                    logger.info("testGetStudentChargesList: calling getStudentChargesList() with taxTypeCode=${firstItem.taxTypeCode}")
                    val filteredByTaxType = api.tuitionFees.getStudentChargesList(
                        taxTypeCode = firstItem.taxTypeCode
                    )
                    logger.info("testGetStudentChargesList: filtered by taxTypeCode result size=${filteredByTaxType.size}")
                    assertTrue(filteredByTaxType.isNotEmpty(), "Filtered by taxTypeCode should return non-empty list")
                }
            }

            logger.info("testGetStudentChargesList: calling getStudentChargesList() with order=aaId")
            val orderedResult = api.tuitionFees.getStudentChargesList(order = "aaId")
            logger.info("testGetStudentChargesList: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getStudentChargesList() with order should not be null")
        } else {
            logger.info("testGetStudentChargesList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getStudentChargesList()
            }
            logger.info("testGetStudentChargesList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetAdisurcMeritScholarships() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val fiscalCode = session.fiscalCode ?: throw IllegalStateException("Fiscal code not available")

            logger.info("testGetAdisurcMeritScholarships: calling getAdisurcMeritScholarships(fiscalCode=$fiscalCode, academicYearEnrollmentId=2025)")
            val result = api.tuitionFees.getAdisurcMeritScholarships(
                fiscalCode = fiscalCode,
                academicYearEnrollmentId = 2025
            )
            logger.info("testGetAdisurcMeritScholarships: result fiscalCode=${result.fiscalCode}, surname=${result.surname}, name=${result.name}, enrollments size=${result.studentEnrollment.size}")
            assertNotNull(result, "getAdisurcMeritScholarships() should not return null")
        } else {
            logger.info("testGetAdisurcMeritScholarships: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getAdisurcMeritScholarships(
                    fiscalCode = session.fiscalCode ?: "UNKNOWN",
                    academicYearEnrollmentId = 2025
                )
            }
            logger.info("testGetAdisurcMeritScholarships: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostInvoiceAttachmentMetadata() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostInvoiceAttachmentMetadata: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostInvoiceAttachmentMetadata: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postInvoiceAttachmentMetadata(
                    invoiceId = 1,
                    body = Esse3InvoiceAttachmentMetadata(
                        fileName = "test.pdf",
                        title = "Test Attachment",
                        description = "Test attachment description",
                        validFlag = 1,
                        webVisibility = 1
                    )
                )
            }
            logger.info("testPostInvoiceAttachmentMetadata: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testCancelInvoice() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testCancelInvoice: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testCancelInvoice: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.cancelInvoice(
                    invoiceId = 1,
                    cancellationType = 1
                )
            }
            logger.info("testCancelInvoice: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPutCancelPayment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPutCancelPayment: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPutCancelPayment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.putCancelPayment(
                    invoiceId = 1,
                    cancelCall = 0
                )
            }
            logger.info("testPutCancelPayment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetSelfCertification() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId
            val academicYearId = 2025L

            logger.info("testGetSelfCertification: calling getSelfCertification(personId=$personId, academicYearId=$academicYearId) with defaults")
            val defaultResult = api.tuitionFees.getSelfCertification(
                personId = personId,
                academicYearId = academicYearId
            )
            logger.info("testGetSelfCertification: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getSelfCertification() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetSelfCertification: selfCertificationId=${item.selfCertificationId}, academicYearId=${item.academicYearId}, isee=${item.isee}, bandDescription=${item.bandDescription}")
            }

            logger.info("testGetSelfCertification: calling getSelfCertification() with pagination start=0, limit=5")
            val paginatedResult = api.tuitionFees.getSelfCertification(
                personId = personId,
                academicYearId = academicYearId,
                start = 0,
                limit = 5
            )
            logger.info("testGetSelfCertification: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            logger.info("testGetSelfCertification: calling getSelfCertification() with order=autocertId")
            val orderedResult = api.tuitionFees.getSelfCertification(
                personId = personId,
                academicYearId = academicYearId,
                order = "autocertId"
            )
            logger.info("testGetSelfCertification: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getSelfCertification() with order should not be null")
        } else {
            logger.info("testGetSelfCertification: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getSelfCertification(
                    personId = studentProfile.personId,
                    academicYearId = 2025
                )
            }
            logger.info("testGetSelfCertification: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetValidExemptionsAcademicYear() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val academicYearId = 2025L

            logger.info("testGetValidExemptionsAcademicYear: calling getValidExemptionsAcademicYear(academicYearId=$academicYearId) with defaults")
            val defaultResult = api.tuitionFees.getValidExemptionsAcademicYear(academicYearId = academicYearId)
            logger.info("testGetValidExemptionsAcademicYear: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getValidExemptionsAcademicYear() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetValidExemptionsAcademicYear: exemptionCode=${item.exemptionCode}, exemptionDescription=${item.exemptionDescription}, priority=${item.priority}, requestable=${item.requestable}")
            }

            logger.info("testGetValidExemptionsAcademicYear: calling getValidExemptionsAcademicYear() with pagination start=0, limit=5")
            val paginatedResult = api.tuitionFees.getValidExemptionsAcademicYear(
                academicYearId = academicYearId,
                start = 0,
                limit = 5
            )
            logger.info("testGetValidExemptionsAcademicYear: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")

            if (defaultResult.isNotEmpty()) {
                val firstExemption = defaultResult.first()
                if (firstExemption.exemptionCode != null) {
                    logger.info("testGetValidExemptionsAcademicYear: calling getValidExemptionsAcademicYear() with exemptionCode=${firstExemption.exemptionCode}")
                    val filteredResult = api.tuitionFees.getValidExemptionsAcademicYear(
                        academicYearId = academicYearId,
                        exemptionCode = firstExemption.exemptionCode
                    )
                    logger.info("testGetValidExemptionsAcademicYear: filtered result size=${filteredResult.size}")
                    assertTrue(filteredResult.isNotEmpty(), "Filtered by exemptionCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetValidExemptionsAcademicYear: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getValidExemptionsAcademicYear(academicYearId = 2025)
            }
            logger.info("testGetValidExemptionsAcademicYear: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testRejectExemption() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testRejectExemption: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testRejectExemption: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.rejectExemption(
                    body = Esse3RejectExemption(
                        academicYear = 2025,
                        fiscalCode = session.fiscalCode ?: "UNKNOWN",
                        exemption = "TEST",
                        reason = "Test rejection"
                    )
                )
            }
            logger.info("testRejectExemption: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostCreateStudentInvoice() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostCreateStudentInvoice: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostCreateStudentInvoice: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postCreateStudentInvoice(
                    body = Esse3InvoiceInsert(
                        academicYearId = 2025,
                        studentId = studentProfile.studentId
                    )
                )
            }
            logger.info("testPostCreateStudentInvoice: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInvoice() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val invoicesList = api.tuitionFees.getInvoicesList()
            logger.info("testGetInvoice: fetched invoices list to derive invoiceId, size=${invoicesList.size}")

            if (invoicesList.isNotEmpty()) {
                val invoiceId = invoicesList.first().invoiceId
                    ?: throw IllegalStateException("No invoiceId found in first invoice")

                logger.info("testGetInvoice: calling getInvoice(invoiceId=$invoiceId)")
                val result = api.tuitionFees.getInvoice(invoiceId = invoiceId)
                logger.info("testGetInvoice: result invoiceId=${result.invoiceId}, invoiceAmount=${result.invoiceAmount}, paidFlag=${result.paidFlag}, invoiceExpiration=${result.invoiceExpiration}")
                assertNotNull(result, "getInvoice() should not return null")
            } else {
                logger.info("testGetInvoice: no invoices found, skipping detail fetch")
            }
        } else {
            logger.info("testGetInvoice: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getInvoice(invoiceId = 1)
            }
            logger.info("testGetInvoice: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostCollection() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostCollection: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostCollection: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postCollection(
                    body = Esse3CollectionData(
                        transactionId = 1,
                        invoiceId = 1,
                        amount = 100.0,
                        paymentDate = "2025-01-01",
                        origin = "TEST"
                    )
                )
            }
            logger.info("testPostCollection: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetStudentExemptionsList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetStudentExemptionsList: calling getStudentExemptionsList() with defaults")
            val defaultResult = api.tuitionFees.getStudentExemptionsList()
            logger.info("testGetStudentExemptionsList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getStudentExemptionsList() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetStudentExemptionsList: studentId=${item.studentId}, enrollmentId=${item.enrollmentId}, courseOfStudyId=${item.courseOfStudyId}, academicYearEnrollmentId=${item.academicYearEnrollmentId}, exemptions=${item.exemptionList.size}")
            }

            logger.info("testGetStudentExemptionsList: calling getStudentExemptionsList() with studentId=${studentProfile.studentId}")
            val resultWithStudentId = api.tuitionFees.getStudentExemptionsList(
                studentId = studentProfile.studentId
            )
            logger.info("testGetStudentExemptionsList: result with studentId size=${resultWithStudentId.size}")
            assertNotNull(resultWithStudentId, "getStudentExemptionsList() with studentId should not be null")

            logger.info("testGetStudentExemptionsList: calling getStudentExemptionsList() with courseOfStudyId=${studentProfile.degreeCourseId}")
            val resultWithCourseId = api.tuitionFees.getStudentExemptionsList(
                courseOfStudyId = studentProfile.degreeCourseId
            )
            logger.info("testGetStudentExemptionsList: result with courseOfStudyId size=${resultWithCourseId.size}")

            logger.info("testGetStudentExemptionsList: calling getStudentExemptionsList() with pagination start=0, limit=5")
            val paginatedResult = api.tuitionFees.getStudentExemptionsList(
                start = 0,
                limit = 5
            )
            logger.info("testGetStudentExemptionsList: paginated result size=${paginatedResult.size}")
            assertTrue(paginatedResult.size <= 5, "Paginated result size should be <= 5")
        } else {
            logger.info("testGetStudentExemptionsList: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getStudentExemptionsList()
            }
            logger.info("testGetStudentExemptionsList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetInvoicesList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.ANY)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetInvoicesList: calling getInvoicesList() with defaults")
            val defaultResult = api.tuitionFees.getInvoicesList()
            logger.info("testGetInvoicesList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getInvoicesList() default result should not be null")
            assertTrue(defaultResult.isNotEmpty(), "getInvoicesList() should return non-empty list")
            for (item in defaultResult.take(5)) {
                logger.info("testGetInvoicesList: invoiceId=${item.invoiceId}, invoiceAmount=${item.invoiceAmount}, paidFlag=${item.paidFlag}, invoiceExpiration=${item.invoiceExpiration}, academicYearId=${item.academicYearId}, iuv=${item.iuv}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()

                if (firstItem.academicYearId != null) {
                    logger.info("testGetInvoicesList: calling getInvoicesList() with academicYearId=${firstItem.academicYearId}")
                    val filteredByYear = api.tuitionFees.getInvoicesList(
                        academicYearId = firstItem.academicYearId
                    )
                    logger.info("testGetInvoicesList: filtered by academicYearId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearId should return non-empty list")
                }

                if (firstItem.invoiceId != null) {
                    logger.info("testGetInvoicesList: calling getInvoicesList() with invoiceId=${firstItem.invoiceId}")
                    val filteredByInvoice = api.tuitionFees.getInvoicesList(
                        invoiceId = firstItem.invoiceId
                    )
                    logger.info("testGetInvoicesList: filtered by invoiceId result size=${filteredByInvoice.size}")
                    assertTrue(filteredByInvoice.isNotEmpty(), "Filtered by invoiceId should return non-empty list")
                }

                logger.info("testGetInvoicesList: calling getInvoicesList() with paidFlag=0")
                val unpaidResult = api.tuitionFees.getInvoicesList(paidFlag = 0)
                logger.info("testGetInvoicesList: unpaid result size=${unpaidResult.size}")

                logger.info("testGetInvoicesList: calling getInvoicesList() with canceledInvoice=0")
                val notCanceledResult = api.tuitionFees.getInvoicesList(canceledInvoice = 0)
                logger.info("testGetInvoicesList: not canceled result size=${notCanceledResult.size}")

                logger.info("testGetInvoicesList: calling getInvoicesList() with expiredFlag=0")
                val notExpiredResult = api.tuitionFees.getInvoicesList(expiredFlag = 0)
                logger.info("testGetInvoicesList: not expired result size=${notExpiredResult.size}")

                logger.info("testGetInvoicesList: calling getInvoicesList() with personId=${studentProfile.personId}")
                val resultWithPerson = api.tuitionFees.getInvoicesList(
                    personId = studentProfile.personId
                )
                logger.info("testGetInvoicesList: result with personId size=${resultWithPerson.size}")

                logger.info("testGetInvoicesList: calling getInvoicesList() with retrieveAdditionalInfo=true")
                val resultWithAdditionalInfo = api.tuitionFees.getInvoicesList(
                    retrieveAdditionalInfo = true
                )
                logger.info("testGetInvoicesList: result with retrieveAdditionalInfo size=${resultWithAdditionalInfo.size}")
                for (item in resultWithAdditionalInfo.take(3)) {
                    logger.info("testGetInvoicesList: additionalInfo invoiceId=${item.invoiceId}, additionalInfo=${item.additionalInfo}")
                }

                if (firstItem.iuv != null) {
                    logger.info("testGetInvoicesList: calling getInvoicesList() with iUV=${firstItem.iuv}")
                    val filteredByIuv = api.tuitionFees.getInvoicesList(
                        iUV = firstItem.iuv
                    )
                    logger.info("testGetInvoicesList: filtered by iUV result size=${filteredByIuv.size}")
                    assertTrue(filteredByIuv.isNotEmpty(), "Filtered by iUV should return non-empty list")
                }

                if (firstItem.noticeCode != null) {
                    logger.info("testGetInvoicesList: calling getInvoicesList() with noticeCode=${firstItem.noticeCode}")
                    val filteredByNotice = api.tuitionFees.getInvoicesList(
                        noticeCode = firstItem.noticeCode
                    )
                    logger.info("testGetInvoicesList: filtered by noticeCode result size=${filteredByNotice.size}")
                    assertTrue(filteredByNotice.isNotEmpty(), "Filtered by noticeCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetInvoicesList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getInvoicesList()
            }
            logger.info("testGetInvoicesList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPaymentsList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId

            logger.info("testGetPaymentsList: calling getPaymentsList(personId=$personId) with defaults")
            val defaultResult = api.tuitionFees.getPaymentsList(personId = personId)
            logger.info("testGetPaymentsList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getPaymentsList() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetPaymentsList: invoiceId=${item.invoiceId}, paymentId=${item.paymentId}, paidAmount=${item.paidAmount}, paymentDate=${item.paymentDate}, name=${item.name}, surname=${item.surname}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()
                if (firstItem.invoiceId != null) {
                    logger.info("testGetPaymentsList: calling getPaymentsList() with invoiceId=${firstItem.invoiceId}")
                    val filteredByInvoice = api.tuitionFees.getPaymentsList(
                        personId = personId,
                        invoiceId = firstItem.invoiceId
                    )
                    logger.info("testGetPaymentsList: filtered by invoiceId result size=${filteredByInvoice.size}")
                    assertTrue(filteredByInvoice.isNotEmpty(), "Filtered by invoiceId should return non-empty list")
                }
            }
        } else {
            logger.info("testGetPaymentsList: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getPaymentsList(personId = studentProfile.personId)
            }
            logger.info("testGetPaymentsList: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetRefundsList() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val personId = studentProfile.personId

            logger.info("testGetRefundsList: calling getRefundsList(personId=$personId) with defaults")
            val defaultResult = api.tuitionFees.getRefundsList(personId = personId)
            logger.info("testGetRefundsList: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getRefundsList() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetRefundsList: invoiceId=${item.invoiceId}, invoiceAmount=${item.invoiceAmount}, refundedFlag=${item.refundedFlag}, refundReasonCode=${item.refundReasonCode}, paymentDate=${item.paymentDate}")
            }

            logger.info("testGetRefundsList: calling getRefundsList() with refundedFlag=0")
            val notRefundedResult = api.tuitionFees.getRefundsList(
                personId = personId,
                refundedFlag = 0
            )
            logger.info("testGetRefundsList: not refunded result size=${notRefundedResult.size}")

            logger.info("testGetRefundsList: calling getRefundsList() with refundedFlag=1")
            val refundedResult = api.tuitionFees.getRefundsList(
                personId = personId,
                refundedFlag = 1
            )
            logger.info("testGetRefundsList: refunded result size=${refundedResult.size}")
        } else {
            logger.info("testGetRefundsList: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getRefundsList(personId = studentProfile.personId)
            }
            logger.info("testGetRefundsList: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostPayInvoice() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostPayInvoice: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostPayInvoice: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postPayInvoice(
                    body = Esse3InvoicePaymentData(
                        invoiceId = 1,
                        amount = 100.0,
                        paymentDate = "2025-01-01",
                        paymentType = "BONIFICO"
                    )
                )
            }
            logger.info("testPostPayInvoice: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testPutPrintPagoPANotice() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val invoicesList = api.tuitionFees.getInvoicesList()
            logger.info("testPutPrintPagoPANotice: fetched invoices list, size=${invoicesList.size}")

            val unpaidInvoice = invoicesList.firstOrNull { it.paidFlag == 0 && it.canceledInvoice == null && it.pagopaEnabled == 1 }
            if (unpaidInvoice != null && unpaidInvoice.invoiceId != null) {
                val invoiceId = unpaidInvoice.invoiceId!!
                logger.info("testPutPrintPagoPANotice: calling putPrintPagoPANotice(invoiceId=$invoiceId)")
                val result = api.tuitionFees.putPrintPagoPANotice(invoiceId = invoiceId)
                logger.info("testPutPrintPagoPANotice: received ByteReadChannel stream for invoiceId=$invoiceId")
                assertNotNull(result, "putPrintPagoPANotice() should return a non-null stream")
            } else {
                logger.info("testPutPrintPagoPANotice: no suitable unpaid PagoPA invoice found, skipping stream test")
            }
        } else {
            logger.info("testPutPrintPagoPANotice: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.putPrintPagoPANotice(invoiceId = 1)
            }
            logger.info("testPutPrintPagoPANotice: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testPutRequestPaymentStatus() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val invoicesList = api.tuitionFees.getInvoicesList()
            logger.info("testPutRequestPaymentStatus: fetched invoices list, size=${invoicesList.size}")

            if (invoicesList.isNotEmpty()) {
                val invoiceId = invoicesList.first().invoiceId
                    ?: throw IllegalStateException("No invoiceId found in first invoice")

                logger.info("testPutRequestPaymentStatus: calling putRequestPaymentStatus(invoiceId=$invoiceId)")
                val result = api.tuitionFees.putRequestPaymentStatus(invoiceId = invoiceId)
                logger.info("testPutRequestPaymentStatus: result invoiceId=${result.invoiceId}, paid=${result.paid}, paymentAmount=${result.paymentAmount}, iuv=${result.iuv}")
                assertNotNull(result, "putRequestPaymentStatus() should not return null")
            } else {
                logger.info("testPutRequestPaymentStatus: no invoices found, skipping status check")
            }
        } else {
            logger.info("testPutRequestPaymentStatus: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.putRequestPaymentStatus(invoiceId = 1)
            }
            logger.info("testPutRequestPaymentStatus: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPagoPAReceipt() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val invoicesList = api.tuitionFees.getInvoicesList()
            logger.info("testGetPagoPAReceipt: fetched invoices list, size=${invoicesList.size}")

            val paidInvoice = invoicesList.firstOrNull { it.paidFlag == 1 && it.iuv != null }
            if (paidInvoice != null && paidInvoice.invoiceId != null) {
                val invoiceId = paidInvoice.invoiceId!!
                logger.info("testGetPagoPAReceipt: calling getPagoPAReceipt(invoiceId=$invoiceId, language=it)")
                val result = api.tuitionFees.getPagoPAReceipt(invoiceId = invoiceId, language = "it")
                logger.info("testGetPagoPAReceipt: received ByteReadChannel stream for invoiceId=$invoiceId")
                assertNotNull(result, "getPagoPAReceipt() should return a non-null stream")

                logger.info("testGetPagoPAReceipt: calling getPagoPAReceipt(invoiceId=$invoiceId, language=en)")
                val resultEn = api.tuitionFees.getPagoPAReceipt(invoiceId = invoiceId, language = "en")
                logger.info("testGetPagoPAReceipt: received ByteReadChannel stream (en) for invoiceId=$invoiceId")
                assertNotNull(resultEn, "getPagoPAReceipt() with language=en should return a non-null stream")
            } else {
                logger.info("testGetPagoPAReceipt: no suitable paid PagoPA invoice found, skipping receipt test")
            }
        } else {
            logger.info("testGetPagoPAReceipt: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getPagoPAReceipt(invoiceId = 1, language = "it")
            }
            logger.info("testGetPagoPAReceipt: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostInitPagoPaTransaction() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostInitPagoPaTransaction: user has required permission, but test is disabled to prevent initiating a real payment transaction")
        } else {
            logger.info("testPostInitPagoPaTransaction: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postInitPagoPaTransaction(
                    body = Esse3PagoPATransaction(
                        invoiceId = 1,
                        returnURL = "https://example.com/return"
                    )
                )
            }
            logger.info("testPostInitPagoPaTransaction: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPagoPATransactions() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT, Esse3PermissionLevel.REGISTERED_USER, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with defaults")
            val defaultResult = api.tuitionFees.getPagoPATransactions()
            logger.info("testGetPagoPATransactions: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getPagoPATransactions() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetPagoPATransactions: invoiceId=${item.invoiceId}, iuv=${item.iuv}, amount=${item.amount}, transactionDate=${item.transactionDate}, outcomeCode=${item.outcomeCode}, state=${item.state}, finalState=${item.finalState}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()

                if (firstItem.invoiceId != null) {
                    logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with invoiceId=${firstItem.invoiceId}")
                    val filteredByInvoice = api.tuitionFees.getPagoPATransactions(
                        invoiceId = firstItem.invoiceId
                    )
                    logger.info("testGetPagoPATransactions: filtered by invoiceId result size=${filteredByInvoice.size}")
                    assertTrue(filteredByInvoice.isNotEmpty(), "Filtered by invoiceId should return non-empty list")
                }

                if (firstItem.iuv != null) {
                    logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with iUV=${firstItem.iuv}")
                    val filteredByIuv = api.tuitionFees.getPagoPATransactions(
                        iUV = firstItem.iuv
                    )
                    logger.info("testGetPagoPATransactions: filtered by iUV result size=${filteredByIuv.size}")
                    assertTrue(filteredByIuv.isNotEmpty(), "Filtered by iUV should return non-empty list")
                }

                if (firstItem.academicYearId != null) {
                    logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with academicYearId=${firstItem.academicYearId}")
                    val filteredByYear = api.tuitionFees.getPagoPATransactions(
                        academicYearId = firstItem.academicYearId!!.toLong()
                    )
                    logger.info("testGetPagoPATransactions: filtered by academicYearId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearId should return non-empty list")
                }

                logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with finalState=1")
                val finalStateResult = api.tuitionFees.getPagoPATransactions(finalState = 1)
                logger.info("testGetPagoPATransactions: finalState=1 result size=${finalStateResult.size}")

                logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with lastTransaction=1")
                val lastTransResult = api.tuitionFees.getPagoPATransactions(lastTransaction = 1)
                logger.info("testGetPagoPATransactions: lastTransaction=1 result size=${lastTransResult.size}")

                if (firstItem.fiscalCode != null) {
                    logger.info("testGetPagoPATransactions: calling getPagoPATransactions() with fiscalCode=${firstItem.fiscalCode}")
                    val filteredByFiscalCode = api.tuitionFees.getPagoPATransactions(
                        fiscalCode = firstItem.fiscalCode
                    )
                    logger.info("testGetPagoPATransactions: filtered by fiscalCode result size=${filteredByFiscalCode.size}")
                    assertTrue(filteredByFiscalCode.isNotEmpty(), "Filtered by fiscalCode should return non-empty list")
                }
            }
        } else {
            logger.info("testGetPagoPATransactions: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getPagoPATransactions()
            }
            logger.info("testGetPagoPATransactions: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetMultibenPayments() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val entity = "UNIMIB"

            logger.info("testGetMultibenPayments: calling getMultibenPayments(entity=$entity) with defaults")
            val defaultResult = api.tuitionFees.getMultibenPayments(entity = entity)
            logger.info("testGetMultibenPayments: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getMultibenPayments() default result should not be null")
            for (item in defaultResult.take(5)) {
                logger.info("testGetMultibenPayments: iuv=${item.iuv}, paymentAmount=${item.paymentAmount}, paymentDate=${item.paymentDate}, fiscalCode=${item.fiscalCode}, name=${item.name}, surname=${item.surname}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()

                if (firstItem.iuv != null) {
                    logger.info("testGetMultibenPayments: calling getMultibenPayments() with iUV=${firstItem.iuv}")
                    val filteredByIuv = api.tuitionFees.getMultibenPayments(
                        entity = entity,
                        iUV = firstItem.iuv
                    )
                    logger.info("testGetMultibenPayments: filtered by iUV result size=${filteredByIuv.size}")
                    assertTrue(filteredByIuv.isNotEmpty(), "Filtered by iUV should return non-empty list")
                }

                if (firstItem.academicYearDebt != null) {
                    logger.info("testGetMultibenPayments: calling getMultibenPayments() with academicYearDebt=${firstItem.academicYearDebt}")
                    val filteredByYear = api.tuitionFees.getMultibenPayments(
                        entity = entity,
                        academicYearDebt = firstItem.academicYearDebt
                    )
                    logger.info("testGetMultibenPayments: filtered by academicYearDebt result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearDebt should return non-empty list")
                }
            }
        } else {
            logger.info("testGetMultibenPayments: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getMultibenPayments(entity = "UNIMIB")
            }
            logger.info("testGetMultibenPayments: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetPagoPAYPayment() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val invoicesList = api.tuitionFees.getInvoicesList()
            logger.info("testGetPagoPAYPayment: fetched invoices list, size=${invoicesList.size}")

            val paidInvoice = invoicesList.firstOrNull { it.paidFlag == 1 && it.iuv != null }
            if (paidInvoice != null && paidInvoice.invoiceId != null) {
                val invoiceId = paidInvoice.invoiceId!!

                logger.info("testGetPagoPAYPayment: calling getPagoPAYPayment(invoiceId=$invoiceId)")
                val resultByInvoice = api.tuitionFees.getPagoPAYPayment(invoiceId = invoiceId)
                logger.info("testGetPagoPAYPayment: result invoiceId=${resultByInvoice.invoiceId}, iuv=${resultByInvoice.iuv}, paymentAmount=${resultByInvoice.paymentAmount}, paid=${resultByInvoice.paid}")
                assertNotNull(resultByInvoice, "getPagoPAYPayment() by invoiceId should not return null")

                if (paidInvoice.iuv != null) {
                    logger.info("testGetPagoPAYPayment: calling getPagoPAYPayment(iUV=${paidInvoice.iuv})")
                    val resultByIuv = api.tuitionFees.getPagoPAYPayment(iUV = paidInvoice.iuv)
                    logger.info("testGetPagoPAYPayment: result by iUV invoiceId=${resultByIuv.invoiceId}, iuv=${resultByIuv.iuv}")
                    assertNotNull(resultByIuv, "getPagoPAYPayment() by iUV should not return null")
                }
            } else {
                logger.info("testGetPagoPAYPayment: no suitable paid PagoPA invoice found, skipping payment lookup")
            }
        } else {
            logger.info("testGetPagoPAYPayment: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getPagoPAYPayment(invoiceId = 1)
            }
            logger.info("testGetPagoPAYPayment: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetEnrollmentsForTaxes() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId

            logger.info("testGetEnrollmentsForTaxes: calling getEnrollmentsForTaxes(studentId=$studentId) with defaults")
            val defaultResult = api.tuitionFees.getEnrollmentsForTaxes(studentId = studentId)
            logger.info("testGetEnrollmentsForTaxes: default result size=${defaultResult.size}")
            assertNotNull(defaultResult, "getEnrollmentsForTaxes() default result should not be null")
            assertTrue(defaultResult.isNotEmpty(), "getEnrollmentsForTaxes() should return non-empty list for enrolled student")
            for (item in defaultResult.take(5)) {
                logger.info("testGetEnrollmentsForTaxes: studentId=${item.studentId}, courseOfStudyId=${item.courseOfStudyId}, courseOfStudyDescription=${item.courseOfStudyDescription}, academicYearEnrollmentId=${item.academicYearEnrollmentId}, courseYear=${item.courseYear}, isee=${item.isee}, exemptionTypeCode=${item.exemptionTypeCode}")
            }

            if (defaultResult.isNotEmpty()) {
                val firstItem = defaultResult.first()
                if (firstItem.academicYearEnrollmentId != null) {
                    logger.info("testGetEnrollmentsForTaxes: calling getEnrollmentsForTaxes() with academicYearEnrollmentId=${firstItem.academicYearEnrollmentId}")
                    val filteredByYear = api.tuitionFees.getEnrollmentsForTaxes(
                        studentId = studentId,
                        academicYearEnrollmentId = firstItem.academicYearEnrollmentId
                    )
                    logger.info("testGetEnrollmentsForTaxes: filtered by academicYearEnrollmentId result size=${filteredByYear.size}")
                    assertTrue(filteredByYear.isNotEmpty(), "Filtered by academicYearEnrollmentId should return non-empty list")
                }
            }

            logger.info("testGetEnrollmentsForTaxes: calling getEnrollmentsForTaxes() with order=aaIscrId")
            val orderedResult = api.tuitionFees.getEnrollmentsForTaxes(
                studentId = studentId,
                order = "aaIscrId"
            )
            logger.info("testGetEnrollmentsForTaxes: ordered result size=${orderedResult.size}")
            assertNotNull(orderedResult, "getEnrollmentsForTaxes() with order should not be null")
        } else {
            logger.info("testGetEnrollmentsForTaxes: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getEnrollmentsForTaxes(studentId = studentProfile.studentId)
            }
            logger.info("testGetEnrollmentsForTaxes: Esse3Exception thrown as expected")
        }
    }

    @Test
    fun testGetTrafficLightParameters() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.STUDENT, Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            val studentId = studentProfile.studentId

            logger.info("testGetTrafficLightParameters: calling getTrafficLightParameters(studentId=$studentId) with defaults")
            val defaultResult = api.tuitionFees.getTrafficLightParameters(studentId = studentId)
            logger.info("testGetTrafficLightParameters: result trafficLight=${defaultResult.trafficLight}, dueAmount=${defaultResult.dueAmount}, expiredTaxes size=${defaultResult.expiredTaxes.size}, dueTaxes size=${defaultResult.dueTaxes.size}")
            assertNotNull(defaultResult, "getTrafficLightParameters() should not return null")

            for (tax in defaultResult.expiredTaxes.take(3)) {
                logger.info("testGetTrafficLightParameters: expiredTax invoiceId=${tax.invoiceId}, taxCode=${tax.taxCode}, taxDescription=${tax.taxDescription}, itemAmount=${tax.itemAmount}")
            }

            for (tax in defaultResult.dueTaxes.take(3)) {
                logger.info("testGetTrafficLightParameters: dueTax invoiceId=${tax.invoiceId}, taxCode=${tax.taxCode}, taxDescription=${tax.taxDescription}, itemAmount=${tax.itemAmount}")
            }

            logger.info("testGetTrafficLightParameters: calling getTrafficLightParameters() with academicYearId=2025")
            val resultWithYear = api.tuitionFees.getTrafficLightParameters(
                studentId = studentId,
                academicYearId = 2025
            )
            logger.info("testGetTrafficLightParameters: result with academicYearId trafficLight=${resultWithYear.trafficLight}, dueAmount=${resultWithYear.dueAmount}")
            assertNotNull(resultWithYear, "getTrafficLightParameters() with academicYearId should not return null")

            logger.info("testGetTrafficLightParameters: calling getTrafficLightParameters() with referenceDate=2025-03-01")
            val resultWithDate = api.tuitionFees.getTrafficLightParameters(
                studentId = studentId,
                referenceDate = "2025-03-01"
            )
            logger.info("testGetTrafficLightParameters: result with referenceDate trafficLight=${resultWithDate.trafficLight}, dueAmount=${resultWithDate.dueAmount}")
            assertNotNull(resultWithDate, "getTrafficLightParameters() with referenceDate should not return null")
        } else {
            logger.info("testGetTrafficLightParameters: user lacks required permissions, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.getTrafficLightParameters(studentId = studentProfile.studentId)
            }
            logger.info("testGetTrafficLightParameters: Esse3Exception thrown as expected")
        }
    }

    @Test
    @Disabled("Irreversible mutating operation")
    fun testPostNotifyStatus() = runTest {
        val requiredPermissions = setOf(Esse3PermissionLevel.TECHNICAL_USER)
        if (userPermissions.any { it in requiredPermissions }) {
            logger.info("testPostNotifyStatus: user has TECHNICAL_USER permission, but test is disabled to prevent data mutation")
        } else {
            logger.info("testPostNotifyStatus: user lacks TECHNICAL_USER permission, expecting Esse3Exception")
            assertFailsWith<Esse3Exception> {
                api.tuitionFees.postNotifyStatus(
                    body = Esse3SpgTransactionStatusData(
                        eventDate = "2025-01-01T00:00:00",
                        channel = "TEST",
                        cart = Esse3Cart(
                            transactionId = "TEST-001"
                        )
                    )
                )
            }
            logger.info("testPostNotifyStatus: Esse3Exception thrown as expected")
        }
    }
}
