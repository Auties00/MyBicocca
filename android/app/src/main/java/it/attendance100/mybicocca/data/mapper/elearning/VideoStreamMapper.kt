package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.elearning.ElearningKaltura
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoVariant
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.model.elearning.video.VideoVariant
import java.time.Instant

/**
 * Maps a resolved Kaltura stream response to the domain model, deriving the thumbnail
 * URL from the Kaltura entry and partner ids.
 */
internal fun ElearningKalturaVideoStreamResponse.Success.toDomain(cmId: Int): VideoStream =
    VideoStream(
        cmId = cmId,
        kalturaEntryId = kalturaEntryId,
        partnerId = partnerId,
        hlsUrl = hlsStreamUrl,
        dashUrl = dashStreamUrl,
        variants = availableVideoVariants.map { it.toDomain() },
        thumbnailUrl = ElearningKaltura.thumbnailUrl(kalturaEntryId, partnerId),
    )

/** Maps one Kaltura quality variant (flavor) to the domain model. */
internal fun ElearningKalturaVideoVariant.toDomain(): VideoVariant =
    VideoVariant(
        flavorId = flavorId ?: "${pixelWidth}x$pixelHeight@$bitrateKbps",
        widthPx = pixelWidth,
        heightPx = pixelHeight,
        bitrateKbps = bitrateKbps,
        frameRateFps = frameRateFps,
    )

/** Maps a cached playback-progress row to the domain model. */
internal fun VideoProgressEntity.toDomain(): VideoProgress =
    VideoProgress(
        cmId = cmId,
        courseId = courseId,
        positionMs = positionMs,
        durationMs = durationMs,
        completed = completed,
        lastUpdatedAt = Instant.ofEpochMilli(lastUpdatedAtMs),
    )

/** Maps a playback-progress snapshot to its cache row; progress is device-local only. */
internal fun VideoProgress.toEntity(accountId: AccountId): VideoProgressEntity =
    VideoProgressEntity(
        accountId = accountId.value,
        cmId = cmId,
        courseId = courseId,
        positionMs = positionMs,
        durationMs = durationMs,
        completed = completed,
        lastUpdatedAtMs = lastUpdatedAt.toEpochMilli(),
    )
