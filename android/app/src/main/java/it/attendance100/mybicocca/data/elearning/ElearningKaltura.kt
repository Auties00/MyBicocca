package it.attendance100.mybicocca.data.elearning

import it.attendance100.mybicocca.data.remote.elearning.api.KalturaPlaybackApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebMediaApi
import it.attendance100.mybicocca.data.remote.elearning.exception.KalturaException

/**
 * Playback of the Kaltura video resources (`mod/kalvidres`) of Moodle courses.
 *
 * Moodle embeds the lecture recordings through Kaltura's KAF in an LTI iframe, and no web
 * service maps an activity to its Kaltura entry. So the entry is read from the activity's page
 * ([resolveKalturaEntryIdForModule]), and its streams from Kaltura's CDN, which serves
 * Bicocca's entries without a Kaltura session ([getVideoStream]).
 *
 * @param pages The activity pages, sent with the user's browser session.
 * @param playback Kaltura's CDN.
 */
class ElearningKaltura(
    private val pages: MoodleWebMediaApi,
    private val playback: KalturaPlaybackApi,
) {
    /**
     * Reads the Kaltura entry of a video activity from its page. Every load logs a "viewed"
     * event in Moodle, so callers should keep the entry rather than read it again.
     *
     * @return The entry, or [ElearningKalturaEntryIdResponse.RequiresReauth] when the page had no
     *   player, typically because the session expired and Moodle served its login page.
     */
    suspend fun resolveKalturaEntryIdForModule(courseModuleId: Int): ElearningKalturaEntryIdResponse {
        val entryId = pages.getKalturaPlayer(courseModuleId).entryId ?: return ElearningKalturaEntryIdResponse.RequiresReauth
        return ElearningKalturaEntryIdResponse.Success(entryId)
    }

    /**
     * Looks up the streams of an entry by fetching its HLS master playlist, which both confirms
     * Kaltura still serves the entry and lists its quality renditions.
     *
     * @return The streams, or [ElearningKalturaVideoStreamResponse.Unavailable] when Kaltura no
     *   longer serves the entry.
     */
    suspend fun getVideoStream(kalturaEntryId: String, partnerId: Int = BICOCCA_PARTNER_ID): ElearningKalturaVideoStreamResponse {
        val playlist = try {
            playback.getHlsPlaylist(partnerId, kalturaEntryId)
        } catch (e: KalturaException) {
            if (e.statusCode == 404) return ElearningKalturaVideoStreamResponse.Unavailable
            throw e
        }
        return ElearningKalturaVideoStreamResponse.Success(
            kalturaEntryId = kalturaEntryId,
            partnerId = partnerId,
            hlsStreamUrl = playManifestUrl(kalturaEntryId, HLS_FORMAT, partnerId),
            dashStreamUrl = playManifestUrl(kalturaEntryId, DASH_FORMAT, partnerId),
            availableVideoVariants = variants(playlist),
        )
    }

    companion object {
        /**
         * Kaltura partner of the Università di Milano-Bicocca, the `oauth_consumer_key` of the
         * LTI launch form Moodle emits.
         */
        const val BICOCCA_PARTNER_ID = 2351962

        /** Kaltura's public CDN, which serves thumbnails and manifests. */
        private const val CDN_URL = "https://cdnapisec.kaltura.com"

        /** Delivery format and file name of the HLS master playlist. */
        private const val HLS_FORMAT = "applehttp/a.m3u8"

        /** Delivery format and file name of the MPEG-DASH manifest. */
        private const val DASH_FORMAT = "mpegdash/manifest.mpd"

        /** Matches one `KEY=VALUE` attribute of an `#EXT-X-STREAM-INF` tag. */
        private val STREAM_INF_ATTRIBUTE = Regex("""([A-Z-]+)=("[^"]*"|[^,]*)""")

        /** Captures the flavor asset id of a rendition URL. */
        private val FLAVOR_ID = Regex("""/flavorId/([\w-]+)/""")

        /**
         * The URL of an entry's thumbnail, which Kaltura resizes to the given size. Public,
         * sessionless and stable, so image loaders and players can fetch it.
         */
        fun thumbnailUrl(kalturaEntryId: String, partnerId: Int = BICOCCA_PARTNER_ID, widthPx: Int? = null, heightPx: Int? = null): String =
            buildString {
                append(CDN_URL).append("/p/").append(partnerId)
                append("/thumbnail/entry_id/").append(kalturaEntryId)
                if (widthPx != null) append("/width/").append(widthPx)
                if (heightPx != null) append("/height/").append(heightPx)
            }

        /**
         * The sessionless `playManifest` URL of an entry in [format] (such as `applehttp/a.m3u8`),
         * valid as long as the entry exists; Kaltura picks the renditions itself.
         */
        fun playManifestUrl(kalturaEntryId: String, format: String, partnerId: Int = BICOCCA_PARTNER_ID): String =
            "$CDN_URL/p/$partnerId/sp/${partnerId}00/playManifest/entryId/$kalturaEntryId/protocol/https/format/$format"

        /**
         * The renditions of an HLS master playlist, by bitrate: each `#EXT-X-STREAM-INF` tag
         * describes the rendition whose URL follows on the next line.
         */
        internal fun variants(playlist: String): List<ElearningKalturaVideoVariant> {
            val lines = playlist.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
            return lines.mapIndexedNotNull { index, line ->
                if (!line.startsWith("#EXT-X-STREAM-INF:")) return@mapIndexedNotNull null
                val attributes = STREAM_INF_ATTRIBUTE.findAll(line.substringAfter(':'))
                    .associate { it.groupValues[1] to it.groupValues[2].removeSurrounding("\"") }
                val resolution = attributes["RESOLUTION"]?.split('x')
                val uri = lines.getOrNull(index + 1)?.takeUnless { it.startsWith("#") }
                ElearningKalturaVideoVariant(
                    flavorId = uri?.let { FLAVOR_ID.find(it)?.groupValues?.get(1) },
                    pixelWidth = resolution?.getOrNull(0)?.toIntOrNull(),
                    pixelHeight = resolution?.getOrNull(1)?.toIntOrNull(),
                    bitrateKbps = attributes["BANDWIDTH"]?.toLongOrNull()?.let { (it / 1000).toInt() },
                    frameRateFps = attributes["FRAME-RATE"]?.toFloatOrNull(),
                )
            }.sortedBy { it.bitrateKbps ?: 0 }
        }
    }
}

/**
 * One quality rendition of a Kaltura entry, from an `#EXT-X-STREAM-INF` tag of its HLS master
 * playlist.
 *
 * @property flavorId The rendition's flavor asset (e.g. `1_2y8xr2cq`), when its URL names it.
 */
data class ElearningKalturaVideoVariant(
    val flavorId: String?,
    val pixelWidth: Int?,
    val pixelHeight: Int?,
    val bitrateKbps: Int?,
    val frameRateFps: Float?,
)

/** What looking up the streams of a Kaltura entry found. */
sealed interface ElearningKalturaVideoStreamResponse {
    /**
     * The entry is playable, from [hlsStreamUrl] (preferred by Media3) or [dashStreamUrl],
     * neither carrying a Kaltura session.
     *
     * @property availableVideoVariants The renditions the HLS playlist lists, by bitrate.
     */
    data class Success(
        val kalturaEntryId: String,
        val partnerId: Int,
        val hlsStreamUrl: String,
        val dashStreamUrl: String,
        val availableVideoVariants: List<ElearningKalturaVideoVariant>,
    ) : ElearningKalturaVideoStreamResponse

    /**
     * Kaltura no longer serves the entry, typically because it was moved to the recycle bin;
     * the Moodle activity still links to it, so retrying won't help.
     */
    data object Unavailable : ElearningKalturaVideoStreamResponse
}

/** What reading the Kaltura entry of a video activity found. */
sealed interface ElearningKalturaEntryIdResponse {
    /** The entry (e.g. `1_tb2txam7`). */
    data class Success(val kalturaEntryId: String) : ElearningKalturaEntryIdResponse

    /**
     * The page had no player, typically because the `MoodleSession` cookie expired. The caller
     * should renew the session and retry; if the retry fails too, the user most likely can't
     * access the course.
     */
    data object RequiresReauth : ElearningKalturaEntryIdResponse
}
