package it.attendance100.mybicocca.domain.model.library

// A terms-of-use agreement the user must consent to before booking a seat.
data class LibraryAgreement(
    val id: Int,
    val name: String?,
    val url: String?,
    val mandatory: Boolean,
)
