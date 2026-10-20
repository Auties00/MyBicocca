package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningKalturaApi.Companion.BICOCCA_PARTNER_ID
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaEntryIdResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoVariant
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json

/**
 * API for playing Kaltura video resources (`mod/kalvidres`) found inside Moodle courses.
 *
 * ## Why this exists
 *
 * Bicocca's e-learning embeds lecture recordings via Kaltura's KAF (Kaltura Application
 * Framework). The Moodle Kaltura plugin exposes no web service that maps a Moodle
 * course-module id (cmId) to a Kaltura entry id: the module's `customdata` is empty, and the
 * only route meant for end users is an LTI launch inside an iframe.
 *
 * This API skips the LTI iframe:
 *
 * 1. [resolveKalturaEntryIdForModule] reads the `mod/kalvidres/view.php` HTML to extract the
 *    Kaltura entry id from the iframe URL, the only place the cmId→entryId binding is
 *    observable. Callers should cache the result: every load logs a "viewed" event in Moodle.
 * 2. [getVideoStream] builds the entry's `playManifest` URLs, which Kaltura serves without a
 *    Kaltura Session for Bicocca's entries, and reads the HLS master playlist for the quality
 *    renditions.
 *
 * ## Browser session prerequisite
 *
 * `mod/kalvidres/view.php` is a Moodle web page (not a WS endpoint) and requires a live
 * `MoodleSession` browser cookie, installed through the [ElearningApi] constructor. When the
 * cookie is missing or has been expired by Moodle's server-side `sessiontimeout`, `view.php`
 * is rendered as a login redirect and the iframe regex fails to match, which surfaces as
 * [ElearningKalturaEntryIdResponse.RequiresReauth] so the caller can renew the session and
 * retry. The API itself never re-authenticates: it is stateless and owns no credentials.
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

        /** Kaltura's public CDN, which serves thumbnails and manifests. */
        private const val KALTURA_CDN_BASE_URL = "https://cdnapisec.kaltura.com"

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
            append(KALTURA_CDN_BASE_URL).append("/p/").append(partnerId)
            append("/thumbnail/entry_id/").append(kalturaEntryId)
            if (widthPx != null) append("/width/").append(widthPx)
            if (heightPx != null) append("/height/").append(heightPx)
        }

        /**
         * Builds Kaltura's sessionless `playManifest` URL for an entry. Kaltura picks the
         * renditions itself; the URL stays valid as long as the entry exists.
         *
         * @param kalturaEntryId The Kaltura entry id, e.g. `"1_tb2txam7"`.
         * @param format The delivery format and file name, e.g. `"applehttp/a.m3u8"`.
         * @param partnerId Kaltura partner id. Defaults to [BICOCCA_PARTNER_ID].
         */
        fun playManifestUrl(
            kalturaEntryId: String,
            format: String,
            partnerId: Int = BICOCCA_PARTNER_ID,
        ): String = "$KALTURA_CDN_BASE_URL/p/$partnerId/sp/${partnerId}00/playManifest" +
            "/entryId/$kalturaEntryId/protocol/https/format/$format"

        /** Delivery format and file name of the HLS master playlist. */
        private const val HLS_FORMAT = "applehttp/a.m3u8"

        /** Delivery format and file name of the MPEG-DASH manifest. */
        private const val DASH_FORMAT = "mpegdash/manifest.mpd"

        /**
         * Locates the Kaltura entry id inside the kalvidres iframe URL embedded in
         * `mod/kalvidres/view.php`. The URL is rendered by `mod/kalvidres/renderer.php`
         * (line 60 of the plugin source) which has emitted the exact `entryid/{id}/`
         * pattern unchanged since the plugin's first release. This is the only place
         * the cmId→entryId mapping is reachable from outside Moodle's database.
         */
        private val KALTURA_ENTRY_ID_FROM_IFRAME_URL_REGEX = Regex("""entryid(?:/|%2F)([\w-]+)""")

        /** Matches one `KEY=VALUE` attribute of an `#EXT-X-STREAM-INF` tag. */
        private val STREAM_INF_ATTRIBUTE_REGEX = Regex("""([A-Z-]+)=("[^"]*"|[^,]*)""")

        /** Captures the flavor asset id from a Kaltura rendition URL. */
        private val FLAVOR_ID_REGEX = Regex("""/flavorId/([\w-]+)/""")
    }

    /**
     * Looks up the Kaltura entry id associated with a Moodle kalvidres course module
     * by reading the module's `view.php` page.
     *
     * Loading the page logs a "viewed" event for the module in Moodle, so callers should
     * cache the entry id rather than resolve it again.
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
     * Looks up the playable stream of a Kaltura entry by fetching its HLS master playlist,
     * which both confirms Kaltura still serves the entry and lists its quality renditions.
     *
     * @param kalturaEntryId The Kaltura entry id, e.g. `"1_tb2txam7"`.
     * @param partnerId Kaltura partner id. Defaults to [BICOCCA_PARTNER_ID].
     * @return [ElearningKalturaVideoStreamResponse.Success] with the manifest URLs, or
     *   [ElearningKalturaVideoStreamResponse.Unavailable] if Kaltura no longer serves the entry.
     * @throws ElearningException If Kaltura answers with any other non-OK HTTP status.
     */
    suspend fun getVideoStream(
        kalturaEntryId: String,
        partnerId: Int = BICOCCA_PARTNER_ID,
    ): ElearningKalturaVideoStreamResponse {
        val hlsUrl = playManifestUrl(kalturaEntryId, HLS_FORMAT, partnerId)
        val response = client.get(hlsUrl)
        when (response.status) {
            HttpStatusCode.OK -> Unit
            HttpStatusCode.NotFound -> return ElearningKalturaVideoStreamResponse.Unavailable
            else -> throw ElearningException(
                errorCode = null,
                errorMessage = "Kaltura playManifest returned ${response.status} for entry $kalturaEntryId"
            )
        }
        return ElearningKalturaVideoStreamResponse.Success(
            kalturaEntryId = kalturaEntryId,
            partnerId = partnerId,
            hlsStreamUrl = hlsUrl,
            dashStreamUrl = playManifestUrl(kalturaEntryId, DASH_FORMAT, partnerId),
            availableVideoVariants = parseMasterPlaylistVariants(response.bodyAsText())
        )
    }

    /**
     * Reads the renditions of an HLS master playlist: each `#EXT-X-STREAM-INF` tag describes
     * the rendition whose URL follows on the next line.
     */
    private fun parseMasterPlaylistVariants(playlist: String): List<ElearningKalturaVideoVariant> {
        val lines = playlist.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        return lines.mapIndexedNotNull { index, line ->
            if (!line.startsWith("#EXT-X-STREAM-INF:")) {
                return@mapIndexedNotNull null
            }
            val attributes = STREAM_INF_ATTRIBUTE_REGEX.findAll(line.substringAfter(':'))
                .associate { it.groupValues[1] to it.groupValues[2].removeSurrounding("\"") }
            val resolution = attributes["RESOLUTION"]?.split('x')
            val uri = lines.getOrNull(index + 1)?.takeUnless { it.startsWith("#") }
            ElearningKalturaVideoVariant(
                flavorId = uri?.let { FLAVOR_ID_REGEX.find(it)?.groupValues?.get(1) },
                pixelWidth = resolution?.getOrNull(0)?.toIntOrNull(),
                pixelHeight = resolution?.getOrNull(1)?.toIntOrNull(),
                bitrateKbps = attributes["BANDWIDTH"]?.toLongOrNull()?.let { (it / 1000).toInt() },
                frameRateFps = attributes["FRAME-RATE"]?.toFloatOrNull()
            )
        }.sortedBy { it.bitrateKbps ?: 0 }
    }
}
