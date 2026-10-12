package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.data.local.settings.PersistedNightlyState
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.update.GithubReleaseApi
import it.attendance100.mybicocca.data.update.GithubReleaseAssetDto
import it.attendance100.mybicocca.data.update.GithubReleaseDto
import it.attendance100.mybicocca.data.update.InstallSourceProvider
import it.attendance100.mybicocca.domain.model.update.DistributionSource
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateRepositoryImplTest {

    private lateinit var api: GithubReleaseApi
    private lateinit var store: UpdateStateStore
    private lateinit var provider: InstallSourceProvider
    private lateinit var repository: UpdateRepositoryImpl

    private val nightlyEnabledFlow = MutableStateFlow(false)
    private val nightlyStateFlow = MutableStateFlow(PersistedNightlyState(
        lastCheckedAtMs = null,
        available = false,
        release = null,
        lastNotifiedVersion = null,
    ))
    private val stateFlow = MutableStateFlow(PersistedUpdateState(
        lastCheckedAtMs = null,
        available = false,
        release = null,
        lastNotifiedVersion = null
    ))

    @Before
    fun setup() {
        api = mockk()
        store = mockk(relaxed = true)
        provider = mockk()

        every { provider.resolve() } returns DistributionSource.GITHUB
        every { store.nightlyEnabled } returns nightlyEnabledFlow
        every { store.nightlyState } returns nightlyStateFlow
        every { store.state } returns stateFlow
        // Read on discovery to decide whether an "update available" notification is worth posting;
        // a relaxed mock hands back an empty flow, which first() treats as a missing value.
        every { store.stableAutoDownload } returns MutableStateFlow(false)
        every { store.nightlyAutoDownload } returns MutableStateFlow(false)

        // The repository only forwards download calls; these tests are about the check flow.
        repository = UpdateRepositoryImpl(
            context = mockk(relaxed = true),
            scope = TestScope(),
            githubApi = api,
            store = store,
            installSourceProvider = provider,
            apkDownloader = mockk(relaxed = true),
            notifier = mockk(relaxed = true),
        )
    }

    private fun release(tag: String, prerelease: Boolean = true) = GithubReleaseDto(
        tagName = tag,
        name = "Nightly $tag",
        body = "commit: abcdef0",
        htmlUrl = "https://github.com/Auties00/MyBicocca/releases/tag/$tag",
        publishedAt = "2026-08-29T12:00:00Z",
        draft = false,
        prerelease = prerelease,
        assets = emptyList()
    )

    @Test
    fun checkForUpdates_whenNightlyEnabled_offersTheHighestNightly() = kotlinx.coroutines.runBlocking {
        nightlyEnabledFlow.value = true
        coEvery { api.getReleases() } returns listOf(
            release("999.0.0-nightly.3"),
            release("999.0.0-nightly.12"),
            release("999.0.0-nightly.9"),
            // The rolling tag from before per-build nightlies, and a stable release: never picked.
            release("nightly"),
            release("0.0.1", prerelease = false),
        )
        coEvery { api.getLatestRelease() } returns null

        val result = repository.checkForUpdates(force = false)

        assertThat((result as UpdateCheckResult.UpdateAvailable).release.versionName)
            .isEqualTo("999.0.0-nightly.12")
        repository.newNightlyUpdateEvents.test {
            val event = awaitItem()
            assertThat(event.versionName).isEqualTo("999.0.0-nightly.12")
            assertThat(event.title).isEqualTo("Nightly 999.0.0-nightly.12")
            assertThat(event.commitSha).isEqualTo("abcdef0")
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { store.setNightlyUpdateAvailable(match { it.versionName == "999.0.0-nightly.12" }, any()) }
        coVerify { store.setNightlyLastNotifiedVersion("999.0.0-nightly.12") }
    }

    @Test
    fun checkForUpdates_whenTheNewestNightlyIsNotNewer_reportsNightlyUpToDate() = runTest {
        nightlyEnabledFlow.value = true
        coEvery { api.getReleases() } returns listOf(release("0.0.0-nightly.1"))
        coEvery { api.getLatestRelease() } returns null

        val result = repository.checkForUpdates(force = true, announce = false)

        assertThat(result).isEqualTo(UpdateCheckResult.UpToDate)
        coVerify { store.setNightlyUpToDate(any()) }
        coVerify(exactly = 0) { store.setNightlyUpdateAvailable(any(), any()) }
    }

    /** Still on offer at every check until installed, but only announced the first time. */
    @Test
    fun checkForUpdates_whenTheNightlyWasAlreadyNotified_doesNotAnnounceItAgain() = runTest {
        nightlyEnabledFlow.value = true
        nightlyStateFlow.value = nightlyStateFlow.value.copy(lastNotifiedVersion = "999.0.0-nightly.12")
        coEvery { api.getReleases() } returns listOf(release("999.0.0-nightly.12"))
        coEvery { api.getLatestRelease() } returns null

        val result = repository.checkForUpdates(force = true)

        assertThat(result).isInstanceOf(UpdateCheckResult.UpdateAvailable::class.java)
        coVerify { store.setNightlyUpdateAvailable(any(), any()) }
        coVerify(exactly = 0) { store.setNightlyLastNotifiedVersion(any()) }
    }

    @Test
    fun checkForUpdates_whenNightlyDisabled_neverLooksForOne() = runTest {
        coEvery { api.getLatestRelease() } returns null

        repository.checkForUpdates(force = true, announce = false)

        coVerify(exactly = 0) { api.getReleases() }
    }

    @Test
    fun releases_listsStableReleasesAndOnlyTheNewestNightly() = runTest {
        nightlyEnabledFlow.value = true
        coEvery { api.getReleases() } returns listOf(
            release("999.0.0-nightly.12").copy(publishedAt = "2026-08-30T12:00:00Z"),
            release("999.0.0-nightly.11").copy(publishedAt = "2026-08-29T12:00:00Z"),
            release("0.0.2", prerelease = false).copy(publishedAt = "2026-08-01T12:00:00Z"),
            release("0.0.1", prerelease = false).copy(publishedAt = "2026-07-01T12:00:00Z"),
        )

        val versions = repository.releases().map { it.versionName }

        assertThat(versions).containsExactly("999.0.0-nightly.12", "0.0.2", "0.0.1").inOrder()
    }

    @Test
    fun releases_leavesOutANightlyAStableReleaseHasOvertaken() = runTest {
        nightlyEnabledFlow.value = true
        coEvery { api.getReleases() } returns listOf(
            release("0.0.2", prerelease = false),
            release("0.0.2-nightly.40"),
        )

        assertThat(repository.releases().map { it.versionName }).containsExactly("0.0.2")
    }

    @Test
    fun releases_leavesOutNightliesOffTheChannel() = runTest {
        coEvery { api.getReleases() } returns listOf(
            release("999.0.0-nightly.12"),
            release("0.0.2", prerelease = false),
        )

        assertThat(repository.releases().map { it.versionName }).containsExactly("0.0.2")
    }

    @Test
    fun checkForUpdates_whenForcedOnTheLatestRelease_reportsUpToDate() = runTest {
        val running = BuildConfig.VERSION_NAME
        coEvery { api.getLatestRelease() } returns GithubReleaseDto(
            tagName = "v$running",
            name = "MyBicocca $running",
            body = "",
            htmlUrl = "url",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = false,
            prerelease = false,
            assets = emptyList()
        )

        val result = repository.checkForUpdates(force = true, announce = false)

        assertThat(result).isEqualTo(UpdateCheckResult.UpToDate)
        coVerify(exactly = 0) { store.setUpdateAvailable(any(), any()) }
    }
}
