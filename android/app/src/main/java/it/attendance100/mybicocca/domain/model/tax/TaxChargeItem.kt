package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

// A single "voce" within an invoice (Esse3 addebiti-studente line item).
data class TaxChargeItem(
    val description: String,
    val amount: Double,
    val installmentDescription: String?,
    val expiration: LocalDate?,
)
