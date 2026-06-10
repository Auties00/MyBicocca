package it.attendance100.mybicocca.domain.usecase.tax

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.repository.TaxRepository
import javax.inject.Inject

/**
 * Loads the career's tuition invoices, each with its charge breakdown, when the registry
 * "Tasse" sub-screen opens or is refreshed. Fetches live from Esse3 — tax data is never cached.
 */
class GetTaxInvoicesUseCase @Inject constructor(
    private val repository: TaxRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<TaxInvoice> =
        repository.getInvoices(careerId)
}
