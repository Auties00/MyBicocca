package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningKalturaApi.Companion.BICOCCA_PARTNER_ID
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaEntryIdResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaPlayableSource
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaPlaybackContextRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaPlaybackContextResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoVariant
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaWidgetSessionRequest
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaWidgetSessionResponse
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json

/**
 * API for playing Kaltura video resources (`mod/kalvidres`) found inside Moodle courses.
 *
 * ## Why this exists
 *
 * Bicocca's e-learning embeds lecture recordings via Kaltura's KAF (Kaltura Application
 * Framework). The official Moodle Kaltura plugin does **not** expose any web service
 * to map a Moodle course-module id (cmId) to a Kaltura entry id, nor to obtain a
 * Kaltura Session (KS). The only routes intended for end users go through an LTI launch
 * inside an iframe.
 *
 * This API skips the entire LTI iframe dance:
 *
 * 1. It reads the `mod/kalvidres/view.php` HTML once to extract the Kaltura entry id
 *    from the iframe URL (the only public surface where the cmId→entryId binding is
 *    observable — verified against the plugin source).
 * 2. It mints an anonymous Kaltura widget session — no shared secret, no LTI launch.
 * 3. It asks Kaltura for the playback context, yielding an HLS / DASH manifest URL
 *    ready to hand to Media3 / ExoPlayer.
 *
 * ## Browser session prerequisite
 *
 * `mod/kalvidres/view.php` is a Moodle web page (not a WS endpoint) and requires a live
 * `MoodleSession` browser cookie. The cookie is established as a side effect of
 * [ElearningAuthApi.login] (the SAML flow walks through Moodle's web layer before
 * issuing the WS token) and can be re-installed on cold process starts via
 * [ElearningApi.resume]. When the cookie is missing or has been expired by Moodle's
 * server-side `sessiontimeout`, `view.php` is rendered as a login redirect and the
 * iframe regex fails to match — the resolvers below surface this as
 * [ElearningKalturaVideoStreamResponse.RequiresReauth] /
 * [ElearningKalturaEntryIdResponse.RequiresReauth] so the caller can re-login and retry.
 * The API itself never re-authenticates: it is stateless and owns no credentials.
 *
 * ## Kaltura error handling
 *
 * Kaltura returns `KalturaAPIException` envelopes under HTTP 200, so each Kaltura-side
 * method returns a sealed response (`Success | Error`); [resolveVideoStreamForModule]
 * collapses them into [ElearningKalturaVideoStreamResponse.KalturaError] for the caller.
 *
 * @param client Shared Ktor client (must have `HttpCookies` installed so the Moodle
 *   session cookie travels with `view.php` requests).
 * @param json Shared kotlinx-serialization [Json].
 */
class ElearningKalturaApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    companion object {
        /**
         * Kaltura partner id for the Università di Milano-Bicocca tenant.
         *
         * Discovered as the `oauth_consumer_key` in the LTI launch form emitted by
         * Bicocca's Moodle. Constant per institution.
         */
        const val BICOCCA_PARTNER_ID = 2351962

        /** Base path of Kaltura's public REST API (v3). */
        private const val KALTURA_REST_API_BASE_URL = "https://www.kaltura.com/api_v3"

        /**
         * Builds Kaltura's CDN thumbnail URL for an entry. Public, sessionless and stable
         * across sessions — safe to hand to Coil or Media3 `MediaMetadata.artworkUri`.
         *
         * @param kalturaEntryId The Kaltura entry id, e.g. `"1_tb2txam7"`.
         * @param partnerId Kaltura partner id. Defaults to [BICOCCA_PARTNER_ID].
         * @param widthPx Optional target width — Kaltura resizes server-side.
         * @param heightPx Optional target height — Kaltura resizes server-side.
         */
        fun thumbnailUrl(
            kalturaEntryId: String,
            partnerId: Int = BICOCCA_PARTNER_ID,
            widthPx: Int? = null,
            heightPx: Int? = null,
        ): String = buildString {
            append("https://cdnapisec.kaltura.com/p/").append(partnerId)
            append("/thumbnail/entry_id/").append(kalturaEntryId)
            if (widthPx != null) append("/width/").append(widthPx)
            if (heightPx != null) append("/height/").append(heightPx)
        }

        /**
         * Locates the Kaltura entry id inside the kalvidres iframe URL embedded in
         * `mod/kalvidres/view.php`. The URL is rendered by `mod/kalvidres/renderer.php`
         * (line 60 of the plugin source) which has emitted the exact `entryid/{id}/`
         * pattern unchanged since the plugin's first release. This is the only place
         * the cmId→entryId mapping is reachable from outside Moodle's database.
         */
        private val KALTURA_ENTRY_ID_FROM_IFRAME_URL_REGEX = Regex("""entryid(?:/|%2F)([\w-]+)""")
    }

    /**
     * Resolves a Moodle kalvidres course-module id to a ready-to-play video stream.
     *
     * Performs three network round-trips on success:
     *
     * 1. `GET mod/kalvidres/view.php?id={courseModuleId}` (Moodle, requires
     *    `MoodleSession`) to extract the Kaltura entry id from the embedded iframe URL.
     * 2. `POST session.startWidgetSession` (Kaltura) for an anonymous KS.
     * 3. `POST baseEntry.getPlaybackContext` (Kaltura) for the HLS/DASH manifest URLs.
     *
     * @param courseModuleId Moodle course-module id of the kalvidres activity
     *   (`cm.id` in Moodle terms — the `id` query parameter on `view.php`).
     * @param partnerId Kaltura partner id. Defaults to [BICOCCA_PARTNER_ID]; override
     *   only if you ever target a different institution.
     * @return [ElearningKalturaVideoStreamResponse.Success] with playable URLs, or
     *   [ElearningKalturaVideoStreamResponse.RequiresReauth] if the Moodle session is
     *   missing or has expired.
     * @throws ElearningException If Kaltura returns a `KalturaAPIException` envelope
     *   from the widget-session or playback-context call (errorCode carries Kaltura's
     *   code), if Kaltura responds with a non-OK HTTP status, or if no HLS source is
     *   present in a successful playback context.
     */
    suspend fun resolveVideoStreamForModule(
        courseModuleId: Int,
        partnerId: Int = BICOCCA_PARTNER_ID,
    ): ElearningKalturaVideoStreamResponse {
        val entryId = when (val response = resolveKalturaEntryIdForModule(courseModuleId)) {
            is ElearningKalturaEntryIdResponse.Success -> response.kalturaEntryId
            ElearningKalturaEntryIdResponse.RequiresReauth -> return ElearningKalturaVideoStreamResponse.RequiresReauth
        }
        val widgetSession = when (val response = startAnonymousKalturaSession(partnerId)) {
            is ElearningKalturaWidgetSessionResponse.Success -> response
            is ElearningKalturaWidgetSessionResponse.Error -> throw ElearningException(
                errorCode = response.code,
                errorMessage = "Kaltura startWidgetSession: ${response.message} (status code ${response.code})",
            )
        }
        val playbackContext = when (val response = getKalturaPlaybackContext(entryId, widgetSession.kalturaSessionToken)) {
            is ElearningKalturaPlaybackContextResponse.Success -> response
            is ElearningKalturaPlaybackContextResponse.Error -> throw ElearningException(
                errorCode = response.code,
                errorMessage = "Kaltura getPlaybackContext: ${response.message} (status code ${response.code})",
            )
        }
        return composeVideoStream(entryId, widgetSession, playbackContext)
    }

    /**
     * Looks up the Kaltura entry id associated with a Moodle kalvidres course module
     * by reading the `view.php` page and matching the embedded iframe URL.
     *
     * Exposed publicly so callers can cache the cmId→entryId mapping locally (e.g. in
     * Room) and skip this round-trip on subsequent playback attempts.
     *
     * @param courseModuleId The Moodle course-module id of the kalvidres activity.
     * @return [ElearningKalturaEntryIdResponse.Success] with the entry id, or
     *   [ElearningKalturaEntryIdResponse.RequiresReauth] if the iframe URL could not be
     *   located in the response body (typically the Moodle session expired and Moodle
     *   served the login page instead of the kalvidres view).
     * @throws ElearningException If `view.php` returns a non-OK HTTP status.
     */
    suspend fun resolveKalturaEntryIdForModule(courseModuleId: Int): ElearningKalturaEntryIdResponse {
        val viewPageUrl = "${BASE_URL}mod/kalvidres/view.php?id=$courseModuleId"
        val response = client.get(viewPageUrl)
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(
                errorCode = null,
                errorMessage = "view.php returned ${response.status} for courseModuleId=$courseModuleId"
            )
        }
        val entryId = KALTURA_ENTRY_ID_FROM_IFRAME_URL_REGEX.find(response.bodyAsText())?.groupValues?.get(1)
            ?: return ElearningKalturaEntryIdResponse.RequiresReauth
        return ElearningKalturaEntryIdResponse.Success(entryId)
    }

    /**
     * Mints an anonymous Kaltura widget session (KS) for the given institution.
     *
     * A widget session is the lightest credential Kaltura issues: it requires no
     * shared secret, has read-only privileges, and is fine for the
     * `baseEntry.getPlaybackContext` call we need next. Bicocca's lecture entries
     * are not access-controlled, so anonymous read works.
     *
     * @param partnerId Kaltura partner id. Defaults to [BICOCCA_PARTNER_ID].
     * @return [ElearningKalturaWidgetSessionResponse.Success] with the KS, or
     *   [ElearningKalturaWidgetSessionResponse.Error] if Kaltura returned a
     *   `KalturaAPIException`.
     * @throws ElearningException If Kaltura responds with a non-OK HTTP status.
     */
    suspend fun startAnonymousKalturaSession(
        partnerId: Int = BICOCCA_PARTNER_ID
    ): ElearningKalturaWidgetSessionResponse {
        val endpoint = "$KALTURA_REST_API_BASE_URL/service/session/action/startWidgetSession?format=1"
        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(ElearningKalturaWidgetSessionRequest(anonymousWidgetId = "_$partnerId"))
        }
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(
                errorCode = null,
                errorMessage = "Kaltura startWidgetSession returned ${response.status}"
            )
        }
        return response.body()
    }

    /**
     * Asks Kaltura for the full playback context of a single entry: every available
     * source URL (HLS, DASH, progressive MP4, …) plus the underlying per-quality
     * flavor assets.
     *
     * Both arguments are required by the Kaltura API; the KS can be a widget KS from
     * [startAnonymousKalturaSession].
     *
     * @param kalturaEntryId The Kaltura entry id, e.g. `"1_tb2txam7"`.
     * @param kalturaSessionToken A valid KS — anonymous is enough for non-protected
     *   entries.
     * @return [ElearningKalturaPlaybackContextResponse.Success] with the available
     *   sources and flavor assets, or [ElearningKalturaPlaybackContextResponse.Error] if
     *   Kaltura returned a `KalturaAPIException`.
     * @throws ElearningException If Kaltura responds with a non-OK HTTP status.
     */
    suspend fun getKalturaPlaybackContext(
        kalturaEntryId: String,
        kalturaSessionToken: String
    ): ElearningKalturaPlaybackContextResponse {
        val endpoint = "$KALTURA_REST_API_BASE_URL/service/baseEntry/action/getPlaybackContext?format=1"
        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                ElearningKalturaPlaybackContextRequest(
                    kalturaEntryId = kalturaEntryId,
                    kalturaSessionToken = kalturaSessionToken,
                    contextDataParams = ElearningKalturaPlaybackContextRequest.ContextDataParams(
                        objectType = "KalturaContextDataParams",
                        flavorTags = "all",
                    ),
                )
            )
        }
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(
                errorCode = null,
                errorMessage = "Kaltura getPlaybackContext returned ${response.status} for entry $kalturaEntryId"
            )
        }
        return response.body()
    }

    /**
     * Picks HLS/DASH URLs out of an [ElearningKalturaPlaybackContextResponse.Success]
     * and combines them with the session metadata into a single
     * [ElearningKalturaVideoStreamResponse.Success].
     *
     * Prefers HTTPS over HTTP — Android refuses cleartext HTTP by default and Kaltura
     * always offers an HTTPS variant for these formats.
     *
     * @throws ElearningException If no HTTPS-capable `applehttp` source exists.
     */
    private fun composeVideoStream(
        kalturaEntryId: String,
        widgetSession: ElearningKalturaWidgetSessionResponse.Success,
        playbackContext: ElearningKalturaPlaybackContextResponse.Success,
    ): ElearningKalturaVideoStreamResponse.Success {
        val hlsSource = findHttpsSourceByDeliveryFormat(playbackContext.playableSources, "applehttp")
            ?: throw ElearningException(
                errorCode = null,
                errorMessage = "No HTTPS HLS source returned for entry $kalturaEntryId"
            )
        val dashSource = findHttpsSourceByDeliveryFormat(playbackContext.playableSources, "mpegdash")

        return ElearningKalturaVideoStreamResponse.Success(
            kalturaEntryId = kalturaEntryId,
            partnerId = widgetSession.partnerId,
            kalturaSessionToken = widgetSession.kalturaSessionToken,
            hlsStreamUrl = hlsSource.streamUrl,
            dashStreamUrl = dashSource?.streamUrl,
            availableVideoVariants = deduplicateAndSortVideoVariantsByBitrate(playbackContext.availableVideoVariants)
        )
    }

    /**
     * Returns the first source in [sources] that matches the requested
     * [deliveryFormat] and explicitly supports HTTPS, or `null` if none does.
     */
    private fun findHttpsSourceByDeliveryFormat(
        sources: List<ElearningKalturaPlayableSource>,
        deliveryFormat: String
    ): ElearningKalturaPlayableSource? =
        sources.firstOrNull { it.deliveryFormat == deliveryFormat && "https" in it.supportedProtocols }

    /**
     * Removes duplicate flavor entries (Kaltura occasionally lists the same flavor
     * twice in the same response) and sorts the remaining ones ascending by bitrate
     * so a quality picker can render them in a predictable order.
     */
    private fun deduplicateAndSortVideoVariantsByBitrate(
        variants: List<ElearningKalturaVideoVariant>
    ): List<ElearningKalturaVideoVariant> =
        variants.distinctBy { it.flavorId }.sortedBy { it.bitrateKbps ?: 0 }
}
