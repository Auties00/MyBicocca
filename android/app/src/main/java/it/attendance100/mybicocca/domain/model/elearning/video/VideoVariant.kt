package it.attendance100.mybicocca.domain.model.elearning.video

/**
 * A discrete quality rendition (Kaltura flavor) of a lecture video stream, offered by the
 * video player's quality picker sheet.
 *
 * @property flavorId Kaltura flavor asset id of this rendition, or a key derived from its
 *   resolution and bitrate when the stream doesn't name the flavor.
 * @property widthPx Frame width in pixels, when reported.
 * @property heightPx Frame height in pixels, when reported.
 * @property bitrateKbps Peak bitrate in kilobits per second, when reported.
 * @property frameRateFps Frame rate, when reported.
 */
data class VideoVariant(
    val flavorId: String,
    val widthPx: Int?,
    val heightPx: Int?,
    val bitrateKbps: Int?,
    val frameRateFps: Float?,
)
