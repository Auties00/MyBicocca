package it.attendance100.mybicocca.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.local.settings.PersistedNightlyState
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.update.GithubReleaseApi
import it.attendance100.mybicocca.data.update.GithubReleaseAssetDto
import it.attendance100.mybicocca.data.update.GithubReleaseDto
import it.attendance100.mybicocca.data.update.InstallSourceProvider
import it.attendance100.mybicocca.domain.model.update.DistributionSource
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
        lastSeenPublishedAtMs = null,
        lastSeenDigest = null,
        available = false,
        release = null
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

    @Test
    fun checkForUpdates_whenNightlyEnabled_checksNightly() = kotlinx.coroutines.runBlocking {
        nightlyEnabledFlow.value = true
        
        val dto = GithubReleaseDto(
            tagName = "nightly",
            name = "Nightly",
            body = "commit: abcdef0",
            htmlUrl = "url",
            publishedAt = "2026-08-29T12:00:00Z",
            draft = false,
            prerelease = true,
            assets = emptyList()
        )
        
        coEvery { api.getReleaseByTag("nightly") } returns dto
        coEvery { api.getLatestRelease() } returns null

        repository.checkForUpdates(force = false)

        // It should have found the nightly and updated the store
        repository.newNightlyUpdateEvents.test {
            val event = awaitItem()
            assertThat(event.title).isEqualTo("Nightly Build")
            assertThat(event.commitSha).isEqualTo("abcdef0")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
