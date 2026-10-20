package it.attendance100.mybicocca.data.remote.elearning.dto

/**
 * One quality rendition of a Kaltura entry, read from an `#EXT-X-STREAM-INF` line of the
 * entry's HLS master playlist.
 *
 * @property flavorId Kaltura flavor asset id of the rendition (e.g. `"1_2y8xr2cq"`), or null if
 *   the rendition URL doesn't carry one.
 * @property pixelWidth Frame width in pixels, from `RESOLUTION`.
 * @property pixelHeight Frame height in pixels, from `RESOLUTION`.
 * @property bitrateKbps Peak bitrate in kilobits per second, from `BANDWIDTH`.
 * @property frameRateFps Frame rate, from `FRAME-RATE`.
 */
data class ElearningKalturaVideoVariant(
    val flavorId: String?,
    val pixelWidth: Int?,
    val pixelHeight: Int?,
    val bitrateKbps: Int?,
    val frameRateFps: Float?,
)

/**
 * Result of looking up the playable stream of a Kaltura entry.
 */
sealed interface ElearningKalturaVideoStreamResponse {

    /**
     * The entry is playable.
     *
     * Pass [hlsStreamUrl] directly to a Media3 `HlsMediaSource`, or use [dashStreamUrl] as a
     * fallback. Neither URL carries a Kaltura session: Bicocca's entries play without one.
     *
     * @property kalturaEntryId Kaltura entry id for the video (`1_…` for Bicocca).
     * @property partnerId Kaltura partner (institution) the entry belongs to.
     * @property hlsStreamUrl HLS (`.m3u8`) master playlist URL, preferred for Media3 on Android.
     * @property dashStreamUrl MPEG-DASH (`.mpd`) manifest URL.
     * @property availableVideoVariants The renditions listed in the HLS master playlist,
     *   sorted ascending by bitrate.
     */
    data class Success(
        val kalturaEntryId: String,
        val partnerId: Int,
        val hlsStreamUrl: String,
        val dashStreamUrl: String,
        val availableVideoVariants: List<ElearningKalturaVideoVariant>,
    ) : ElearningKalturaVideoStreamResponse

    /**
     * Kaltura no longer serves the entry, typically because it was moved to the recycle bin
     * (the manifest answers 404 with `X-Kaltura: error-58`). The Moodle activity still links
     * to it, so retrying won't help.
     */
    data object Unavailable : ElearningKalturaVideoStreamResponse
}

/**
 * Result of resolving a Kaltura entry id for a kalvidres course module.
 */
sealed interface ElearningKalturaEntryIdResponse {
    /** The Kaltura entry id (e.g. `"1_tb2txam7"`). */
    data class Success(val kalturaEntryId: String) : ElearningKalturaEntryIdResponse

    /**
     * `mod/kalvidres/view.php` didn't contain the Kaltura iframe, typically because the
     * `MoodleSession` cookie expired and Moodle served its login page instead. The caller
     * should mint a new browser session, build a new
     * [it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi] with it, and retry.
     * If the retry still fails, the user most likely can't access the source course.
     */
    data object RequiresReauth : ElearningKalturaEntryIdResponse
}
