package it.attendance100.mybicocca.data.mapper.update

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.update.GithubReleaseAssetDto
import it.attendance100.mybicocca.data.update.GithubReleaseDto
import org.junit.Test
import java.time.Instant

class ReleaseMapperTest {

    @Test
    fun toNightlyAppReleaseOrNull_validNightly_mapsCorrectly() {
        val dto = GithubReleaseDto(
            tagName = "0.0.6-nightly.12",
            name = "Nightly Build",
            body = "some changes\ncommit: a1b2c3d4e5f6g7h8",
            htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/0.0.6-nightly.12",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = false,
            prerelease = true,
            assets = listOf(
                GithubReleaseAssetDto(
                    name = "app-universal-release.apk",
                    browserDownloadUrl = "https://github.com/download/app-universal-release.apk",
                    size = 1000L,
                    contentType = "application/vnd.android.package-archive",
                    digest = "sha256:abcd"
                )
            )
        )

        val release = dto.toNightlyAppReleaseOrNull()

        assertThat(release).isNotNull()
        assertThat(release?.versionName).isEqualTo("0.0.6-nightly.12")
        assertThat(release?.publishedAt).isEqualTo(Instant.parse("2026-08-29T12:00:00Z"))
        assertThat(release?.title).isEqualTo("Nightly Build")
        assertThat(release?.commitSha).isEqualTo("a1b2c3d")
        assertThat(release?.isPreRelease).isTrue()
        assertThat(release?.pageUrl).isEqualTo(dto.htmlUrl)
        assertThat(release?.assets).hasSize(1)
        assertThat(release?.assets?.first()?.digest).isEqualTo("sha256:abcd")
    }

    @Test
    fun toNightlyAppReleaseOrNull_noCommitSha_mapsWithNullSha() {
        val dto = GithubReleaseDto(
            tagName = "v0.0.6-nightly.3",
            name = "Nightly",
            body = "no commit info here",
            htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/v0.0.6-nightly.3",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = false,
            prerelease = true,
            assets = emptyList()
        )

        val release = dto.toNightlyAppReleaseOrNull()

        assertThat(release).isNotNull()
        assertThat(release?.versionName).isEqualTo("0.0.6-nightly.3")
        assertThat(release?.commitSha).isNull()
    }

    @Test
    fun toNightlyAppReleaseOrNull_draft_isNull() {
        assertThat(nightly("0.0.6-nightly.3", draft = true).toNightlyAppReleaseOrNull()).isNull()
    }

    /** Highest by version, not first in the list: GitHub's order is by creation, re-runs included. */
    @Test
    fun latestNightlyOrNull_picksTheHighestNumberedNightly() {
        val releases = listOf(
            nightly("0.0.6-nightly.9"),
            nightly("0.0.6-nightly.12"),
            nightly("0.0.6-nightly.10"),
        )

        assertThat(releases.latestNightlyOrNull()?.tagName).isEqualTo("0.0.6-nightly.12")
    }

    @Test
    fun latestNightlyOrNull_nextBaseVersionWins() {
        val releases = listOf(nightly("0.0.6-nightly.40"), nightly("0.0.7-nightly.0"))

        assertThat(releases.latestNightlyOrNull()?.tagName).isEqualTo("0.0.7-nightly.0")
    }

    @Test
    fun latestNightlyOrNull_skipsEverythingThatIsNotAPublishedNightly() {
        val releases = listOf(
            nightly("nightly"), // the rolling tag from before per-build nightlies
            nightly("0.0.5", prerelease = false),
            nightly("0.0.6-beta.1"),
            nightly("0.0.6-nightly.99", draft = true),
            nightly("0.0.6-nightly.98", prerelease = false),
        )

        assertThat(releases.latestNightlyOrNull()).isNull()
    }

    private fun nightly(tag: String, prerelease: Boolean = true, draft: Boolean = false) =
        GithubReleaseDto(
            tagName = tag,
            name = "Nightly $tag",
            body = "commit: a1b2c3d",
            htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/$tag",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = draft,
            prerelease = prerelease,
        )
}
