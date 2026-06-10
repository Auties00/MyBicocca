package it.attendance100.mybicocca.domain.model.library

/**
 * A bookable university library — an Affluences "site". The two Bicocca libraries are children
 * of the library-system root site; only the ones that accept seat booking are surfaced. Rendered
 * as the landing cards and detail page of the Biblioteca flow (registry screen, services tab).
 *
 * @property id Affluences site UUID.
 * @property slug Affluences URL slug of the site.
 * @property name Primary display name.
 * @property secondaryName Secondary display name; null when the site has none.
 * @property address Street address; null in the light children listing.
 * @property latitude Site latitude; null in the light children listing.
 * @property longitude Site longitude; null in the light children listing.
 * @property pictureUrl URL of the site's first picture, used as the card image; null when absent.
 * @property phone Contact phone number; null when unpublished or in the light children listing.
 * @property email Contact e-mail; null when unpublished or in the light children listing.
 * @property websiteUrl Library website URL; null when unpublished or in the light children listing.
 * @property bookable Whether the site accepts seat booking; non-bookable sites are filtered out.
 * @property liveStatus Light status carried by the children listing, enough for the landing
 * cards; the full status (closing text plus hourly forecast) is fetched per library on demand.
 * Null when the listing carries no status.
 */
data class Library(
    val id: String,
    val slug: String,
    val name: String,
    val secondaryName: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val pictureUrl: String?,
    val phone: String?,
    val email: String?,
    val websiteUrl: String?,
    val bookable: Boolean,
    val liveStatus: LibraryLiveStatus?,
)
