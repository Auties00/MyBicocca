package it.attendance100.mybicocca.data.mapper

import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoVariant
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.model.elearning.video.VideoVariant
import java.time.Instant

internal fun ElearningKalturaVideoStreamResponse.Success.toDomain(cmId: Int): VideoStream =
    VideoStream(
        cmId = cmId,
        kalturaEntryId = kalturaEntryId,
        partnerId = partnerId,
        hlsUrl = hlsStreamUrl,
        dashUrl = dashStreamUrl,
        variants = availableVideoVariants.map { it.toDomain() },
    )

internal fun ElearningKalturaVideoVariant.toDomain(): VideoVariant =
    VideoVariant(
        flavorId = flavorId,
        widthPx = pixelWidth,
        heightPx = pixelHeight,
        bitrateKbps = bitrateKbps,
        frameRateFps = frameRateFps,
        sizeBytes = sizeBytes,
        fileExtension = fileExtension,
    )

internal fun VideoProgressEntity.toDomain(): VideoProgress =
    VideoProgress(
        cmId = cmId,
        courseId = courseId,
        positionMs = positionMs,
        durationMs = durationMs,
        completed = completed,
        lastUpdatedAt = Instant.ofEpochMilli(lastUpdatedAtMs),
    )

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
