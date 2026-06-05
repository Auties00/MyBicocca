package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full information about a site as returned by the app API v4 (`GET /sites/{site}`).
 *
 * A site is any place monitored by Affluences: a library, museum, pool, administration office...
 * Sites can be hierarchical: a site like a university library system has child sites
 * (the individual libraries), discoverable through [AffluencesSiteCard] listings.
 *
 * @property id The UUID of the site.
 * @property slug The URL slug of the site (e.g. `universita-bicocca-biblioteca-di-ateneo`).
 * Endpoints accepting a site identifier accept either the UUID or the slug.
 * @property primaryName The main display name of the site.
 * @property secondaryName The secondary display name (e.g. the institution the site belongs to).
 * @property language The default language of the site (e.g. `it`).
 * @property timeZone The IANA time zone of the site (e.g. `Europe/Rome`). All date-time strings
 * returned by the site endpoints are local to this zone.
 * @property email The contact email of the site.
 * @property phone The contact phone number of the site.
 * @property websiteUrl The institutional website of the site.
 * @property booking Booking capabilities of the site.
 * @property publicationStatus The publication status of the site (e.g. `PUBLIC`, `RESTRICTED`).
 * @property address The full postal address of the site.
 * @property latitude The latitude of the site in decimal degrees.
 * @property longitude The longitude of the site in decimal degrees.
 * @property categories The categories the site belongs to.
 * @property pictures Absolute URLs of the site pictures.
 * @property pageLayout Which optional sections the site page should display, or null when the
 * default layout applies.
 * @property parent The parent site, or null when the site is a root site.
 */
@Serializable
data class AffluencesSite(
    @SerialName("id")
    val id: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("primaryName")
    val primaryName: String,
    @SerialName("secondaryName")
    val secondaryName: String? = null,
    @SerialName("lang")
    val language: String? = null,
    @SerialName("timeZone")
    val timeZone: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("websiteUrl")
    val websiteUrl: String? = null,
    @SerialName("booking")
    val booking: AffluencesBookingInfo? = null,
    @SerialName("publicationStatus")
    val publicationStatus: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
    @SerialName("categories")
    val categories: List<AffluencesSiteCategory> = emptyList(),
    @SerialName("pictures")
    val pictures: List<String> = emptyList(),
    @SerialName("pageLayout")
    val pageLayout: AffluencesPageLayout? = null,
    @SerialName("parent")
    val parent: AffluencesSiteCard? = null
)

/**
 * Booking capabilities of a site.
 *
 * @property hasBooking Whether the site supports booking through the Affluences reservation API.
 * @property bookingUrl The external booking URL, when booking happens outside Affluences.
 * @property ticketUrl The external ticketing URL, when the site sells tickets.
 */
@Serializable
data class AffluencesBookingInfo(
    @SerialName("hasBooking")
    val hasBooking: Boolean = false,
    @SerialName("bookingUrl")
    val bookingUrl: String? = null,
    @SerialName("ticketUrl")
    val ticketUrl: String? = null
)

/**
 * A category a site belongs to, as returned by the app API v4 (e.g. "Biblioteca universitaria").
 *
 * @property id The numeric identifier of the category.
 * @property name The singular display name of the category.
 * @property namePlural The plural display name of the category.
 */
@Serializable
data class AffluencesSiteCategory(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("namePlural")
    val namePlural: String? = null
)

/**
 * Optional page sections enabled for a site.
 *
 * A null flag means the server did not specify the section.
 *
 * @property timetables Whether the opening hours section should be displayed.
 * @property events Whether the events ("happening now") section should be displayed.
 */
@Serializable
data class AffluencesPageLayout(
    @SerialName("timetables")
    val timetables: Boolean? = null,
    @SerialName("events")
    val events: Boolean? = null
)

/**
 * Compact site representation used by the children, similar-sites, and parent listings
 * of the app API v4.
 *
 * @property id The UUID of the site.
 * @property slug The URL slug of the site.
 * @property primaryName The main display name of the site.
 * @property secondaryName The secondary display name of the site.
 * @property pictures Absolute URLs of the site pictures.
 * @property status The open/closed status of the site, when provided by the listing.
 * @property liveAttendance The real-time attendance of the site, or null when the site is closed
 * or does not publish live data.
 * @property booking Booking capabilities of the site, when provided by the listing.
 */
@Serializable
data class AffluencesSiteCard(
    @SerialName("id")
    val id: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("primaryName")
    val primaryName: String,
    @SerialName("secondaryName")
    val secondaryName: String? = null,
    @SerialName("pictures")
    val pictures: List<String> = emptyList(),
    @SerialName("status")
    val status: AffluencesSiteCardStatus? = null,
    @SerialName("liveAttendance")
    val liveAttendance: AffluencesLiveAttendance? = null,
    @SerialName("booking")
    val booking: AffluencesBookingInfo? = null
)

/**
 * Open/closed status attached to a [AffluencesSiteCard].
 *
 * Text fields are localized according to the `Accept-Language` of the request and are only
 * populated for the relevant state (opening texts when closed, closing texts when open).
 *
 * @property isOpen Whether the site is currently open.
 * @property openingText Localized text describing when the site opens (e.g. "Apre alle 08:30").
 * @property isOpeningSoon Whether the site is about to open.
 * @property closingText Localized text describing when the site closes.
 * @property isClosingSoon Whether the site is about to close.
 */
@Serializable
data class AffluencesSiteCardStatus(
    @SerialName("isOpen")
    val isOpen: Boolean,
    @SerialName("openingText")
    val openingText: String? = null,
    @SerialName("isOpeningSoon")
    val isOpeningSoon: Boolean? = null,
    @SerialName("closingText")
    val closingText: String? = null,
    @SerialName("isClosingSoon")
    val isClosingSoon: Boolean? = null
)

/**
 * An event happening at a site, as returned by the app API v4 (`GET /sites/{site}/events`).
 *
 * @property id The identifier of the event.
 * @property title The display title of the event.
 * @property descriptionHtml The HTML description of the event.
 * @property imageUrl Absolute URL of the event illustration.
 * @property url External URL with more information about the event.
 */
@Serializable
data class AffluencesEvent(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String? = null,
    @SerialName("descriptionHtml")
    val descriptionHtml: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("url")
    val url: String? = null
)

/**
 * A pop-in message a site wants to show to its visitors, as returned by the app API v4
 * (`GET /sites/{site}/messages`).
 *
 * @property contentHtml The HTML content of the message.
 * @property url External URL with more information.
 */
@Serializable
data class AffluencesPopInMessage(
    @SerialName("contentHtml")
    val contentHtml: String? = null,
    @SerialName("url")
    val url: String? = null
)
