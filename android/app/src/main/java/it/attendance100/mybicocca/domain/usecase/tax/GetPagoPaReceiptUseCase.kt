package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Downloads the pagoPA payment receipt ("quietanza") PDF of a paid invoice when the user
 * requests it from the registry "Tasse" sub-screen, returning the raw bytes to view or share.
 */
class GetPagoPaReceiptUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        invoiceId: InvoiceId,
        language: String = "it",
    ): ByteArray = repository.getPagoPaReceipt(careerId, invoiceId, language)
}
