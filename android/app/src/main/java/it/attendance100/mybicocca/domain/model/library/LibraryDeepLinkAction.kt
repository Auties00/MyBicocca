package it.attendance100.mybicocca.domain.model.library

/**
 * What an Affluences reservation email link asks the app to do.
 *
 * Bicocca libraries confirm and cancel seat reservations through links sent by email rather
 * than in-app codes; the app registers itself as a handler for those links and translates
 * them into these actions.
 */
enum class LibraryActionKind { Cancel }

/**
 * A parsed Affluences deep link, handed from the activity's intent to the Biblioteca flow.
 *
 * @property kind The action the link requests.
 * @property reservationToken The reservation's UUID token carried in the link's query string;
 * it is the credential Affluences accepts for confirm/cancel calls, distinct from the numeric
 * reservation id.
 */
data class LibraryDeepLinkAction(
    val kind: LibraryActionKind,
    val reservationToken: String,
)
