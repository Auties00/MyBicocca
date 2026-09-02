package it.attendance100.mybicocca.data.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.notification.NotificationChannelGroupId
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationImportance
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates every channel in [NotificationChannelId] once per process. Called from
 * `MyBicoccaApplication.onCreate` after the `:crash` process guard, so the crash-host process
 * never does it.
 *
 * `createNotificationChannel` is itself idempotent — re-creating an existing channel updates only
 * the name and description and leaves the user's importance and sound alone — but [register] is
 * guarded anyway, since `onCreate` can run more than once per process and there is no reason to
 * repeat the work.
 */
@Singleton
class NotificationChannelRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var registered = false

    fun register() {
        if (registered) return
        registered = true

        // API 25 has no channels at all; importance rides on the builder there instead
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = NotificationManagerCompat.from(context)

        NotificationChannelGroupId.entries.forEach { group ->
            manager.createNotificationChannelGroup(
                NotificationChannelGroupCompat.Builder(group.id)
                    .setName(context.getString(group.nameRes))
                    .build()
            )
        }

        NotificationChannelId.entries.forEach { channel ->
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(channel.id, channel.importance.platformValue())
                    .setName(context.getString(channel.nameRes))
                    .setDescription(context.getString(channel.descriptionRes))
                    .setGroup(channel.group.id)
                    .build()
            )
        }

        RETIRED_CHANNEL_IDS.forEach(manager::deleteNotificationChannel)
    }

    companion object {
        /**
         * Ids of channels this app used to create and no longer does
         * Deleting them stops a retired channel sitting in system settings forever on an upgraded installation.
         * Only ever list an id this app created: deleting a channel a library owns breaks that library's notification.
         */
        val RETIRED_CHANNEL_IDS = emptyList<String>()
    }
}

/**
 * Channels are created with an importance the user may then override, so this mapping only ever
 * decides the *initial* value.
 */
internal fun NotificationImportance.platformValue(): Int = when (this) {
    NotificationImportance.MIN -> NotificationManagerCompat.IMPORTANCE_MIN
    NotificationImportance.LOW -> NotificationManagerCompat.IMPORTANCE_LOW
    NotificationImportance.DEFAULT -> NotificationManagerCompat.IMPORTANCE_DEFAULT
    NotificationImportance.HIGH -> NotificationManagerCompat.IMPORTANCE_HIGH
}

/** The importance the user has actually left the channel on, or null below API 26. */
internal fun NotificationManagerCompat.currentImportanceOf(channel: NotificationChannelId): Int? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) null
    else getNotificationChannelCompat(channel.id)?.importance ?: NotificationManager.IMPORTANCE_NONE
