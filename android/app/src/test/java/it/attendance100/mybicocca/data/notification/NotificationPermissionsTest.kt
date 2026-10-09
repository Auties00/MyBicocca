package it.attendance100.mybicocca.data.notification

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * [NotificationPermissions.canNotify] across the three things that can independently hide a
 * notification: the runtime permission, the app-level toggle, and the channel's own importance.
 * Each is a separate switch and each has to be able to veto on its own.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationPermissionsTest {

    private lateinit var app: Application
    private lateinit var permissions: NotificationPermissions
    private lateinit var manager: NotificationManager

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        permissions = NotificationPermissions(app)
        manager = app.getSystemService(NotificationManager::class.java)
        NotificationChannelRegistrar(app).register()
    }

    private fun grantPost() = shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    private fun denyPost() = shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun `granted permission and enabled notifications can post`() {
        grantPost()
        assertThat(permissions.canNotify(NotificationChannelId.UPDATE_ACTIONABLE)).isTrue()
    }

    @Test
    fun `denied runtime permission cannot post`() {
        denyPost()
        assertThat(permissions.canNotify(NotificationChannelId.UPDATE_ACTIONABLE)).isFalse()
    }

    @Test
    fun `app-level toggle off cannot post even with the permission granted`() {
        grantPost()
        shadowOf(manager).setNotificationsEnabled(false)
        assertThat(permissions.canNotify(NotificationChannelId.UPDATE_ACTIONABLE)).isFalse()
    }

    @Test
    fun `a channel muted to IMPORTANCE_NONE cannot post`() {
        grantPost()
        // What the user does by turning off a single category in system settings; the app-level
        // toggle and the permission both stay on, so only the channel check catches it.
        manager.deleteNotificationChannel(NotificationChannelId.UPDATE_PROGRESS.id)
        manager.createNotificationChannel(
            android.app.NotificationChannel(
                NotificationChannelId.UPDATE_PROGRESS.id,
                "muted",
                NotificationManager.IMPORTANCE_NONE,
            )
        )

        assertThat(permissions.canNotify(NotificationChannelId.UPDATE_PROGRESS)).isFalse()
        // Muting one channel must not silence the other.
        assertThat(permissions.canNotify(NotificationChannelId.UPDATE_ACTIONABLE)).isTrue()
    }

    @Test
    fun `permission is not requestable once granted`() {
        grantPost()
        assertThat(permissions.needsPermissionRequest()).isFalse()
    }

    @Test
    fun `permission is requestable while denied`() {
        denyPost()
        assertThat(permissions.needsPermissionRequest()).isTrue()
    }
}
