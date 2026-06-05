package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDataEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMapSearchResults
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesPlaylist
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSearchResults
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteFilters
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteSearchPage
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteSearchRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSuggestedCategoriesEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSuggestedCategory
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSuggestedPlaylistsEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSuggestedSite
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSuggestedSitesEnvelope
import kotlinx.serialization.json.Json

/**
 * API for search and discovery operations.
 *
 * Provides access to:
 * - Free-text search across sites, categories, and playlists
 * - Filtered and paginated site search
 * - Map-bounded site search
 * - Search filter metadata (categories, services)
 * - Home page suggestions (categories, playlists, sites)
 *
 * All operations are served by the app API v3 and require no authentication.
 */
class AffluencesSearchApi(
    client: HttpClient,
    json: Json
) : AffluencesAbstractApi(client, json) {

    /**
     * Searches sites, categories, and playlists matching a free-text query.
     *
     * @param query The free-text query (e.g. "bicocca")
     * @return The matching sites, categories, and playlists
     */
    suspend fun searchSites(query: String): AffluencesSearchResults =
        executeGet<AffluencesDataEnvelope<AffluencesSearchResults>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites/search",
            queryParams = mapOf("q" to query)
        ).data

    /**
     * Searches sites matching a combination of criteria, with pagination.
     *
     * Unlike [searchSites], results use the rich [it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteOverview]
     * representation, which includes the current occupancy forecast of each site.
     *
     * @param request The search criteria
     * @return One page of matching sites
     */
    suspend fun searchSitesFiltered(request: AffluencesSiteSearchRequest): AffluencesSiteSearchPage =
        executePost<AffluencesDataEnvelope<AffluencesSiteSearchPage>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites",
            body = request
        ).data

    /**
     * Searches sites for display on a map.
     *
     * @param request The search criteria. [AffluencesSiteSearchRequest.page] is ignored:
     * map results are not paginated.
     * @return The matching sites and the coordinates the map should center on
     */
    suspend fun searchSitesOnMap(request: AffluencesSiteSearchRequest): AffluencesMapSearchResults =
        executePost<AffluencesDataEnvelope<AffluencesMapSearchResults>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites/map",
            body = request
        ).data

    /**
     * Retrieves the filter metadata available for site searches: all categories and services
     * that can be referenced from a [AffluencesSiteSearchRequest].
     *
     * @return The available filters
     */
    suspend fun getSiteFilters(): AffluencesSiteFilters =
        executePost<AffluencesDataEnvelope<AffluencesSiteFilters>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites/filters",
            body = EmptyJsonBody
        ).data

    /**
     * Retrieves a playlist of sites by its identifier.
     *
     * @param playlistId The numeric identifier of the playlist
     * @return The playlist
     */
    suspend fun getPlaylist(playlistId: Int): AffluencesPlaylist =
        executeGet<AffluencesDataEnvelope<AffluencesPlaylist>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites/playlists/$playlistId"
        ).data

    /**
     * Retrieves the suggested category groups shown on the Affluences home page.
     *
     * @param timeZone The IANA time zone used to localize the suggestions (sent as the
     * `X-TIMEZONE` header)
     * @return The suggested category groups
     */
    suspend fun getSuggestedCategories(timeZone: String = "Europe/Rome"): List<AffluencesSuggestedCategory> =
        executeGet<AffluencesDataEnvelope<AffluencesSuggestedCategoriesEnvelope>>(
            baseUrl = APP_API_V3_URL,
            path = "/suggestions/categories",
            headers = mapOf("X-TIMEZONE" to timeZone)
        ).data.categories

    /**
     * Retrieves the suggested playlists shown on the Affluences home page.
     *
     * @param highlighted Whether to retrieve the highlighted playlists or the regular ones
     * @param timeZone The IANA time zone used to localize the suggestions (sent as the
     * `X-TIMEZONE` header)
     * @return The suggested playlists, or an empty list when there are none for the time zone
     */
    suspend fun getSuggestedPlaylists(
        highlighted: Boolean,
        timeZone: String = "Europe/Rome"
    ): List<AffluencesPlaylist> =
        executeGet<AffluencesDataEnvelope<AffluencesSuggestedPlaylistsEnvelope>>(
            baseUrl = APP_API_V3_URL,
            path = "/suggestions/playlists",
            queryParams = mapOf("highlighted" to highlighted),
            headers = mapOf("X-TIMEZONE" to timeZone)
        ).data.playlists

    /**
     * Retrieves the suggested sites shown on the Affluences home page.
     *
     * @param timeZone The IANA time zone used to localize the suggestions (sent as the
     * `X-TIMEZONE` header)
     * @return The suggested sites, or an empty list when there are none for the time zone
     */
    suspend fun getSuggestedSites(timeZone: String = "Europe/Rome"): List<AffluencesSuggestedSite> =
        executeGet<AffluencesDataEnvelope<AffluencesSuggestedSitesEnvelope>>(
            baseUrl = APP_API_V3_URL,
            path = "/suggestions/sites",
            headers = mapOf("X-TIMEZONE" to timeZone)
        ).data.sites

    private companion object {
        // POST /sites/filters expects a JSON body, even if empty
        private val EmptyJsonBody = emptyMap<String, String>()
    }
}
