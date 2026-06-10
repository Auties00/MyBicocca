package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.PaymentStatus
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Checks the live pagoPA outcome of an invoice's latest transaction when the user taps
 * "verifica stato pagamento" on the registry "Tasse" sub-screen. Returns null when the invoice
 * has no pagoPA transaction yet.
 */
class GetPaymentStatusUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId, invoiceId: InvoiceId): PaymentStatus? =
        repository.getPaymentStatus(careerId, invoiceId)
}
