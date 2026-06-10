package it.attendance100.mybicocca.domain.model.elearning.video

/**
 * A playable Kaltura stream resolved for a kalvidres course module.
 *
 * Resolved on demand from the e-learning platform (Moodle's Kaltura integration) by the video
 * playback repository; streams are not cached in Room because their URLs are session-scoped.
 * Feeds the in-app video player and its system media notification.
 *
 * @property cmId The Moodle course-module id the stream was resolved for.
 * @property kalturaEntryId Kaltura's entry id, e.g. `1_tb2txam7`.
 * @property partnerId Kaltura partner (tenant) id the entry belongs to.
 * @property hlsUrl HLS manifest URL the player streams from.
 * @property dashUrl DASH manifest URL, when Kaltura offers one.
 * @property variants Discrete quality renditions of the entry, listed in the quality picker.
 * @property thumbnailUrl Public, sessionless CDN poster image for the entry, derived at map time
 * so consumers never assemble Kaltura URLs themselves; used as the media-notification artwork.
 */
data class VideoStream(
    val cmId: Int,
    val kalturaEntryId: String,
    val partnerId: Int,
    val hlsUrl: String,
    val dashUrl: String?,
    val variants: List<VideoVariant>,
    val thumbnailUrl: String,
)
