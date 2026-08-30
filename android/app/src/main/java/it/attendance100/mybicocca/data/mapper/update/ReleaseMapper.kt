package it.attendance100.mybicocca.data.mapper.update

import it.attendance100.mybicocca.data.update.GithubReleaseDto
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import java.time.Instant

/**
 * Maps a GitHub release DTO to the domain [AppRelease], or null when it is unusable — a draft,
 * or missing the tag/page the rest of the feature depends on. The `v` prefix is stripped from
 * the tag for display and version comparison; the title falls back to the tag when GitHub has
 * no release name; the ISO-8601 publish timestamp is parsed best-effort.
 */
fun GithubReleaseDto.toAppReleaseOrNull(): AppRelease? {
    if (draft) return null
    val tag = tagName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val url = htmlUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val version = tag.removePrefix("v").removePrefix("V")
    return AppRelease(
        versionName = version,
        title = name?.trim()?.takeIf { it.isNotEmpty() } ?: tag,
        notes = body?.trim().orEmpty(),
        pageUrl = url,
        publishedAt = publishedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
        isPreRelease = prerelease,
        assets = assets.map { asset ->
            AppReleaseAsset(
                name = asset.name,
                downloadUrl = asset.browserDownloadUrl,
                size = asset.size,
                digest = asset.digest
            )
        }
    )
}

private val COMMIT_SHA_REGEX = Regex("""commit:\s*([0-9a-f]{7,40})""", RegexOption.IGNORE_CASE)

/**
 * Maps a "nightly" [GithubReleaseDto] to an [AppRelease].
 *
 * - [AppRelease.versionName] is set to the formatted publish date (e.g. "29 ago 2026"),
 *   since the tag is always "nightly" rather than a semver.
 * - [AppRelease.commitSha] is the first 7 chars of the SHA parsed from the release body
 *   (CI writes "commit: {full-sha}"). Null when absent.
 * - [AppRelease.isPreRelease] is always true.
 */
fun GithubReleaseDto.toNightlyAppReleaseOrNull(publishedAtMs: Long): AppRelease? {
    val url = htmlUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val publishedInstant = Instant.ofEpochMilli(publishedAtMs)
    val dateLabel = java.time.format.DateTimeFormatter
        .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM, java.time.format.FormatStyle.SHORT)
        .withZone(java.time.ZoneId.systemDefault())
        .format(publishedInstant)
    val commitSha = body?.let { COMMIT_SHA_REGEX.find(it)?.groupValues?.getOrNull(1) }
        ?.take(7)
    return AppRelease(
        versionName = dateLabel,
        title = "Nightly Build",
        notes = body?.trim().orEmpty(),
        pageUrl = url,
        publishedAt = publishedInstant,
        isPreRelease = true,
        assets = assets.map {
            AppReleaseAsset(
                name = it.name,
                downloadUrl = it.browserDownloadUrl,
                size = it.size,
                digest = it.digest
            )
        },
        commitSha = commitSha,
    )
}
