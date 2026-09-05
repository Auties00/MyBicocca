package it.attendance100.mybicocca.data.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one place that answers "would a notification on this channel actually be seen?".
 *
 * Three things have to hold, and they fail independently: the runtime permission (Android 13+),
 * the app-level toggle in system settings, and the channel's own importance not having been set
 * to "none" by the user. Asking in one place is what stops each feature reinventing two of the
 * three and forgetting the last.
 *
 * **This never gates work.** A foreground service still runs with an invisible notification, so
 * work whose progress can't be shown must still happen. Callers use [canNotify] to skip *building*
 * something nobody will see, never to decide whether to do the thing itself.
 */
@Singleton
class NotificationPermissions @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Whether `POST_NOTIFICATIONS` is held. Always true below Android 13, where it doesn't exist. */
    fun hasPostPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Whether the user has left notifications on for the app as a whole. */
    fun areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun canNotify(channel: NotificationChannelId): Boolean {
        if (!hasPostPermission() || !areNotificationsEnabled()) return false

        // No channels below API 26, so the app-level toggle is the whole answer there.
        val importance = NotificationManagerCompat.from(context).currentImportanceOf(channel) ?: return true
        return importance != NotificationManager.IMPORTANCE_NONE
    }

    /**
     * Whether a Live Update would actually be promoted to the status-bar chip.
     *
     * Two separate things can stop it and only this one is visible from here: the notification
     * also has to be *shaped* right (ongoing, a supported style, not colorized, not a group
     * summary, channel importance above minimum). The system declines silently either way, so a
     * notification that looks correct and simply never appears in the chip is the normal symptom.
     *
     * False is never a reason to skip posting: an unpromoted Live Update is an ordinary progress
     * notification, which is exactly the pre-Android-16 behaviour.
     */
    fun canPromoteOngoing(): Boolean {
        if (Build.VERSION.SDK_INT < PROMOTED_ONGOING_SDK) return false
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.canPostPromotedNotifications()
    }

    /**
     * Whether there is a permission to ask for at all: it exists on this OS and isn't held yet.
     *
     * Deliberately not "will a prompt appear" — after two denials the system request becomes a
     * silent no-op, and only an Activity can tell (via `shouldShowRequestPermissionRationale`).
     * The caller that owns an Activity makes that distinction; this one only rules out the cases
     * where asking is meaningless.
     */
    fun needsPermissionRequest(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPostPermission()

    private companion object {
        /** Android 16, where promoted ongoing notifications arrive. */
        const val PROMOTED_ONGOING_SDK = 36
    }
}
