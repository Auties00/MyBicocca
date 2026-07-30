package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.tax.TaxCacheDao
import it.attendance100.mybicocca.data.mapper.tax.mapInvoices
import it.attendance100.mybicocca.data.mapper.tax.toChargeItemEntities
import it.attendance100.mybicocca.data.mapper.tax.toDomain
import it.attendance100.mybicocca.data.mapper.tax.toEntity
import it.attendance100.mybicocca.data.mapper.tax.toIseeDeclaration
import it.attendance100.mybicocca.data.mapper.tax.toPaymentStatus
import it.attendance100.mybicocca.data.mapper.tax.toRefund
import it.attendance100.mybicocca.data.mapper.tax.toSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransaction
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import it.attendance100.mybicocca.domain.repository.TaxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live implementation over the Esse3 tuition-fees endpoints. The student endpoints 403
 * without the identifying params: lista-fatture and lista-rimborsi need persId,
 * addebiti-studente / semaforo / parametri-iscrizioni-per-tasse need stuId, which equals
 * CareerId.value. Both ids are resolved from the active account, and the requested career must
 * belong to it.
 *
 * List reads (invoices, summary, ISEE, refunds) hit the server first and are written through to
 * the offline tax mirror ([TaxCacheDao]); the mirror is served only when a live call fails with
 * an [IOException] (no network) so the taxes screens stay readable. An offline read with no
 * cached rows rethrows, surfacing the error state rather than a misleading empty list. Payment
 * state and the pagoPA actions are never cached — a stale paid/unpaid read is misleading and the
 * actions are disabled offline.
 */
@Singleton
class TaxRepositoryImpl @Inject constructor(
    private val stringResolver: StringResolver,
    private val sessionManager: SessionManager,
    private val taxCacheDao: TaxCacheDao,
) : TaxRepository {

    /**
     * Fetches lista-fatture (by persId) and addebiti-studente (by stuId) concurrently, then
     * joins the charge lines onto their invoices off the main thread, writing the result through
     * to the offline mirror. Offline, the cached invoices and their charge lines are read back
     * and rejoined.
     */
    override suspend fun getInvoices(careerId: CareerId): List<TaxInvoice> {
        requireCareer(careerId)
        val personId = requirePersonId()
        val studentId = careerId.value
        val api = sessionManager.esse3()
        return try {
            val (invoices, charges) = coroutineScope {
                val invoicesDef = async { api.tuitionFees.getInvoicesList(personId = personId) }
                val chargesDef = async { api.tuitionFees.getStudentChargesList(studentId = studentId) }
                invoicesDef.await() to chargesDef.await()
            }
            val mapped = withContext(Dispatchers.Default) { mapInvoices(invoices, charges) }
            taxCacheDao.replaceInvoices(
                careerId.value,
                mapped.mapIndexed { index, invoice -> invoice.toEntity(careerId, index) },
                mapped.flatMap { invoice -> invoice.toChargeItemEntities(careerId) },
            )
            mapped
        } catch (e: IOException) {
            val itemsByInvoice = taxCacheDao.getChargeItems(careerId.value)
                .groupBy { it.invoiceId }
            taxCacheDao.getInvoices(careerId.value)
                .map { invoice ->
                    invoice.toDomain(itemsByInvoice[invoice.invoiceId].orEmpty().map { it.toDomain() })
                }
                .ifEmpty { throw e }
        }
    }

    override suspend fun getSummary(careerId: CareerId): TaxSummary {
        requireCareer(careerId)
        return try {
            val summary = sessionManager.esse3().tuitionFees
                .getTrafficLightParameters(studentId = careerId.value)
                .toSummary()
            taxCacheDao.replaceSummary(careerId.value, summary.toEntity(careerId))
            summary
        } catch (e: IOException) {
            taxCacheDao.getSummary(careerId.value)?.toDomain() ?: throw e
        }
    }

    override suspend fun getIseeDeclarations(careerId: CareerId): List<IseeDeclaration> {
        requireCareer(careerId)
        return try {
            val declarations = sessionManager.esse3().tuitionFees
                .getEnrollmentsForTaxes(studentId = careerId.value)
                .map { it.toIseeDeclaration() }
            taxCacheDao.replaceIseeDeclarations(
                careerId.value,
                declarations.mapIndexed { index, declaration -> declaration.toEntity(careerId, index) },
            )
            declarations
        } catch (e: IOException) {
            taxCacheDao.getIseeDeclarations(careerId.value).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    override suspend fun startPagoPaPayment(
        careerId: CareerId,
        invoiceId: InvoiceId,
        returnUrl: String,
    ): String {
        requireCareer(careerId)
        val response = sessionManager.esse3().tuitionFees.postInitPagoPaTransaction(
            body = Esse3PagoPATransaction(invoiceId = invoiceId.value, returnURL = returnUrl),
        )
        return response.pagopaRedirectUrl?.takeIf { it.isNotBlank() }
            ?: error(stringResolver.getString(it.attendance100.mybicocca.R.string.tax_pagopa_no_link))
    }

    override suspend fun getPagoPaNotice(careerId: CareerId, invoiceId: InvoiceId): ByteArray {
        requireCareer(careerId)
        return sessionManager.esse3().tuitionFees
            .putPrintPagoPANotice(invoiceId.value)
            .drainToByteArray()
    }

    override suspend fun getPagoPaReceipt(
        careerId: CareerId,
        invoiceId: InvoiceId,
        language: String,
    ): ByteArray {
        requireCareer(careerId)
        return sessionManager.esse3().tuitionFees
            .getPagoPAReceipt(invoiceId.value, language)
            .drainToByteArray()
    }

    /**
     * Reads /pagopa/transazioni, which is STUDENT-accessible; lastTransaction=1 returns just
     * the latest attempt for this invoice, and an empty list means no pagoPA transaction yet.
     */
    override suspend fun getPaymentStatus(careerId: CareerId, invoiceId: InvoiceId): PaymentStatus? {
        requireCareer(careerId)
        return sessionManager.esse3().tuitionFees
            .getPagoPATransactions(invoiceId = invoiceId.value, lastTransaction = 1)
            .firstOrNull()
            ?.toPaymentStatus(invoiceId)
    }

    /** Reads lista-rimborsi with refundedFlag=2, which yields all refunds (refunded + pending). */
    override suspend fun getRefunds(careerId: CareerId): List<Refund> {
        requireCareer(careerId)
        val personId = requirePersonId()
        return try {
            val refunds = sessionManager.esse3().tuitionFees
                .getRefundsList(personId = personId, refundedFlag = 2)
                .map { it.toRefund() }
            taxCacheDao.replaceRefunds(
                careerId.value,
                refunds.mapIndexed { index, refund -> refund.toEntity(careerId, index) },
            )
            refunds
        } catch (e: IOException) {
            taxCacheDao.getRefunds(careerId.value).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    /** Fully reads a streamed PDF response into memory off the main thread. */
    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }

    private fun requireCareer(careerId: CareerId): Career {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve career for taxes.")
        return account.academic.careers.firstOrNull { it.id == careerId }
            ?: error("Career ${careerId.value} not found on active account.")
    }

    private fun requirePersonId(): Long {
        val account = sessionManager.activeAccount.value
            ?: error("No active account; cannot resolve personId for taxes.")
        return account.academic.personId
    }
}
