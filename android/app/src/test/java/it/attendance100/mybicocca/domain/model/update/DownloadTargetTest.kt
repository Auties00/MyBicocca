package it.attendance100.mybicocca.domain.model.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * [scopedTo] keeps a screen about one release from showing another release's download as its own
 * — above all from offering another release's APK under its name.
 */
class DownloadTargetTest {

    private val nightly = release(versionName = "0.0.6-nightly.26", commitSha = "aaa111")
    private val stable = release(versionName = "0.0.6", commitSha = "bbb222")
    private val apk = File("update.apk")

    @Test
    fun anotherReleasesDownloadedApk_readsAsIdle() {
        val state = DownloadState.Success(apk).scopedTo(DownloadTarget.of(nightly), stable)

        assertThat(state).isEqualTo(DownloadState.Idle)
    }

    @Test
    fun anotherReleasesProgress_readsAsIdle() {
        val state = DownloadState.Downloading(40).scopedTo(DownloadTarget.of(nightly), stable)

        assertThat(state).isEqualTo(DownloadState.Idle)
    }

    @Test
    fun theReleasesOwnState_passesThrough() {
        val success = DownloadState.Success(apk)

        assertThat(success.scopedTo(DownloadTarget.of(stable), stable)).isEqualTo(success)
    }

    @Test
    fun aStateWithNoTarget_readsAsIdle() {
        val state = DownloadState.InstallDeclined(apk).scopedTo(null, stable)

        assertThat(state).isEqualTo(DownloadState.Idle)
    }

    @Test
    fun theCommitDecides_whenTheReleaseNamesOne() {
        val sameVersionOtherBuild = DownloadTarget(stable.versionName, commitSha = "ccc333")

        assertThat(sameVersionOtherBuild.matches(stable)).isFalse()
    }

    @Test
    fun theVersionDecides_whenTheReleaseNamesNoCommit() {
        val noCommit = stable.copy(commitSha = null)

        assertThat(DownloadTarget(stable.versionName, commitSha = "ccc333").matches(noCommit)).isTrue()
        assertThat(DownloadTarget(nightly.versionName, commitSha = null).matches(noCommit)).isFalse()
    }

    private fun release(versionName: String, commitSha: String?) = AppRelease(
        versionName = versionName,
        title = versionName,
        notes = "",
        pageUrl = "https://example.test",
        publishedAt = null,
        isPreRelease = false,
        commitSha = commitSha,
    )
}
