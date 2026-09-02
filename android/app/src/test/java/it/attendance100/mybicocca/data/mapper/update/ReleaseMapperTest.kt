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
            tagName = "nightly",
            name = "Nightly Build",
            body = "some changes\ncommit: a1b2c3d4e5f6g7h8",
            htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/nightly",
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

        val publishedMs = Instant.parse(dto.publishedAt).toEpochMilli()
        val release = dto.toNightlyAppReleaseOrNull(publishedMs)

        assertThat(release).isNotNull()
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
            tagName = "nightly",
            name = "Nightly",
            body = "no commit info here",
            htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/nightly",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = false,
            prerelease = true,
            assets = emptyList()
        )

        val publishedMs = Instant.parse(dto.publishedAt).toEpochMilli()
        val release = dto.toNightlyAppReleaseOrNull(publishedMs)

        assertThat(release).isNotNull()
        assertThat(release?.commitSha).isNull()
    }
}
