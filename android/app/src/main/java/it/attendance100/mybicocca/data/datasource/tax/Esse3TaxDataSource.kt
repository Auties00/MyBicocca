package it.attendance100.mybicocca.data.datasource.tax

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.dto.esse3.Esse3PagoPATransaction
import it.attendance100.mybicocca.data.dto.esse3.Esse3Invoices
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentDebit
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import it.attendance100.mybicocca.data.model.document.AppDocument
import it.attendance100.mybicocca.data.model.tax.Invoice
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.util.toAppDocument
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3TaxDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getStudentCharges(studentId: Long): List<TaxCharge> = withContext(ioDispatcher) {
        esse3Api.tuitionFees.getStudentChargesList(studentId = studentId)
            .mapNotNull { it.toTaxCharge(studentId) }
    }

    suspend fun getInvoices(personId: Long, careerId: Long): List<Invoice> =
        withContext(ioDispatcher) {
            esse3Api.tuitionFees.getInvoicesList(personId = personId)
                .mapNotNull { it.toInvoice(careerId) }
        }

    private fun Esse3StudentDebit.toTaxCharge(careerId: Long): TaxCharge? {
        val chargeId = invoiceId ?: return null
        return TaxCharge(
            id = chargeId,
            careerId = careerId,
            description = taxDescription ?: itemDescription ?: "",
            amount = invoiceAmount ?: 0.0,
            paidAmount = paidAmount ?: 0.0,
            dueDate = invoiceExpiration,
            paymentDate = paymentDate,
            status = when {
                canceledFlag == 1 -> "CANCELED"
                paidFlag == 1 -> "PAID"
                expiredFlag == 1 -> "EXPIRED"
                else -> "PENDING"
            },
            invoiceNumber = invoiceCode ?: chargeId.toString(),
            bulletinCode = bulletinNumber ?: noticeCode,
            modalita = collectedBy ?: if (iuv != null || noticeCode != null) "PagoPA" else null,
            rptStatus = iuv,
            isPagoPaEnabled = iuv != null || noticeCode != null,
        )
    }

    suspend fun getLatestIseeAcademicYearId(studentId: Long): Long? = withContext(ioDispatcher) {
        esse3Api.tuitionFees.getEnrollmentsForTaxes(studentId = studentId)
            .maxOfOrNull { it.academicYearEnrollmentId?.toLong() ?: 0L }
            ?.takeIf { it > 0L }
    }

    suspend fun getIseeDeclarations(academicYearId: Long): List<IseeDeclaration> =
        withContext(ioDispatcher) {
            val personId = authTokenStore.esse3PersonId
            if (personId == -1L) return@withContext emptyList()

            esse3Api.tuitionFees.getSelfCertification(
                personId = personId,
                academicYearId = academicYearId,
            ).mapNotNull { dto ->
                val id = dto.selfCertificationId ?: return@mapNotNull null
                IseeDeclaration(
                    id = id,
                    personId = dto.personId ?: personId,
                    academicYear = dto.academicYearId ?: academicYearId,
                    iseeValue = dto.isee,
                    ispeValue = dto.ispe,
                    bandDescription = dto.bandDescription,
                    presentationDate = dto.presentationDate,
                    familyMemberCount = dto.components.size,
                )
            }
        }

    private fun Esse3Invoices.toInvoice(careerId: Long): Invoice? {
        val invId = invoiceId ?: return null
        return Invoice(
            id = invId,
            careerId = careerId,
            description = mav1Description ?: "",
            amount = invoiceAmount ?: 0.0,
            issueDate = issuanceDate,
            paymentDate = paymentDate,
            status = when {
                canceledInvoice != null && canceledInvoice != 0L -> "CANCELED"
                paidFlag == 1 -> "PAID"
                expiredInvoiceFlag == 1 -> "EXPIRED"
                else -> "PENDING"
            },
            isPagoPaEnabled = pagopaEnabled == 1,
            isPagoPaImmediate = pagopaImmediate == 1,
            isPagoPaNotice = pagopaNotice == 1,
            noticeCode = noticeCode,
            iuv = iuv,
        )
    }

    suspend fun startPagoPaTransaction(
        invoiceId: Long,
        returnUrl: String,
    ): String = withContext(ioDispatcher) {
        esse3Api.tuitionFees.postInitPagoPaTransaction(
            body = Esse3PagoPATransaction(
                invoiceId = invoiceId,
                returnURL = returnUrl,
            ),
        ).pagopaRedirectUrl.orEmpty()
    }

    suspend fun getPagoPaNotice(invoiceId: Long): AppDocument = withContext(ioDispatcher) {
        esse3Api.tuitionFees.putPrintPagoPANotice(invoiceId)
            .toAppDocument(fileName = "avviso_pagopa_${invoiceId}.pdf")
    }

    suspend fun getPagoPaReceipt(
        invoiceId: Long,
        language: String = "it",
    ): AppDocument = withContext(ioDispatcher) {
        esse3Api.tuitionFees.getPagoPAReceipt(invoiceId, language)
            .toAppDocument(fileName = "quietanza_pagopa_${invoiceId}.pdf")
    }
}
