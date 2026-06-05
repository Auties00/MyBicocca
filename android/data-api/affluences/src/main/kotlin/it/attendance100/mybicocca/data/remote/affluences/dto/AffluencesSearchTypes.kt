package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rich site representation used by the app API v3 (`GET /sites/{site}`, `POST /sites`,
 * `POST /sites/map`).
 *
 * Unlike the v4 [AffluencesSite], this representation bundles the current occupancy forecast,
 * making it the cheapest way to answer "how busy is this site right now".
 *
 * @property id The UUID of the site.
 * @property slug The URL slug of the site.
 * @property parentSlug The slug of the parent site, or null when the site is a root site.
 * @property primaryName The main display name of the site.
 * @property secondaryName The secondary display name of the site.
 * @property concatName The concatenation of primary and secondary names.
 * @property categories The categories the site belongs to.
 * @property precisionLevel The precision of the attendance data published by the site.
 * Known values include `HIGHEST` (exact sensors) and `ESTIMATED` (statistical estimate).
 * @property timeZone The IANA time zone of the site.
 * @property language The default language of the site.
 * @property location The geographic location of the site.
 * @property phoneNumber The contact phone number of the site.
 * @property email The contact email of the site.
 * @property websiteUrl The institutional website of the site.
 * @property notices Notices the site wants to display prominently.
 * @property estimatedDistanceMeters The distance from the search coordinates in meters, populated
 * only by searches that include a latitude and longitude.
 * @property currentForecast The forecast for the current half-hour slot.
 * @property todayForecasts The hourly forecasts for the current day.
 * @property children The child sites, recursively using this same representation.
 * @property posterImage Absolute URL of the cover picture of the site.
 * @property images Absolute URLs of the site pictures.
 */
@Serializable
data class AffluencesSiteOverview(
    @SerialName("id")
    val id: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("parent")
    val parentSlug: String? = null,
    @SerialName("primary_name")
    val primaryName: String,
    @SerialName("secondary_name")
    val secondaryName: String? = null,
    @SerialName("concat_name")
    val concatName: String? = null,
    @SerialName("categories")
    val categories: List<AffluencesOverviewCategory> = emptyList(),
    @SerialName("affluence_precision_level")
    val precisionLevel: String? = null,
    @SerialName("time_zone")
    val timeZone: String? = null,
    @SerialName("lang")
    val language: String? = null,
    @SerialName("location")
    val location: AffluencesLocation? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("url")
    val websiteUrl: String? = null,
    @SerialName("notices")
    val notices: List<AffluencesOverviewNotice> = emptyList(),
    @SerialName("estimated_distance")
    val estimatedDistanceMeters: Double? = null,
    @SerialName("current_forecast")
    val currentForecast: AffluencesForecast? = null,
    @SerialName("today_forecasts")
    val todayForecasts: List<AffluencesHourlyForecast> = emptyList(),
    @SerialName("children")
    val children: List<AffluencesSiteOverview> = emptyList(),
    @SerialName("poster_image")
    val posterImage: String? = null,
    @SerialName("images")
    val images: List<String> = emptyList()
)

/**
 * A category a site belongs to, as returned by the app API v3.
 *
 * @property id The numeric identifier of the category.
 * @property name The singular display name of the category.
 * @property namePlural The plural display name of the category.
 */
@Serializable
data class AffluencesOverviewCategory(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("name_plural")
    val namePlural: String? = null
)

/**
 * The geographic location of a site, as returned by the app API v3.
 *
 * @property coordinates The geographic coordinates of the site.
 * @property address The postal address of the site.
 */
@Serializable
data class AffluencesLocation(
    @SerialName("coordinates")
    val coordinates: AffluencesCoordinates? = null,
    @SerialName("address")
    val address: AffluencesAddress? = null
)

/**
 * The postal address of a site.
 *
 * @property route The street name and number.
 * @property city The city name.
 * @property zipCode The postal code.
 * @property region The administrative region.
 * @property countryCode The ISO 3166-1 alpha-2 country code (e.g. `IT`).
 */
@Serializable
data class AffluencesAddress(
    @SerialName("route")
    val route: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("zip_code")
    val zipCode: String? = null,
    @SerialName("region")
    val region: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null
)

/**
 * A notice attached to a v3 site representation.
 *
 * @property message The plain-text content of the notice.
 * @property messageHtml The HTML content of the notice.
 * @property url External URL with more information.
 */
@Serializable
data class AffluencesOverviewNotice(
    @SerialName("message")
    val message: String? = null,
    @SerialName("message_html")
    val messageHtml: String? = null,
    @SerialName("url")
    val url: String? = null
)

/**
 * An occupancy forecast as returned by the app API v3.
 *
 * @property opened Whether the site is forecast to be open.
 * @property occupancyPercentage The forecast occupancy as a percentage (0-100).
 * @property waitingTimeMinutes The forecast waiting time in minutes.
 * @property waitingTimeOverflow Whether the waiting time exceeds the maximum the site can measure.
 * @property flow The forecast crowd level, for sites with estimated data.
 */
@Serializable
data class AffluencesForecast(
    @SerialName("opened")
    val opened: Boolean = false,
    @SerialName("occupancy")
    val occupancyPercentage: Int? = null,
    @SerialName("waiting_time")
    val waitingTimeMinutes: Int? = null,
    @SerialName("waiting_time_overflow")
    val waitingTimeOverflow: Boolean = false,
    @SerialName("flow")
    val flow: AffluencesCrowdLevel? = null
)

/**
 * An occupancy forecast bound to an hour of the current day.
 *
 * @property hour The start of the forecast slot, as an `HH:mm` time string local to the site
 * time zone.
 * @property forecast The forecast for the slot.
 */
@Serializable
data class AffluencesHourlyForecast(
    @SerialName("hour")
    val hour: String,
    @SerialName("forecast")
    val forecast: AffluencesForecast? = null
)

/**
 * Results of a free-text search across sites, categories, and playlists, as returned by the
 * app API v3 (`GET /sites/search?q=`).
 *
 * @property categories The categories matching the query.
 * @property sites The published sites matching the query.
 * @property playlists The playlists matching the query.
 * @property unreferencedSites The sites matching the query that are not published on the
 * Affluences website.
 */
@Serializable
data class AffluencesSearchResults(
    @SerialName("categories")
    val categories: List<AffluencesOverviewCategory> = emptyList(),
    @SerialName("sites")
    val sites: List<AffluencesSearchSite> = emptyList(),
    @SerialName("playlists")
    val playlists: List<AffluencesPlaylist> = emptyList(),
    @SerialName("unreferenced_sites")
    val unreferencedSites: List<AffluencesSearchSite> = emptyList()
)

/**
 * Compact site representation used by free-text search results.
 *
 * @property id The UUID of the site.
 * @property slug The URL slug of the site.
 * @property name The full display name of the site (primary and secondary names combined).
 * @property primaryName The main display name of the site.
 * @property secondaryName The secondary display name of the site.
 * @property description The short description of the site, usually its address.
 * @property imageUrl Absolute URL of the cover picture of the site.
 * @property images Absolute URLs of the site pictures.
 */
@Serializable
data class AffluencesSearchSite(
    @SerialName("id")
    val id: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("primary_name")
    val primaryName: String? = null,
    @SerialName("secondary_name")
    val secondaryName: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("images")
    val images: List<String> = emptyList()
)

/**
 * A curated playlist of sites (e.g. "what to do around").
 *
 * @property id The numeric identifier of the playlist.
 * @property name The localized display name of the playlist.
 * @property description The localized description of the playlist.
 * @property imageUrl Absolute URL of the playlist illustration.
 * @property icon Material icon name used by curated "what to do around" playlists.
 */
@Serializable
data class AffluencesPlaylist(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("icon")
    val icon: String? = null
)

/**
 * Request payload for the filtered site searches of the app API v3 (`POST /sites` and
 * `POST /sites/map`).
 *
 * All criteria are optional; null fields are omitted from the request. Criteria are combined
 * with a logical AND.
 *
 * @property searchQuery Free-text query matched against site names.
 * @property page The 0-based page to return (0 or null for the first page). Pagination applies
 * to `POST /sites` only.
 * @property subset Restricts the search to the given site UUIDs.
 * @property selectedCategories Restricts the search to sites belonging to any of the given
 * category identifiers (see [AffluencesSiteFilters.categories]).
 * @property selectedServices Restricts the search to sites offering all of the given service
 * identifiers (see [AffluencesSiteFilters.services]).
 * @property playlistId Restricts the search to sites belonging to the given playlist.
 * @property latitude Latitude used to sort results by distance and compute
 * [AffluencesSiteOverview.estimatedDistanceMeters].
 * @property longitude Longitude paired with [latitude].
 * @property openDate Restricts the search to sites open on the given `yyyy-MM-dd` date.
 * @property openTime Restricts the search to sites open at the given `HH:mm:ss` time,
 * paired with [openDate].
 */
@Serializable
data class AffluencesSiteSearchRequest(
    @SerialName("search_query")
    val searchQuery: String? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("subset")
    val subset: List<String>? = null,
    @SerialName("selected_categories")
    val selectedCategories: List<Int>? = null,
    @SerialName("selected_services")
    val selectedServices: List<Int>? = null,
    @SerialName("playlist_id")
    val playlistId: Int? = null,
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
    @SerialName("openDate")
    val openDate: String? = null,
    @SerialName("openTime")
    val openTime: String? = null
)

/**
 * A page of filtered site search results, as returned by the app API v3 (`POST /sites`).
 *
 * @property nextPage The 0-based index of the page following this one, to pass as
 * [AffluencesSiteSearchRequest.page] in the next request.
 * @property maxSize The maximum number of results per page. A page with fewer results than
 * this is the last page.
 * @property results The sites of this page.
 */
@Serializable
data class AffluencesSiteSearchPage(
    @SerialName("page")
    val nextPage: Int = 1,
    @SerialName("max_size")
    val maxSize: Int = 0,
    @SerialName("results")
    val results: List<AffluencesSiteOverview> = emptyList()
)

/**
 * Results of a map-bounded site search, as returned by the app API v3 (`POST /sites/map`).
 *
 * @property center The coordinates the map should center on.
 * @property results The sites matching the search.
 */
@Serializable
data class AffluencesMapSearchResults(
    @SerialName("center")
    val center: AffluencesCoordinates? = null,
    @SerialName("results")
    val results: List<AffluencesSiteOverview> = emptyList()
)

/**
 * The filter metadata available for site searches, as returned by the app API v3
 * (`POST /sites/filters`).
 *
 * @property categories All site categories, usable in
 * [AffluencesSiteSearchRequest.selectedCategories].
 * @property services All site services, usable in
 * [AffluencesSiteSearchRequest.selectedServices].
 */
@Serializable
data class AffluencesSiteFilters(
    @SerialName("categories")
    val categories: List<AffluencesOverviewCategory> = emptyList(),
    @SerialName("services")
    val services: List<AffluencesServiceFilter> = emptyList()
)

/**
 * A service usable as a site search filter.
 *
 * @property id The numeric identifier of the service.
 * @property name The localized display name of the service.
 */
@Serializable
data class AffluencesServiceFilter(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
)

/**
 * A suggested category group shown on the Affluences home page, as returned by the app API v3
 * (`GET /suggestions/categories`).
 *
 * @property name The localized display name of the group (e.g. "Biblioteca", "Sport").
 * @property imageUrl Absolute URL of the group illustration.
 * @property categoryIds The category identifiers grouped under this suggestion.
 */
@Serializable
data class AffluencesSuggestedCategory(
    @SerialName("name")
    val name: String,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("categoriesIds")
    val categoryIds: List<Int> = emptyList()
)

/**
 * A suggested site shown on the Affluences home page, as returned by the app API v3
 * (`GET /suggestions/sites`).
 *
 * The shape of this payload could not be fully verified against live data; all fields
 * are therefore optional.
 *
 * @property id The UUID of the site.
 * @property slug The URL slug of the site.
 * @property name The display name of the site.
 * @property imageUrl Absolute URL of the cover picture of the site.
 */
@Serializable
data class AffluencesSuggestedSite(
    @SerialName("id")
    val id: String? = null,
    @SerialName("slug")
    val slug: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null
)

/**
 * Envelope of the suggested playlists response.
 *
 * @property playlists The suggested playlists.
 */
@Serializable
internal class AffluencesSuggestedPlaylistsEnvelope(
    @SerialName("playlists")
    val playlists: List<AffluencesPlaylist> = emptyList()
)

/**
 * Envelope of the suggested sites response.
 *
 * @property sites The suggested sites.
 */
@Serializable
internal class AffluencesSuggestedSitesEnvelope(
    @SerialName("sites")
    val sites: List<AffluencesSuggestedSite> = emptyList()
)

/**
 * Envelope of the suggested categories response.
 *
 * @property categories The suggested category groups.
 */
@Serializable
internal class AffluencesSuggestedCategoriesEnvelope(
    @SerialName("categories")
    val categories: List<AffluencesSuggestedCategory> = emptyList()
)
