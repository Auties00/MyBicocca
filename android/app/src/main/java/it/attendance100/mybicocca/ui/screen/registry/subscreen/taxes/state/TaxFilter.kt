package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state

import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import java.time.LocalDate

/**
 * The two sub-lists the Tasse modal splits into, in footer/pager order. [ToPay] = still owed
 * (PENDING or its overdue EXPIRED variant); [Paid] = everything settled or closed (PAID, plus
 * CANCELED — voided invoices that no longer need paying; their card still reads "Annullata").
 */
enum class TaxFilter(val label: String) {
    ToPay("Da pagare"),
    Paid("Pagate"),
}

fun TaxInvoice.taxFilter(): TaxFilter = when (status) {
    TaxStatus.PENDING, TaxStatus.EXPIRED -> TaxFilter.ToPay
    TaxStatus.PAID, TaxStatus.CANCELED -> TaxFilter.Paid
}

/**
 * Partitions once into both buckets (always present, possibly empty), each sorted for its
 * bucket: unpaid by soonest expiration, settled by most recent.
 */
fun List<TaxInvoice>.groupByFilter(): Map<TaxFilter, List<TaxInvoice>> {
    val grouped = groupBy { it.taxFilter() }
    return TaxFilter.entries.associateWith { filter ->
        val items = grouped[filter].orEmpty()
        when (filter) {
            TaxFilter.ToPay -> items.sortedBy { it.expiration ?: LocalDate.MAX }
            TaxFilter.Paid -> items.sortedByDescending { it.paymentDate ?: it.issueDate ?: LocalDate.MIN }
        }
    }
}
