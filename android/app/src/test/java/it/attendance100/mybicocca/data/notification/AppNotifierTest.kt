package it.attendance100.mybicocca.data.notification

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.notification.ActionIntent
import it.attendance100.mybicocca.core.notification.Alert
import it.attendance100.mybicocca.core.notification.GroupKey
import it.attendance100.mybicocca.core.notification.NotificationAction
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.core.notification.NotificationSpec
import it.attendance100.mybicocca.core.notification.Progress
import androidx.lifecycle.LifecycleOwner
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppNotifierTest {

    private lateinit var app: Application
    private lateinit var notifier: AppNotifier
    private lateinit var manager: NotificationManager
    private lateinit var foreground: AppForegroundState

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        manager = app.getSystemService(NotificationManager::class.java)
        NotificationChannelRegistrar(app).register()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        foreground = AppForegroundState()
        notifier = AppNotifier(app, NotificationPermissions(app), NotificationRouter(app), foreground)
    }

    /** Drives the lifecycle callbacks directly; nothing here needs a real process lifecycle. */
    private fun appOnScreen(onScreen: Boolean) {
        val owner = mockk<LifecycleOwner>(relaxed = true)
        if (onScreen) foreground.onStart(owner) else foreground.onStop(owner)
    }

    private fun spec(
        id: NotificationId = NotificationId.UpdateAvailable,
        channel: NotificationChannelId = NotificationChannelId.UPDATE_ACTIONABLE,
        alert: Alert = Alert.Once,
        progress: Progress? = null,
        route: NotificationRoute? = null,
        actions: List<NotificationAction> = emptyList(),
        group: GroupKey? = null,
    ) = NotificationSpec(
        channel = channel,
        id = id,
        title = "Nuova versione disponibile",
        text = "v0.0.6",
        alert = alert,
        progress = progress,
        route = route,
        actions = actions,
        group = group,
    )

    private fun posted() = shadowOf(manager).allNotifications

    @Test
    fun `a posted spec reaches the tray on its own channel`() {
        assertThat(notifier.post(spec())).isTrue()

        assertThat(posted()).hasSize(1)
        assertThat(posted().single().channelId)
            .isEqualTo(NotificationChannelId.UPDATE_ACTIONABLE.id)
    }

    @Test
    fun `nothing is posted when the permission is denied`() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        assertThat(notifier.post(spec())).isFalse()
        assertThat(posted()).isEmpty()
    }

    @Test
    fun `re-posting the same id updates the slot instead of adding a second`() {
        notifier.post(spec())
        notifier.post(spec().copy(title = "Aggiornato"))

        assertThat(posted()).hasSize(1)
        assertThat(posted().single().extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo("Aggiornato")
    }

    @Test
    fun `Alert Once marks the notification as alert-only-once`() {
        notifier.post(spec(alert = Alert.Once))

        val flags = posted().single().flags
        assertThat(flags and Notification.FLAG_ONLY_ALERT_ONCE).isNotEqualTo(0)
    }

    @Test
    fun `Alert Every leaves the notification free to alert again`() {
        notifier.post(spec(alert = Alert.Every))

        val flags = posted().single().flags
        assertThat(flags and Notification.FLAG_ONLY_ALERT_ONCE).isEqualTo(0)
    }

    /**
     * Alert.Once sets FLAG_ONLY_ALERT_ONCE; going silent instead does not, which is what makes the
     * downgrade observable at all from a posted notification.
     */
    @Test
    fun `a spec with a foreground alert goes silent while the app is on screen`() {
        appOnScreen(true)

        notifier.post(spec(alert = Alert.Once).copy(foregroundAlert = Alert.Never))

        assertThat(posted().single().flags and Notification.FLAG_ONLY_ALERT_ONCE).isEqualTo(0)
    }

    @Test
    fun `the same spec alerts normally once the app is off screen`() {
        appOnScreen(false)

        notifier.post(spec(alert = Alert.Once).copy(foregroundAlert = Alert.Never))

        assertThat(posted().single().flags and Notification.FLAG_ONLY_ALERT_ONCE).isNotEqualTo(0)
    }

    @Test
    fun `determinate progress carries its percentage`() {
        notifier.post(
            spec(
                id = NotificationId.UpdateProgress,
                channel = NotificationChannelId.UPDATE_PROGRESS,
                progress = Progress.Determinate(42),
            )
        )

        val extras = posted().single().extras
        assertThat(extras.getInt(Notification.EXTRA_PROGRESS)).isEqualTo(42)
        assertThat(extras.getInt(Notification.EXTRA_PROGRESS_MAX)).isEqualTo(100)
        assertThat(extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE)).isFalse()
    }

    @Test
    fun `a progress percentage outside the range is clamped rather than rejected`() {
        notifier.post(
            spec(
                id = NotificationId.UpdateProgress,
                channel = NotificationChannelId.UPDATE_PROGRESS,
                progress = Progress.Determinate(140),
            )
        )

        assertThat(posted().single().extras.getInt(Notification.EXTRA_PROGRESS)).isEqualTo(100)
    }

    @Test
    fun `a second progress tick in the same instant is throttled`() {
        val progressSpec = spec(
            id = NotificationId.UpdateProgress,
            channel = NotificationChannelId.UPDATE_PROGRESS,
            progress = Progress.Determinate(10),
        )

        assertThat(notifier.post(progressSpec)).isTrue()
        assertThat(notifier.post(progressSpec.copy(progress = Progress.Determinate(11)))).isFalse()
    }

    @Test
    fun `a route becomes a tap intent`() {
        notifier.post(spec(route = NotificationRoute.UpdatePage))
        assertThat(posted().single().contentIntent).isNotNull()
    }

    @Test
    fun `a spec without a route has no tap intent`() {
        notifier.post(spec())
        assertThat(posted().single().contentIntent).isNull()
    }

    @Test
    fun `actions are attached in order`() {
        notifier.post(
            spec(
                actions = listOf(
                    NotificationAction("Annulla", ActionIntent.Broadcast("test.CANCEL")),
                    NotificationAction("Apri", ActionIntent.Open(NotificationRoute.UpdatePage)),
                )
            )
        )

        val actions = posted().single().actions
        assertThat(actions.map { it.title.toString() }).containsExactly("Annulla", "Apri").inOrder()
    }

    @Test
    fun `two actions on one notification get distinct intents`() {
        // Same request code would make PendingIntent.getBroadcast return the first action's intent
        // for the second, so both buttons would do the same thing.
        notifier.post(
            spec(
                actions = listOf(
                    NotificationAction("Annulla", ActionIntent.Broadcast("test.CANCEL")),
                    NotificationAction("Riprova", ActionIntent.Broadcast("test.RETRY")),
                )
            )
        )

        val actions = posted().single().actions
        assertThat(actions[0].actionIntent).isNotEqualTo(actions[1].actionIntent)
    }

    @Test
    fun `a grouped notification also posts a summary`() {
        notifier.post(spec(group = GroupKey("updates")))

        assertThat(posted()).hasSize(2)
        assertThat(posted().count { it.flags and Notification.FLAG_GROUP_SUMMARY != 0 }).isEqualTo(1)
    }

    @Test
    fun `cancel clears the slot`() {
        notifier.post(spec())
        notifier.cancel(NotificationId.UpdateAvailable)

        assertThat(posted()).isEmpty()
    }

    @Test
    fun `cancelAll clears one channel and leaves the other alone`() {
        notifier.post(spec())
        notifier.post(
            spec(
                id = NotificationId.UpdateProgress,
                channel = NotificationChannelId.UPDATE_PROGRESS,
                progress = Progress.Determinate(10),
            )
        )
        assertThat(posted()).hasSize(2)

        notifier.cancelAll(NotificationChannelId.UPDATE_PROGRESS)

        assertThat(posted().single().channelId)
            .isEqualTo(NotificationChannelId.UPDATE_ACTIONABLE.id)
    }

    @Test
    fun `cancel resets the throttle so the next download starts fresh`() {
        val progressSpec = spec(
            id = NotificationId.UpdateProgress,
            channel = NotificationChannelId.UPDATE_PROGRESS,
            progress = Progress.Determinate(10),
        )
        notifier.post(progressSpec)
        notifier.cancel(NotificationId.UpdateProgress)

        assertThat(notifier.post(progressSpec)).isTrue()
    }
}
