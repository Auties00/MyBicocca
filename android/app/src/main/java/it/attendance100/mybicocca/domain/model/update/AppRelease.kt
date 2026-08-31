package it.attendance100.mybicocca.domain.model.update

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * A published app release as seen on the distribution source (GitHub today). Backs the rows of
 * the What's New page, and the newest one also drives the "Update available" tile in the About
 * modal.
 *
 * @property versionName The release's version tag, normalized of any `v` prefix (e.g. "0.1").
 * @property title The release's display name; falls back to the version tag when GitHub has none.
 * @property notes The release body — the changelog/commit summary written at release time. May
 *   be blank when a release ships without notes.
 * @property pageUrl The canonical web page for this release (GitHub `html_url`), opened when the
 *   user taps a "What's New" entry or the update tile on a sideloaded build.
 * @property publishedAt When the release was published, or null if the source omitted it.
 * @property isPreRelease Whether the source flagged this as a pre-release.
 */
data class AppRelease(
    val versionName: String,
    val title: String,
    val notes: String,
    val pageUrl: String,
    val publishedAt: Instant?,
    val isPreRelease: Boolean,
    val assets: List<AppReleaseAsset> = emptyList(),
    val commitSha: String? = null
)

/** Whether an already-downloaded [AppRelease] should install without an explicit user tap. */
fun AppRelease.shouldInstallSilently(nightlyAutoInstall: Boolean): Boolean =
    isPreRelease && nightlyAutoInstall

/** Whether [AppRelease] should be downloaded *and* installed with zero user interaction. */
fun AppRelease.shouldRunFullyUnattended(nightlyAutoDownload: Boolean, nightlyAutoInstall: Boolean): Boolean =
    isPreRelease && nightlyAutoDownload && nightlyAutoInstall

@Serializable
data class AppReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val digest: String? = null // Expected content hash from the distribution source, formatted "sha256:<hex>
)
