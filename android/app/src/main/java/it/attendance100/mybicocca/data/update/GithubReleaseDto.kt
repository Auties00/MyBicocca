package it.attendance100.mybicocca.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The subset of GitHub's release object the app reads
 * (`GET /repos/{owner}/{repo}/releases` and `/releases/latest`). Unknown fields are ignored by
 * the lenient JSON config on the client, so this stays small.
 *
 * @property tagName The git tag the release points at (e.g. "v0.1") — the version source.
 * @property name The release's display title; null/blank when the release was published untitled.
 * @property body The release notes (markdown). Null when the release has no notes.
 * @property htmlUrl The release's web page.
 * @property publishedAt ISO-8601 publish timestamp; null for an unpublished/draft state.
 * @property draft Whether this is an unpublished draft (excluded from what the user sees).
 * @property prerelease Whether GitHub flagged this as a pre-release.
 */
@Serializable
data class GithubReleaseDto(
    @SerialName("tag_name") val tagName: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("assets") val assets: List<GithubReleaseAssetDto> = emptyList(),
)
