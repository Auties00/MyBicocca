package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.badge.BadgeEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUserBadge
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

/**
 * Maps one badge of the Moodle user-badges web service into its cache row, preferring
 * the issue date over the creation date and treating non-positive course ids as
 * site-wide (null). Epoch-second timestamps are normalized to milliseconds.
 */
internal fun ElearningUserBadge.toEntity(accountId: AccountId): BadgeEntity =
    BadgeEntity(
        accountId = accountId.value,
        badgeId = id,
        name = name,
        description = description,
        imageUrl = badgeUrl,
        issuedAtMs = (dateIssued ?: timeCreated).toMillisOrNullSec(),
        courseId = courseId?.takeIf { it > 0 },
    )

/** Maps a cached badge row to the domain model. */
internal fun BadgeEntity.toDomain(): Badge =
    Badge(
        id = badgeId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        issuedAt = issuedAtMs?.let(Instant::ofEpochMilli),
        courseId = courseId?.let(::CourseId),
    )

/** Converts a Moodle epoch-second timestamp to milliseconds, reading 0 as absent. */
private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
