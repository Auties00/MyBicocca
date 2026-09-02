package it.attendance100.mybicocca.data.notification

import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.notification.NotificationChannelGroupId
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationChannelRegistrarTest {

    private lateinit var app: Application
    private lateinit var manager: NotificationManager

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        manager = app.getSystemService(NotificationManager::class.java)
    }

    @Test
    fun `every declared channel is created with its declared importance and group`() {
        NotificationChannelRegistrar(app).register()

        NotificationChannelId.entries.forEach { declared ->
            val created = manager.getNotificationChannel(declared.id)
            assertThat(created).isNotNull()
            assertThat(created.importance).isEqualTo(declared.importance.platformValue())
            assertThat(created.group).isEqualTo(declared.group.id)
        }
    }

    @Test
    fun `every declared group is created`() {
        NotificationChannelRegistrar(app).register()

        val createdIds = manager.notificationChannelGroups.map { it.id }
        assertThat(createdIds).containsAtLeastElementsIn(NotificationChannelGroupId.entries.map { it.id })
    }

    @Test
    fun `re-registering does not reset an importance the user changed`() {
        val registrar = NotificationChannelRegistrar(app)
        registrar.register()

        // Stand in for the user muting the category in system settings. The platform ignores an
        // importance change from code on an existing channel, which is the whole reason ids are
        // versioned — this test is what catches a future "just recreate it" shortcut.
        val channel = manager.getNotificationChannel(NotificationChannelId.UPDATE_ACTIONABLE.id)
        channel.importance = NotificationManager.IMPORTANCE_NONE
        manager.createNotificationChannel(channel)

        NotificationChannelRegistrar(app).register()

        assertThat(manager.getNotificationChannel(NotificationChannelId.UPDATE_ACTIONABLE.id).importance)
            .isEqualTo(NotificationManager.IMPORTANCE_NONE)
    }

    @Test
    fun `register is a no-op the second time on the same instance`() {
        val registrar = NotificationChannelRegistrar(app)
        registrar.register()
        val afterFirst = manager.notificationChannels.size

        registrar.register()

        assertThat(manager.notificationChannels).hasSize(afterFirst)
    }
}

/** API 25 predates channels entirely; registration must be a silent no-op rather than a crash. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [25])
class NotificationChannelRegistrarLegacyTest {

    @Test
    fun `registering on API 25 does not crash`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        NotificationChannelRegistrar(app).register()
    }
}
