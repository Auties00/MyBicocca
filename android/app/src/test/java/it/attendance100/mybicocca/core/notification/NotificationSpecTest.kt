package it.attendance100.mybicocca.core.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * How loudly a notification announces itself depends on whether the user is already looking at the
 * app. The rule lives on the spec so it can be checked here rather than on a device, where "did it
 * buzz?" is not something a test can ask.
 */
class NotificationSpecTest {

    private fun spec(alert: Alert = Alert.Once, foregroundAlert: Alert? = null) = NotificationSpec(
        channel = NotificationChannelId.UPDATE_ACTIONABLE,
        id = NotificationId.UpdateAvailable,
        title = "Nuova versione disponibile",
        alert = alert,
        foregroundAlert = foregroundAlert,
    )

    @Test
    fun `backgrounded, a spec alerts the way it asked to`() {
        val spec = spec(alert = Alert.Once, foregroundAlert = Alert.Never)

        assertThat(spec.effectiveAlert(foregrounded = false)).isEqualTo(Alert.Once)
    }

    @Test
    fun `foregrounded, the foreground alert takes over`() {
        val spec = spec(alert = Alert.Once, foregroundAlert = Alert.Never)

        assertThat(spec.effectiveAlert(foregrounded = true)).isEqualTo(Alert.Never)
    }

    /** Opting in is per spec: a feature with no in-app equivalent should still be heard. */
    @Test
    fun `a spec that opts out is unaffected by the app being open`() {
        val spec = spec(alert = Alert.Every, foregroundAlert = null)

        assertThat(spec.effectiveAlert(foregrounded = true)).isEqualTo(Alert.Every)
        assertThat(spec.effectiveAlert(foregrounded = false)).isEqualTo(Alert.Every)
    }
}
