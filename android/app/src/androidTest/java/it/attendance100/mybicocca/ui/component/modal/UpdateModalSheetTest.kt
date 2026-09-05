package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.UpdateModalKind
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * What leaving the update sheet means, in every state it can be left from.
 *
 * The sheet takes a `StateFlow<DownloadState>` and plain callbacks rather than a ViewModel, so
 * every state here is reached by writing to a flow — no GitHub, no downloader, no WorkManager, no
 * APK on disk. That is the whole reason the matrix can be covered at all: reproducing "downloading,
 * 40%, on the nightly channel" by hand needs a real release and a real transfer.
 *
 * The rules under test:
 * - nothing started -> leaving declines the update;
 * - downloading an ordinary update -> leaving pops the sheet and the download carries on;
 * - downloading a channel change -> leaving declines it and stops the download.
 *
 * The back gesture is asserted alongside the button in each case, because for a channel change the
 * two must mean the same thing — dismissing *is* declining, and a back that quietly left the switch
 * flipped would strand the user on a channel they backed out of.
 */
@RunWith(AndroidJUnit4::class)
class UpdateModalSheetTest {

    @get:Rule
    val compose = createComposeRule()

    private val downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)

    private var dismissed = 0
    private var stayedOnChannel = 0
    private var restoredNightlyTo: Boolean? = null
    private var downloadsRequested = 0
    private var installed: File? = null

    private val release = AppRelease(
        versionName = "9.9.9",
        title = "Test",
        notes = "",
        pageUrl = "https://example.test",
        publishedAt = null,
        isPreRelease = false,
        assets = listOf(
            AppReleaseAsset(
                name = "app-universal.apk",
                downloadUrl = "https://example.test/app-universal.apk",
                size = 1,
            )
        ),
        commitSha = null,
    )

    private fun showSheet(kind: UpdateModalKind = UpdateModalKind.Standard) {
        compose.setBicoccaContent {
            UpdateModalSheet(
                release = release,
                downloadStateFlow = downloadState,
                onDownload = { downloadsRequested++ },
                onInstall = { installed = it },
                onDismiss = { dismissed++ },
                channelSwitch = kind.channelSwitch { nightlyEnabled ->
                    stayedOnChannel++
                    restoredNightlyTo = nightlyEnabled
                },
            )
        }
    }

    // ----------------------------------------------------------- nothing started

    @Test
    fun ordinaryUpdate_leavingBeforeAnyDownload_declines() {
        showSheet()

        compose.onNodeWithTag(UpdateModalTestTags.DOWNLOAD).assertIsDisplayed()
        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        assertThat(dismissed).isEqualTo(1)
        assertThat(downloadsRequested).isEqualTo(0)
    }

    @Test
    fun channelSwitch_leavingBeforeAnyDownload_putsTheSwitchBack() {
        showSheet(UpdateModalKind.SwitchToNightly)

        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        assertThat(stayedOnChannel).isEqualTo(1)
        assertThat(restoredNightlyTo).isFalse()
        // The channel undo replaces the plain dismiss rather than running alongside it, or the
        // host would close the sheet twice and clear the saved slot out from under itself.
        assertThat(dismissed).isEqualTo(0)
    }

    // --------------------------------------------------------- download in flight

    @Test
    fun ordinaryUpdate_leavingMidDownload_popsTheSheetAndLetsItRun() {
        showSheet()
        downloadState.value = DownloadState.Downloading(40)
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.PROGRESS).assertIsDisplayed()
        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        // Nothing here can stop the download: the sheet has no cancel path for an ordinary update,
        // and dismissing is all that happens.
        assertThat(dismissed).isEqualTo(1)
        assertThat(stayedOnChannel).isEqualTo(0)
    }

    @Test
    fun channelSwitch_leavingMidDownload_declinesAndCancels() {
        showSheet(UpdateModalKind.RestoreStable)
        downloadState.value = DownloadState.Downloading(40)
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        assertThat(stayedOnChannel).isEqualTo(1)
        assertThat(restoredNightlyTo).isTrue()
        assertThat(dismissed).isEqualTo(0)
    }

    /**
     * The guard that used to block this predates foreground-service downloads, when leaving really
     * would have killed one. It now only traps whoever is on a slow connection.
     */
    @Test
    fun leavingIsNeverBlockedWhileDownloading() {
        showSheet()
        downloadState.value = DownloadState.Downloading(3)
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).assertIsEnabled().performClick()

        assertThat(dismissed).isEqualTo(1)
    }

    /** Queued counts as started: the user tapped Download, they just can't see it move yet. */
    @Test
    fun ordinaryUpdate_leavingWhileQueued_popsTheSheetAndLetsItRun() {
        showSheet()
        downloadState.value = DownloadState.Enqueued
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.PROGRESS).assertIsDisplayed()
        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        assertThat(dismissed).isEqualTo(1)
        assertThat(stayedOnChannel).isEqualTo(0)
    }

    // ------------------------------------------------------------- the back gesture

    @Test
    fun ordinaryUpdate_backMidDownload_matchesTheButton() {
        showSheet()
        downloadState.value = DownloadState.Downloading(40)
        compose.waitForIdle()

        Espresso.pressBack()
        compose.waitForIdle()

        assertThat(dismissed).isEqualTo(1)
        assertThat(stayedOnChannel).isEqualTo(0)
    }

    @Test
    fun channelSwitch_backMidDownload_declinesJustLikeTheButton() {
        showSheet(UpdateModalKind.SwitchToNightly)
        downloadState.value = DownloadState.Downloading(40)
        compose.waitForIdle()

        Espresso.pressBack()
        compose.waitForIdle()

        assertThat(stayedOnChannel).isEqualTo(1)
        assertThat(restoredNightlyTo).isFalse()
        assertThat(dismissed).isEqualTo(0)
    }

    // ------------------------------------------------------------- already downloaded

    /** A finished download is an offer, not something in progress: leaving keeps the APK. */
    @Test
    fun ordinaryUpdate_leavingWhenReadyToInstall_keepsTheOffer() {
        showSheet()
        downloadState.value = DownloadState.Success(File("/cache/updates/app-universal.apk"))
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.INSTALL).assertIsDisplayed()
        compose.onNodeWithTag(UpdateModalTestTags.LEAVE).performClick()

        assertThat(dismissed).isEqualTo(1)
        assertThat(installed).isNull()
    }

    /** A declined install left the APK on disk, so the sheet still offers to install it. */
    @Test
    fun installDeclined_stillOffersTheInstall() {
        val file = File("/cache/updates/app-universal.apk")
        showSheet()
        downloadState.value = DownloadState.InstallDeclined(file)
        compose.waitForIdle()

        compose.onNodeWithTag(UpdateModalTestTags.INSTALL).performClick()

        assertThat(installed).isEqualTo(file)
    }

    /** Nothing installs itself; the install button is the only path to the system dialog. */
    @Test
    fun aFinishedDownloadNeverInstallsOnItsOwn() {
        showSheet()
        downloadState.value = DownloadState.Success(File("/cache/updates/app-universal.apk"))
        compose.waitForIdle()

        assertThat(installed).isNull()
    }
}
