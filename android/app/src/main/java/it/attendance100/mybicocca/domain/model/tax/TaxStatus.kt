package it.attendance100.mybicocca.domain.model.tax

/**
 * Payment lifecycle of an Esse3 tuition invoice, derived from the invoice's
 * paid/expired/cancelled flags. Drives the status badge on the registry "Tasse" sub-screen.
 */
enum class TaxStatus { PAID, PENDING, EXPIRED, CANCELED }
