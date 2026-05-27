package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.tax.mapInvoices
import it.attendance100.mybicocca.data.mapper.tax.toIseeDeclaration
import it.attendance100.mybicocca.data.mapper.tax.toSummary
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PagoPATransaction
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import it.attendance100.mybicocca.domain.repository.TaxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : TaxRepository {

    // Esse3 student endpoints 403 without the id params: lista-fatture needs persId,
    // addebiti-studente / semaforo / parametri-iscrizioni need stuId (== CareerId.value).
    override suspend fun getInvoices(careerId: CareerId): List<TaxInvoice> {
        requireCareer(careerId)
        val personId = requirePersonId()
        val studentId = careerId.value
        val api = sessionManager.esse3()
        val (invoices, charges) = coroutineScope {
            val invoicesDef = async { api.tuitionFees.getInvoicesList(personId = personId) }
            val chargesDef = async { api.tuitionFees.getStudentChargesList(studentId = studentId) }
            invoicesDef.await() to chargesDef.await()
        }
        return withContext(Dispatchers.Default) { mapInvoices(invoices, charges) }
    }

    override suspend fun getSummary(careerId: CareerId): TaxSummary {
        requireCareer(careerId)
        return sessionManager.esse3().tuitionFees
            .getTrafficLightParameters(studentId = careerId.value)
            .toSummary()
    }

    override suspend fun getIseeDeclarations(careerId: CareerId): List<IseeDeclaration> {
        requireCareer(careerId)
        return sessionManager.esse3().tuitionFees
            .getEnrollmentsForTaxes(studentId = careerId.value)
            .map { it.toIseeDeclaration() }
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
            ?: error("pagoPA non ha restituito un link di pagamento.")
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

    // Fully reads a streamed PDF response into memory off the main thread.
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
