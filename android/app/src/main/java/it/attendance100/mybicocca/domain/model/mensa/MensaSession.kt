package it.attendance100.mybicocca.domain.model.mensa

/** Signed-in RistoCloud user session and live wallet balance. */
data class MensaSession(
    val email: String,
    val userName: String?,
    val barcode: String?,
    val walletBalance: Double?,
    val phone: String? = null,
)
