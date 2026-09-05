package it.attendance100.mybicocca.data.notification

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.notification.ActionIntent
import it.attendance100.mybicocca.core.notification.Alert
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.core.notification.Progress
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The download notification doubles as the foreground service's own, so several of its properties
 * are load-bearing rather than cosmetic: it has to be ongoing, it has to be silent across a
 * hundred updates, and it has to carry the Cancel action because an FGS notification cannot be
 * swiped away.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpdateNotificationsTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `download progress is ongoing and silent`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = 40)

        assertThat(spec.channel).isEqualTo(NotificationChannelId.UPDATE_PROGRESS)
        assertThat(spec.id).isEqualTo(NotificationId.UpdateProgress)
        assertThat(spec.ongoing).isTrue()
        assertThat(spec.autoCancel).isFalse()
        assertThat(spec.alert).isEqualTo(Alert.Never)
    }

    @Test
    fun `a reported percentage becomes determinate progress`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = 40)
        assertThat(spec.progress).isEqualTo(Progress.Determinate(40))
        assertThat(spec.progressPercent).isEqualTo(40)
    }

    /**
     * Everything the shell also raises a snackbar for goes quiet while the app is open — the same
     * news twice, once of it buzzing, is the failure this prevents. It still reaches the tray,
     * because the snackbar is gone in seconds and the notification is what the user comes back to.
     */
    @Test
    fun `the actionable notifications stay silent while the app is on screen`() {
        val specs = listOf(
            UpdateNotifications.updateAvailable(context, "9.9.9"),
            UpdateNotifications.nightlyUpdateAvailable(context, "nightly-1"),
            UpdateNotifications.updateReady(context, "9.9.9", "/apk"),
        )

        specs.forEach { spec ->
            assertThat(spec.effectiveAlert(foregrounded = true)).isEqualTo(Alert.Never)
            assertThat(spec.effectiveAlert(foregrounded = false)).isNotEqualTo(Alert.Never)
        }
    }

    /**
     * The Live Update ask. Inert below Android 16, so it costs nothing to carry — but the chip has
     * room for a few characters only, which is what the short text is for.
     */
    @Test
    fun `download progress asks to be promoted, with text short enough for the chip`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = 42)

        assertThat(spec.promoted).isTrue()
        assertThat(spec.shortCriticalText).isEqualTo("42%")
    }

    /** Nothing to show in a chip that reads "null%" — the spinner speaks for itself. */
    @Test
    fun `a download with nothing reported yet has no chip text`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = null)

        assertThat(spec.promoted).isTrue()
        assertThat(spec.shortCriticalText).isNull()
    }

    /** Nothing reported yet is a spinner, not a bar sitting convincingly at zero. */
    @Test
    fun `no reported percentage is indeterminate`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = null)
        assertThat(spec.progress).isEqualTo(Progress.Indeterminate)
        assertThat(spec.progressPercent).isNull()
    }

    @Test
    fun `it carries a cancel action, the only way out of a foreground-service notification`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = 10)

        val action = spec.actions.single()
        assertThat(action.intent)
            .isEqualTo(ActionIntent.Broadcast(UpdateNotifications.ACTION_CANCEL_DOWNLOAD))
    }

    /**
     * The action is only wired if a receiver declares it in the manifest. [NotificationRouter]
     * sends it as an implicit intent scoped to this package, and a manifest receiver with no
     * intent filter is reachable by component only — so a missing filter leaves the Cancel button
     * resolving to nothing, with no error and no way for the user to stop a download.
     */
    @Test
    fun `the cancel action resolves to a receiver`() {
        val action = UpdateNotifications.downloadProgress(context, percent = 10)
            .actions.single().intent as ActionIntent.Broadcast

        val intent = Intent(action.action).setPackage(context.packageName)

        assertThat(context.packageManager.queryBroadcastReceivers(intent, 0)).isNotEmpty()
    }

    @Test
    fun `tapping it opens the update page`() {
        val spec = UpdateNotifications.downloadProgress(context, percent = 10)
        assertThat(spec.route).isEqualTo(NotificationRoute.UpdatePage)
    }

    /**
     * A declined install leaves the APK on disk and the offer standing, so this one has to outlive
     * its own tap — and must not buzz again when it is re-posted.
     */
    @Test
    fun `ready to install survives being tapped and does not re-alert`() {
        val spec = UpdateNotifications.updateReady(context, "9.9.9", "/cache/updates/app.apk")

        assertThat(spec.autoCancel).isFalse()
        assertThat(spec.alert).isEqualTo(Alert.Once)
    }

    /**
     * Through the app, not straight at the system installer: a tap can't run code, so going direct
     * would skip installApk and silently stop declined installs being detected at all.
     */
    @Test
    fun `ready to install routes through the app rather than at the installer`() {
        val spec = UpdateNotifications.updateReady(context, "9.9.9", "/cache/updates/app.apk")

        assertThat(spec.route).isEqualTo(NotificationRoute.InstallApk("/cache/updates/app.apk"))
    }

    /** Nothing is downloaded yet, so the tap can only offer the page it would be downloaded from. */
    @Test
    fun `update available opens the update page`() {
        val spec = UpdateNotifications.updateAvailable(context, "9.9.9")

        assertThat(spec.channel).isEqualTo(NotificationChannelId.UPDATE_ACTIONABLE)
        assertThat(spec.route).isEqualTo(NotificationRoute.UpdatePage)
    }

    /**
     * A slot each, so none of them replaces another by accident. The two channels matter most:
     * their checks run concurrently, so a shared slot means whichever finishes second silently
     * erases the other's notification.
     */
    @Test
    fun `every update notification holds its own slot`() {
        val ids = listOf(
            UpdateNotifications.updateAvailable(context, "9.9.9").id,
            UpdateNotifications.nightlyUpdateAvailable(context, "nightly-1").id,
            UpdateNotifications.downloadProgress(context, 10).id,
            UpdateNotifications.updateReady(context, "9.9.9", "/apk").id,
        )

        assertThat(ids.map { it.value }).containsNoDuplicates()
    }

    /** Indeterminate and determinate must share a slot, or the bar would appear twice. */
    @Test
    fun `every progress spec targets the same slot`() {
        assertThat(UpdateNotifications.downloadProgress(context, null).id)
            .isEqualTo(UpdateNotifications.downloadProgress(context, 99).id)
    }
}
