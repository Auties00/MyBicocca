package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDataEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesLiveData
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEvent
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesPopInMessage
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSite
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteCard
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteDetails
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteOverview
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesWeekTimetable
import kotlinx.serialization.json.Json

/**
 * API for site page operations.
 *
 * Provides access to everything the Affluences website shows on a site page:
 * - Site information (names, contacts, location, categories, pictures)
 * - Real-time occupancy and per-day forecasts
 * - Weekly opening hours
 * - Practical details (services, access conditions)
 * - Related sites (children, similar)
 * - Events and pop-in messages
 *
 * Every method accepts a site identifier, which can be either the UUID or the URL slug
 * of the site (e.g. `universita-bicocca-biblioteca-di-ateneo`).
 */
class AffluencesSitesApi(
    client: HttpClient,
    json: Json
) : AffluencesAbstractApi(client, json) {

    /**
     * Retrieves the full information of a site.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The site information
     */
    suspend fun getSite(siteIdentifier: String): AffluencesSite =
        executeGet<AffluencesDataEnvelope<AffluencesSite>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier"
        ).data

    /**
     * Retrieves the real-time data of a site: current status, live attendance, critical
     * notices, and occupancy forecasts.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The live data of the site
     */
    suspend fun getLiveData(siteIdentifier: String): AffluencesLiveData =
        executeGet<AffluencesDataEnvelope<AffluencesLiveData>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/live-data"
        ).data

    /**
     * Retrieves the opening hours of a site for one week.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @param weekOffset The week to retrieve, as an offset from the current week
     * (0 = this week, 1 = next week...)
     * @return The weekly timetable
     */
    suspend fun getWeekTimetable(siteIdentifier: String, weekOffset: Int = 0): AffluencesWeekTimetable =
        executeGet<AffluencesDataEnvelope<AffluencesWeekTimetable>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/timetables",
            queryParams = mapOf("weekOffset" to weekOffset)
        ).data

    /**
     * Retrieves the practical details of a site: available services and free-form
     * information sections.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The site details
     */
    suspend fun getDetails(siteIdentifier: String): AffluencesSiteDetails =
        executeGet<AffluencesDataEnvelope<AffluencesSiteDetails>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/details"
        ).data

    /**
     * Retrieves the child sites of a site (e.g. the individual libraries of a library system).
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The child sites, or an empty list when the site has no children
     */
    suspend fun getChildren(siteIdentifier: String): List<AffluencesSiteCard> =
        executeGet<AffluencesDataEnvelope<List<AffluencesSiteCard>>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/children"
        ).data

    /**
     * Retrieves sites similar to a site, as suggested by Affluences.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The similar sites, or an empty list when there are none
     */
    suspend fun getSimilarSites(siteIdentifier: String): List<AffluencesSiteCard> =
        executeGet<AffluencesDataEnvelope<List<AffluencesSiteCard>>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/similar"
        ).data

    /**
     * Retrieves the events currently happening at a site.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The events, or an empty list when there are none
     */
    suspend fun getEvents(siteIdentifier: String): List<AffluencesEvent> =
        executeGet<AffluencesDataEnvelope<List<AffluencesEvent>>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/events"
        ).data

    /**
     * Retrieves the pop-in messages of a site.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @param lastCheckin When provided, only messages published after this checkpoint are
     * returned. The website passes the timestamp of the previous visit.
     * @return The messages, or an empty list when there are none
     */
    suspend fun getMessages(siteIdentifier: String, lastCheckin: String? = null): List<AffluencesPopInMessage> =
        executeGet<AffluencesDataEnvelope<List<AffluencesPopInMessage>>>(
            baseUrl = APP_API_V4_URL,
            path = "/sites/$siteIdentifier/messages",
            queryParams = buildMap {
                if (lastCheckin != null) {
                    put("lastCheckin", lastCheckin)
                }
            }
        ).data

    /**
     * Retrieves the rich legacy representation of a site from the app API v3.
     *
     * Unlike [getSite], this representation bundles the current occupancy forecast and the
     * hourly forecasts of the day, making it the cheapest way to answer "how busy is this
     * site right now" with a single request.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The site overview
     */
    suspend fun getSiteOverview(siteIdentifier: String): AffluencesSiteOverview =
        executeGet<AffluencesDataEnvelope<AffluencesSiteOverview>>(
            baseUrl = APP_API_V3_URL,
            path = "/sites/$siteIdentifier"
        ).data
}
