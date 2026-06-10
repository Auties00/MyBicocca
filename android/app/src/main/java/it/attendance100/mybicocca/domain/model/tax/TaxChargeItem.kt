package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

/**
 * A single charge line ("voce") within a tuition invoice, sourced from the Esse3
 * student-charges (addebiti-studente) endpoint and joined to its invoice by invoice id.
 * Rendered as the per-invoice breakdown on the registry "Tasse" sub-screen.
 *
 * @property description Human-readable description of the charge.
 * @property amount Charge amount in euro.
 * @property installmentDescription Label of the installment ("rata") the charge belongs to, when any.
 * @property expiration Due date of the charge, when Esse3 provides one.
 */
data class TaxChargeItem(
    val description: String,
    val amount: Double,
    val installmentDescription: String?,
    val expiration: LocalDate?,
)
