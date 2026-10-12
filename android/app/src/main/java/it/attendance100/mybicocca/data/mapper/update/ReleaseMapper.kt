package it.attendance100.mybicocca.data.mapper.update

import it.attendance100.mybicocca.core.version.SemVer
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

/** A per-build nightly's tag: `0.0.6-nightly.12`, with the optional `v` the stable mapper allows. */
private val NIGHTLY_TAG_REGEX = Regex("""^[vV]?\d+\.\d+\.\d+-nightly\.\d+$""")

/**
 * The newest nightly among [this] releases, or null when there is none.
 *
 * Every nightly is its own pre-release, tagged `<next version>-nightly.<N>`, so the newest is the
 * highest by [SemVer] — not the most recently published, which a re-run of an older commit's
 * workflow could make it. Anything else in the list (stable releases, drafts, or the rolling
 * `nightly` tag from before per-build nightlies) is skipped.
 */
fun List<GithubReleaseDto>.latestNightlyOrNull(): GithubReleaseDto? =
    filter { !it.draft && it.prerelease && it.tagName?.trim()?.matches(NIGHTLY_TAG_REGEX) == true }
        .mapNotNull { dto -> SemVer.parse(dto.tagName)?.let { dto to it } }
        .maxByOrNull { (_, version) -> version }
        ?.first

/**
 * Maps a nightly [GithubReleaseDto] to an [AppRelease], or null when it is unusable — the same
 * rules as [toAppReleaseOrNull].
 *
 * - [AppRelease.versionName] is the tag without its `v` (e.g. "0.0.6-nightly.12"), which is also
 *   the running nightly's own version name, so the two compare directly.
 * - [AppRelease.title] falls back to "Nightly Build" when GitHub has no release name.
 * - [AppRelease.commitSha] is the first 7 chars of the SHA parsed from the release body
 *   (CI writes "commit: {full-sha}"). Null when absent.
 * - [AppRelease.isPreRelease] is always true.
 */
fun GithubReleaseDto.toNightlyAppReleaseOrNull(): AppRelease? {
    if (draft) return null
    val tag = tagName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val url = htmlUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val commitSha = body?.let { COMMIT_SHA_REGEX.find(it)?.groupValues?.getOrNull(1) }
        ?.take(7)
    return AppRelease(
        versionName = tag.removePrefix("v").removePrefix("V"),
        title = name?.trim()?.takeIf { it.isNotEmpty() } ?: "Nightly Build",
        notes = body?.trim().orEmpty(),
        pageUrl = url,
        publishedAt = publishedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
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
