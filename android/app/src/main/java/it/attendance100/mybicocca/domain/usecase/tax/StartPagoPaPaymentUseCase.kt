package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Starts an immediate pagoPA payment for an invoice when the user taps "Paga" on the registry
 * "Tasse" sub-screen. Creates a pagoPA transaction on Esse3 and returns the checkout URL to
 * open in the browser; the return URL is where pagoPA redirects once the flow ends.
 */
class StartPagoPaPaymentUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId, invoiceId: InvoiceId, returnUrl: String): String =
        repository.startPagoPaPayment(careerId, invoiceId, returnUrl)
}
