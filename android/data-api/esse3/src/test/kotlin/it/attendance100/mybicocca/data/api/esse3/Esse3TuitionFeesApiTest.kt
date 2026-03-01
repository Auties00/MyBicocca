package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3TuitionFeesApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3TuitionFeesApiTest::class.java.name)

    // Academic year IDs to probe for exemptions/fees queries
    private val academicYearIds = listOf(2023L, 2022L, 2021L, 2020L)

    // Entity code used for multiben payments (institution identifier in PagoPA ecosystem)
    private val multiBenEntity = "80054330167"

    @Test
    suspend fun testGetInvoicesList() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId)
            logger.info("getInvoicesList(personId=$personId): found ${invoices.size} invoices")
            for (invoice in invoices) {
                logger.info(
                    "  invoice: fattId=${invoice.invoiceId}, aaId=${invoice.academicYearId}, " +
                        "amount=${invoice.invoiceAmount}, paid=${invoice.paidFlag}, " +
                        "canceled=${invoice.canceledInvoice}, expiration=${invoice.invoiceExpiration}"
                )
                assertNotNull(invoice.invoiceId, "invoiceId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetInvoicesListWithFilters() {
        try {
            val personId = studentProfile.personId

            // Filter by paid flag
            val paidInvoices = api.tuitionFees.getInvoicesList(personId = personId, paidFlag = 1)
            logger.info("getInvoicesList(paidFlag=1): found ${paidInvoices.size} paid invoices")

            // Filter by not paid flag
            val unpaidInvoices = api.tuitionFees.getInvoicesList(personId = personId, paidFlag = 0)
            logger.info("getInvoicesList(paidFlag=0): found ${unpaidInvoices.size} unpaid invoices")

            // Filter by academic year
            for (aaId in academicYearIds.take(2)) {
                val yearInvoices = api.tuitionFees.getInvoicesList(personId = personId, academicYearId = aaId)
                logger.info("getInvoicesList(aaId=$aaId): found ${yearInvoices.size} invoices")
            }

            // With additional info
            val invoicesWithInfo = api.tuitionFees.getInvoicesList(personId = personId, retrieveAdditionalInfo = true)
            logger.info("getInvoicesList(retrieveAdditionalInfo=true): found ${invoicesWithInfo.size} invoices")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetInvoice() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId)
            if (invoices.isEmpty()) {
                logger.warning("No invoices found, skipping testGetInvoice")
                return
            }

            for (invoiceSummary in invoices) {
                val invoiceId = invoiceSummary.invoiceId ?: continue
                try {
                    val invoice = api.tuitionFees.getInvoice(invoiceId)
                    logger.info(
                        "getInvoice($invoiceId): aaId=${invoice.academicYearId}, " +
                            "amount=${invoice.invoiceAmount}, paid=${invoice.paidFlag}, " +
                            "expiration=${invoice.invoiceExpiration}, iuv=${invoice.iuv}"
                    )
                    assertNotNull(invoice.invoiceId, "invoiceId should not be null")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getInvoice($invoiceId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getInvoice($invoiceId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testGetInvoice: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPaymentsList() {
        try {
            val personId = studentProfile.personId
            val payments = api.tuitionFees.getPaymentsList(personId = personId)
            logger.info("getPaymentsList(personId=$personId): found ${payments.size} payments")
            for (payment in payments) {
                logger.info(
                    "  payment: fattId=${payment.invoiceId}, pagId=${payment.paymentId}, " +
                        "amount=${payment.paidAmount}, date=${payment.paymentDate}, iuv=${payment.iuv}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPaymentsList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPaymentsListByInvoice() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId, paidFlag = 1)
            if (invoices.isEmpty()) {
                logger.warning("No paid invoices found, skipping testGetPaymentsListByInvoice")
                return
            }

            for (invoiceSummary in invoices.take(3)) {
                val invoiceId = invoiceSummary.invoiceId ?: continue
                try {
                    val payments = api.tuitionFees.getPaymentsList(personId = personId, invoiceId = invoiceId)
                    logger.info("getPaymentsList(personId=$personId, invoiceId=$invoiceId): found ${payments.size} payments")
                    for (payment in payments) {
                        logger.info(
                            "  payment: pagId=${payment.paymentId}, amount=${payment.paidAmount}, " +
                                "date=${payment.paymentDate}"
                        )
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPaymentsList(invoiceId=$invoiceId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPaymentsList(invoiceId=$invoiceId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testGetPaymentsListByInvoice: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPersonChargesList() {
        try {
            val personId = studentProfile.personId
            val charges = api.tuitionFees.getPersonChargesList(personId = personId)
            logger.info("getPersonChargesList(personId=$personId): found ${charges.size} charges")
            for (charge in charges) {
                logger.info(
                    "  charge: fattId=${charge.invoiceId}, aaId=${charge.academicYearId}, " +
                        "amount=${charge.itemAmount}, paid=${charge.paidFlag}, canceled=${charge.canceledFlag}"
                )
                assertNotNull(charge.invoiceId, "invoiceId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPersonChargesList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPersonChargesListWithFilters() {
        try {
            val personId = studentProfile.personId

            // Filter by paid flag
            val paidCharges = api.tuitionFees.getPersonChargesList(personId = personId, paidFlag = 1)
            logger.info("getPersonChargesList(paidFlag=1): found ${paidCharges.size} paid charges")

            // Filter by unpaid flag
            val unpaidCharges = api.tuitionFees.getPersonChargesList(personId = personId, paidFlag = 0)
            logger.info("getPersonChargesList(paidFlag=0): found ${unpaidCharges.size} unpaid charges")

            // Filter by academic year
            for (aaId in academicYearIds.take(2)) {
                val yearCharges = api.tuitionFees.getPersonChargesList(personId = personId, academicYearId = aaId)
                logger.info("getPersonChargesList(aaId=$aaId): found ${yearCharges.size} charges")
            }

            // With pagination
            val pagedCharges = api.tuitionFees.getPersonChargesList(personId = personId, start = 0, limit = 5)
            logger.info("getPersonChargesList(paginated): found ${pagedCharges.size} charges (max 5)")
            assertTrue(pagedCharges.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPersonChargesList with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentChargesList() {
        try {
            val studentId = studentProfile.studentId
            val charges = api.tuitionFees.getStudentChargesList(studentId = studentId)
            logger.info("getStudentChargesList(studentId=$studentId): found ${charges.size} charges")
            for (charge in charges) {
                logger.info(
                    "  studentCharge: fattId=${charge.invoiceId}, aaId=${charge.academicYearId}, " +
                        "amount=${charge.itemAmount}, paid=${charge.paidFlag}, taxCode=${charge.taxCode}"
                )
                assertNotNull(charge.invoiceId, "invoiceId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentChargesList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentChargesListWithFilters() {
        try {
            val studentId = studentProfile.studentId

            // Filter by paid
            val paidCharges = api.tuitionFees.getStudentChargesList(studentId = studentId, paidFlag = 1)
            logger.info("getStudentChargesList(paidFlag=1): found ${paidCharges.size} paid student charges")

            // Filter by unpaid
            val unpaidCharges = api.tuitionFees.getStudentChargesList(studentId = studentId, paidFlag = 0)
            logger.info("getStudentChargesList(paidFlag=0): found ${unpaidCharges.size} unpaid student charges")

            // Filter by academic year
            for (aaId in academicYearIds.take(2)) {
                val yearCharges = api.tuitionFees.getStudentChargesList(studentId = studentId, academicYearId = aaId)
                logger.info("getStudentChargesList(aaId=$aaId): found ${yearCharges.size} student charges")
            }

            // With pagination
            val pagedCharges = api.tuitionFees.getStudentChargesList(studentId = studentId, start = 0, limit = 5)
            logger.info("getStudentChargesList(paginated): found ${pagedCharges.size} student charges (max 5)")
            assertTrue(pagedCharges.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentChargesList with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetRefundsList() {
        try {
            val personId = studentProfile.personId
            val refunds = api.tuitionFees.getRefundsList(personId = personId)
            logger.info("getRefundsList(personId=$personId): found ${refunds.size} refunds")
            for (refund in refunds) {
                logger.info(
                    "  refund: fattId=${refund.invoiceId}, amount=${refund.invoiceAmount}, " +
                        "refundedFlg=${refund.refundedFlag}, reason=${refund.refundReasonCode}, " +
                        "issuanceDate=${refund.issuanceDate}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRefundsList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetRefundsListWithFilter() {
        try {
            val personId = studentProfile.personId

            val refundedItems = api.tuitionFees.getRefundsList(personId = personId, refundedFlag = 1)
            logger.info("getRefundsList(refundedFlag=1): found ${refundedItems.size} refunded items")

            val pendingRefunds = api.tuitionFees.getRefundsList(personId = personId, refundedFlag = 0)
            logger.info("getRefundsList(refundedFlag=0): found ${pendingRefunds.size} pending refunds")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getRefundsList with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetEnrollmentsForTaxes() {
        try {
            val studentId = studentProfile.studentId
            val enrollments = api.tuitionFees.getEnrollmentsForTaxes(studentId = studentId)
            logger.info("getEnrollmentsForTaxes(studentId=$studentId): found ${enrollments.size} enrollments")
            for (enrollment in enrollments) {
                logger.info(
                    "  enrollment: aaIscrId=${enrollment.academicYearEnrollmentId}, " +
                        "stuId=${enrollment.studentId}, cdsId=${enrollment.courseOfStudyId}, " +
                        "isee=${enrollment.isee}, exemptionType=${enrollment.exemptionTypeCode}"
                )
                assertNotNull(enrollment.studentId, "studentId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getEnrollmentsForTaxes not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetEnrollmentsForTaxesWithAcademicYear() {
        try {
            val studentId = studentProfile.studentId
            val enrollments = api.tuitionFees.getEnrollmentsForTaxes(studentId = studentId)
            val firstEnrollment = enrollments.firstOrNull { it.academicYearEnrollmentId != null }
            if (firstEnrollment == null) {
                logger.warning("No enrollment with academicYearEnrollmentId found, skipping testGetEnrollmentsForTaxesWithAcademicYear")
                return
            }

            val aaIscrId = firstEnrollment.academicYearEnrollmentId!!
            val filteredEnrollments = api.tuitionFees.getEnrollmentsForTaxes(
                studentId = studentId,
                academicYearEnrollmentId = aaIscrId
            )
            logger.info(
                "getEnrollmentsForTaxes(studentId=$studentId, aaIscrId=$aaIscrId): " +
                    "found ${filteredEnrollments.size} enrollments"
            )
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getEnrollmentsForTaxes with academicYear not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTrafficLightParameters() {
        try {
            val studentId = studentProfile.studentId
            val trafficLight = api.tuitionFees.getTrafficLightParameters(studentId = studentId)
            logger.info(
                "getTrafficLightParameters(studentId=$studentId): " +
                    "trafficLight=${trafficLight.trafficLight}, dueAmount=${trafficLight.dueAmount}, " +
                    "expiredTaxes=${trafficLight.expiredTaxes.size}, dueTaxes=${trafficLight.dueTaxes.size}"
            )
            for (expiredTax in trafficLight.expiredTaxes) {
                logger.info(
                    "  expiredTax: fattId=${expiredTax.invoiceId}, taxCode=${expiredTax.taxCode}, " +
                        "amount=${expiredTax.itemAmount}, expiration=${expiredTax.expirationDate}"
                )
                assertNotNull(expiredTax.invoiceId, "expiredTax invoiceId should not be null")
            }
            for (dueTax in trafficLight.dueTaxes) {
                logger.info(
                    "  dueTax: fattId=${dueTax.invoiceId}, taxCode=${dueTax.taxCode}, " +
                        "amount=${dueTax.itemAmount}, expiration=${dueTax.expirationDate}"
                )
                assertNotNull(dueTax.invoiceId, "dueTax invoiceId should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTrafficLightParameters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTrafficLightParametersWithAcademicYear() {
        try {
            val studentId = studentProfile.studentId
            for (aaId in academicYearIds.take(2)) {
                try {
                    val trafficLight = api.tuitionFees.getTrafficLightParameters(
                        studentId = studentId,
                        academicYearId = aaId
                    )
                    logger.info(
                        "getTrafficLightParameters(studentId=$studentId, aaId=$aaId): " +
                            "trafficLight=${trafficLight.trafficLight}, dueAmount=${trafficLight.dueAmount}"
                    )
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTrafficLightParameters($aaId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getTrafficLightParameters with aaId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetSelfCertification() {
        try {
            val personId = studentProfile.personId
            for (aaId in academicYearIds.take(3)) {
                try {
                    val certs = api.tuitionFees.getSelfCertification(personId = personId, academicYearId = aaId)
                    logger.info("getSelfCertification(personId=$personId, aaId=$aaId): found ${certs.size} certifications")
                    for (cert in certs) {
                        logger.info(
                            "  selfCert: isee=${cert.isee}, aaId=${cert.academicYearId}, " +
                                "components=${cert.components.size}"
                        )
                    }
                } catch (e: Esse3ValidationException) {
                    logger.warning("getSelfCertification(aaId=$aaId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getSelfCertification not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetValidExemptionsAcademicYear() {
        for (aaId in academicYearIds) {
            try {
                val exemptions = api.tuitionFees.getValidExemptionsAcademicYear(academicYearId = aaId)
                logger.info("getValidExemptionsAcademicYear(aaId=$aaId): found ${exemptions.size} exemptions")
                for (exemption in exemptions) {
                    logger.info(
                        "  exemption: code=${exemption.exemptionCode}, desc=${exemption.exemptionDescription}, " +
                            "priority=${exemption.priority}, maxIsee=${exemption.maxIsee}"
                    )
                    assertNotNull(exemption.exemptionCode, "exemptionCode should not be null")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getValidExemptionsAcademicYear(aaId=$aaId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getValidExemptionsAcademicYear(aaId=$aaId) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetValidExemptionsAcademicYearWithPagination() {
        for (aaId in academicYearIds.take(2)) {
            try {
                val exemptions = api.tuitionFees.getValidExemptionsAcademicYear(
                    academicYearId = aaId,
                    start = 0,
                    limit = 5
                )
                logger.info("getValidExemptionsAcademicYear(aaId=$aaId, paginated): found ${exemptions.size} exemptions (max 5)")
                assertTrue(exemptions.size <= 5, "Paginated result should have at most 5 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getValidExemptionsAcademicYear(aaId=$aaId, paginated) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getValidExemptionsAcademicYear(aaId=$aaId, paginated) validation error: ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetStudentExemptionsList() {
        try {
            val studentId = studentProfile.studentId
            val exemptions = api.tuitionFees.getStudentExemptionsList(studentId = studentId)
            logger.info("getStudentExemptionsList(studentId=$studentId): found ${exemptions.size} exemptions")
            for (exemption in exemptions) {
                logger.info(
                    "  studentExemption: aaIscrId=${exemption.academicYearEnrollmentId}, " +
                        "exemptions=${exemption.exemptionList.size}, enrollmentTypeCode=${exemption.enrollmentTypeCode}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentExemptionsList not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetStudentExemptionsListWithAcademicYear() {
        try {
            val studentId = studentProfile.studentId
            val allExemptions = api.tuitionFees.getStudentExemptionsList(studentId = studentId)
            val firstExemption = allExemptions.firstOrNull { it.academicYearEnrollmentId != null }
            if (firstExemption == null) {
                logger.warning("No student exemptions with academicYearEnrollmentId found, testing with year IDs")
                return
            }

            val aaIscrId = firstExemption.academicYearEnrollmentId!!
            val filtered = api.tuitionFees.getStudentExemptionsList(
                studentId = studentId,
                academicYearEnrollmentId = aaIscrId
            )
            logger.info(
                "getStudentExemptionsList(studentId=$studentId, aaIscrId=$aaIscrId): " +
                    "found ${filtered.size} exemptions"
            )
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getStudentExemptionsList with aaIscrId not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPagoPATransactions() {
        try {
            val invoices = api.tuitionFees.getInvoicesList(personId = studentProfile.personId)
            val firstInvoice = invoices.firstOrNull { it.invoiceId != null }
            if (firstInvoice == null) {
                logger.warning("No invoices found to test getPagoPATransactions, testing with no filter")
            }

            // Test without filter
            try {
                val transactions = api.tuitionFees.getPagoPATransactions()
                logger.info("getPagoPATransactions (no filter): found ${transactions.size} transactions")
                for (transaction in transactions) {
                    logger.info(
                        "  transaction: fattId=${transaction.invoiceId}, " +
                            "iuv=${transaction.iuv}, state=${transaction.state}, " +
                            "amount=${transaction.amount}"
                    )
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getPagoPATransactions (no filter) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getPagoPATransactions (no filter) validation error: ${e.message}")
            }

            // Test filtering by invoiceId
            if (firstInvoice != null) {
                val invoiceId = firstInvoice.invoiceId!!
                try {
                    val transactionsForInvoice = api.tuitionFees.getPagoPATransactions(invoiceId = invoiceId)
                    logger.info(
                        "getPagoPATransactions(invoiceId=$invoiceId): " +
                            "found ${transactionsForInvoice.size} transactions"
                    )
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPagoPATransactions(invoiceId=$invoiceId) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPagoPATransactions(invoiceId=$invoiceId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testGetPagoPATransactions: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPagoPATransactionsWithFilters() {
        try {
            // Filter by finalState
            val finalTransactions = api.tuitionFees.getPagoPATransactions(finalState = 1)
            logger.info("getPagoPATransactions(finalState=1): found ${finalTransactions.size} final transactions")

            // Filter by lastTransaction
            val lastTransactions = api.tuitionFees.getPagoPATransactions(lastTransaction = 1)
            logger.info("getPagoPATransactions(lastTransaction=1): found ${lastTransactions.size} last transactions")

            // Filter by academicYear
            for (aaId in academicYearIds.take(2)) {
                try {
                    val yearTransactions = api.tuitionFees.getPagoPATransactions(academicYearId = aaId)
                    logger.info("getPagoPATransactions(aaId=$aaId): found ${yearTransactions.size} transactions")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPagoPATransactions(aaId=$aaId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPagoPATransactions with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testPutRequestPaymentStatus() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId)
            val unpaidInvoice = invoices.firstOrNull { it.invoiceId != null && it.paidFlag != 1 }
            if (unpaidInvoice == null) {
                logger.warning("No unpaid invoice found to test putRequestPaymentStatus")
                return
            }

            val invoiceId = unpaidInvoice.invoiceId!!
            try {
                val status = api.tuitionFees.putRequestPaymentStatus(invoiceId = invoiceId)
                logger.info(
                    "putRequestPaymentStatus($invoiceId): " +
                        "paid=${status.paid}, paymentAmount=${status.paymentAmount}, " +
                        "date=${status.paymentDate}"
                )
                assertNotNull(status, "Payment status should not be null")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("putRequestPaymentStatus($invoiceId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("putRequestPaymentStatus($invoiceId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testPutRequestPaymentStatus: ${e.message}")
        }
    }

    @Test
    suspend fun testPutPrintPagoPANotice() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId)
            val unpaidInvoice = invoices.firstOrNull { it.invoiceId != null && it.paidFlag != 1 && it.iuv != null }
            if (unpaidInvoice == null) {
                logger.warning("No unpaid invoice with IUV found for putPrintPagoPANotice test")
                return
            }

            val invoiceId = unpaidInvoice.invoiceId!!
            try {
                val notice = api.tuitionFees.putPrintPagoPANotice(invoiceId = invoiceId)
                logger.info("putPrintPagoPANotice($invoiceId): notice length=${notice.length}")
                assertNotNull(notice, "PagoPA notice should not be null")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("putPrintPagoPANotice($invoiceId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("putPrintPagoPANotice($invoiceId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testPutPrintPagoPANotice: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPagoPAReceipt() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId, paidFlag = 1)
            val paidInvoice = invoices.firstOrNull { it.invoiceId != null && it.iuv != null }
            if (paidInvoice == null) {
                logger.warning("No paid invoice with IUV found for getPagoPAReceipt test")
                return
            }

            val invoiceId = paidInvoice.invoiceId!!
            for (language in listOf("IT", "EN")) {
                try {
                    val receipt = api.tuitionFees.getPagoPAReceipt(invoiceId = invoiceId, language = language)
                    logger.info("getPagoPAReceipt($invoiceId, lang=$language): receipt length=${receipt.length}")
                    assertNotNull(receipt, "PagoPA receipt should not be null")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getPagoPAReceipt($invoiceId, lang=$language) not authorized: ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getPagoPAReceipt($invoiceId, lang=$language) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testGetPagoPAReceipt: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPagoPAYPayment() {
        try {
            val personId = studentProfile.personId
            val invoices = api.tuitionFees.getInvoicesList(personId = personId)
            val firstInvoice = invoices.firstOrNull { it.invoiceId != null }
            if (firstInvoice == null) {
                logger.warning("No invoice found to test getPagoPAYPayment")
                return
            }

            val invoiceId = firstInvoice.invoiceId!!
            try {
                val payment = api.tuitionFees.getPagoPAYPayment(invoiceId = invoiceId)
                logger.info(
                    "getPagoPAYPayment(invoiceId=$invoiceId): " +
                        "paid=${payment.paid}, paymentAmount=${payment.paymentAmount}, date=${payment.paymentDate}"
                )
                assertNotNull(payment, "PagoPA payment should not be null")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getPagoPAYPayment(invoiceId=$invoiceId) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getPagoPAYPayment(invoiceId=$invoiceId) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicesList not authorized, skipping testGetPagoPAYPayment: ${e.message}")
        }
    }

    @Test
    suspend fun testGetMultibenPayments() {
        try {
            val payments = api.tuitionFees.getMultibenPayments(entity = multiBenEntity)
            logger.info("getMultibenPayments(entity=$multiBenEntity): found ${payments.size} payments")
            for (payment in payments) {
                logger.info(
                    "  multibenPayment: iuv=${payment.iuv}, amount=${payment.paymentAmount}, " +
                        "date=${payment.paymentDate}, fiscalCode=${payment.fiscalCode}"
                )
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getMultibenPayments not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getMultibenPayments validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetMultibenPaymentsWithFilters() {
        try {
            for (aaId in academicYearIds.take(2)) {
                try {
                    val payments = api.tuitionFees.getMultibenPayments(
                        entity = multiBenEntity,
                        academicYearDebt = aaId
                    )
                    logger.info("getMultibenPayments(entity=$multiBenEntity, aaDebito=$aaId): found ${payments.size} payments")
                } catch (e: Esse3ValidationException) {
                    logger.warning("getMultibenPayments(aaDebito=$aaId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getMultibenPayments with filters not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetAdisurcMeritScholarships() {
        val fiscalCode = session.fiscalCode
        if (fiscalCode == null) {
            logger.warning("No fiscalCode in session, skipping testGetAdisurcMeritScholarships")
            return
        }

        val enrollments = try {
            api.tuitionFees.getEnrollmentsForTaxes(studentId = studentProfile.studentId)
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getEnrollmentsForTaxes not authorized, skipping testGetAdisurcMeritScholarships: ${e.message}")
            return
        }

        val firstEnrollment = enrollments.firstOrNull { it.academicYearEnrollmentId != null }
        if (firstEnrollment == null) {
            logger.warning("No enrollment with academicYearEnrollmentId found for testGetAdisurcMeritScholarships")
            return
        }

        val aaIscrId = firstEnrollment.academicYearEnrollmentId!!
        try {
            val merit = api.tuitionFees.getAdisurcMeritScholarships(
                fiscalCode = fiscalCode,
                academicYearEnrollmentId = aaIscrId
            )
            logger.info(
                "getAdisurcMeritScholarships(fiscalCode=$fiscalCode, aaIscrId=$aaIscrId): " +
                    "merit=${merit}"
            )
            assertNotNull(merit, "Merit scholarship data should not be null")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getAdisurcMeritScholarships not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getAdisurcMeritScholarships validation error: ${e.message}")
        }
    }

    @Test
    @Disabled("cancelInvoice is destructive: permanently cancels an invoice and cannot be reversed")
    suspend fun testCancelInvoice() {
        // Disabled: canceling an invoice is irreversible.
    }

    @Test
    @Disabled("putCancelPayment is destructive: cancels a payment validation and cannot be safely reversed")
    suspend fun testPutCancelPayment() {
        // Disabled: cancels a payment validation and cannot be safely reversed.
    }

    @Test
    @Disabled("postCreateStudentInvoice is destructive: creates an invoice record in the system and cannot be deleted without a dedicated endpoint")
    suspend fun testPostCreateStudentInvoice() {
        // Disabled: creates an invoice in the system.
    }

    @Test
    @Disabled("postCollection is a write op that inserts payment collection data and cannot be safely reversed")
    suspend fun testPostCollection() {
        // Disabled: inserts collection data that cannot be safely rolled back.
    }

    @Test
    @Disabled("postPayInvoice is destructive: marks an invoice as paid and cannot be safely reversed")
    suspend fun testPostPayInvoice() {
        // Disabled: marks an invoice as paid.
    }

    @Test
    @Disabled("postAcquireScholarshipOutcomeApplications is an admin write op that processes scholarship outcomes")
    suspend fun testPostAcquireScholarshipOutcomeApplications() {
        // Disabled: processes scholarship outcome applications in batch, not reversible.
    }

    @Test
    @Disabled("postAcquireExemptions is an admin write op that creates exemption records and cannot be safely reversed")
    suspend fun testPostAcquireExemptions() {
        // Disabled: creates exemption records in the system.
    }

    @Test
    @Disabled("rejectExemption is destructive: changes the state of an exemption and cannot be safely reversed")
    suspend fun testRejectExemption() {
        // Disabled: rejects an exemption, changing its state permanently.
    }

    @Test
    @Disabled("postInitPagoPaTransaction is a write op that initiates a PagoPA payment transaction")
    suspend fun testPostInitPagoPaTransaction() {
        // Disabled: initiates a real PagoPA payment transaction.
    }

    @Test
    @Disabled("postNotifyStatus is a write op that notifies payment status via SPG and cannot be safely called without a real transaction context")
    suspend fun testPostNotifyStatus() {
        // Disabled: sends a payment status notification to SPG, requires a real transaction context.
    }

    @Test
    @Disabled("postInvoiceAttachmentMetadata is a write op that creates attachment metadata for an invoice and cannot be safely reversed")
    suspend fun testPostInvoiceAttachmentMetadata() {
        // Disabled: creates attachment metadata in the system.
    }
}
