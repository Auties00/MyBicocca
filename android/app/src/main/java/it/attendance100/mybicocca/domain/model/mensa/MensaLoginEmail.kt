package it.attendance100.mybicocca.domain.model.mensa

/**
 * The RistoCloud account is normally made with the university address, so a bare username
 * ("l.lupi3") is completed to it, as the university sign-in does. Anything with an `@` is left
 * exactly as typed, for people who registered with another address.
 */
fun String.asMensaLoginEmail(): String {
    val trimmed = trim()
    return if (trimmed.isEmpty() || '@' in trimmed) trimmed else "$trimmed@campus.unimib.it"
}
