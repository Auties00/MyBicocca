package it.attendance100.mybicocca.domain.model.library

/**
 * A terms-of-use agreement the user must consent to before booking a seat, from the Affluences
 * agreements endpoint; surfaced as the consent step of the Biblioteca booking flow.
 *
 * @property id Affluences agreement id.
 * @property name Agreement display name; null when absent.
 * @property url Link to the full agreement text; null when absent.
 * @property mandatory Whether consent is required to book.
 */
data class LibraryAgreement(
    val id: Int,
    val name: String?,
    val url: String?,
    val mandatory: Boolean,
)
