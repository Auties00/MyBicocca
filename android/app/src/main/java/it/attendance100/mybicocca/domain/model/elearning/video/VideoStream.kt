package it.attendance100.mybicocca.domain.model.elearning.video

data class VideoStream(
    val cmId: Int,
    val kalturaEntryId: String,
    val partnerId: Int,
    val hlsUrl: String,
    val dashUrl: String?,
    val variants: List<VideoVariant>,
)
