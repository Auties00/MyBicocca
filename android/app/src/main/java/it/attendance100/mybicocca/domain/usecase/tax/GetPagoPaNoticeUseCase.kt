package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Downloads the pagoPA payment notice ("avviso") PDF of an invoice when the user requests it
 * from the registry "Tasse" sub-screen, returning the raw bytes to view or share.
 */
class GetPagoPaNoticeUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId, invoiceId: InvoiceId): ByteArray =
        repository.getPagoPaNotice(careerId, invoiceId)
}
