package it.attendance100.mybicocca.data.mapper.update

import it.attendance100.mybicocca.data.update.GithubReleaseDto
import it.attendance100.mybicocca.domain.model.update.AppRelease
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
    )
}
