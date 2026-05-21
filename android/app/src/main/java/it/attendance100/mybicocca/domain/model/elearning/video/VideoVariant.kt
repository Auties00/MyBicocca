package it.attendance100.mybicocca.domain.model.elearning.video

data class VideoVariant(
    val flavorId: String,
    val widthPx: Int?,
    val heightPx: Int?,
    val bitrateKbps: Int?,
    val frameRateFps: Float?,
    val sizeBytes: Long?,
    val fileExtension: String?,
)
