package it.attendance100.mybicocca.domain.model.elearning.video

/**
 * A discrete quality rendition (Kaltura flavor) of a lecture video stream, offered by the
 * video player's quality picker sheet.
 *
 * @property flavorId Kaltura flavor asset id selecting this rendition.
 * @property widthPx Frame width in pixels, when reported.
 * @property heightPx Frame height in pixels, when reported.
 * @property bitrateKbps Average bitrate in kilobits per second, when reported.
 * @property frameRateFps Frame rate, when reported.
 * @property sizeBytes Download size in bytes, when reported.
 * @property fileExtension Container file extension (e.g. mp4), when reported.
 */
data class VideoVariant(
    val flavorId: String,
    val widthPx: Int?,
    val heightPx: Int?,
    val bitrateKbps: Int?,
    val frameRateFps: Float?,
    val sizeBytes: Long?,
    val fileExtension: String?,
)
