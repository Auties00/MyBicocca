package it.attendance100.mybicocca.core.notification

import androidx.annotation.StringRes
import it.attendance100.mybicocca.R

/**
 * Importance as this app declares it, kept free of `android.app.NotificationManager` constants so
 * a channel definition can be asserted on in a plain unit test. Mapped to the platform's
 * `IMPORTANCE_*` on API 26+, and to `NotificationCompat.PRIORITY_*` on API 25, where channels
 * don't exist and importance has to live on the builder instead.
 */
enum class NotificationImportance { MIN, LOW, DEFAULT, HIGH }

/** Channel groups, so system settings stays legible once there are a dozen channels rather than two. */
enum class NotificationChannelGroupId(
    val id: String,
    @StringRes val nameRes: Int,
) {
    UPDATES("updates", R.string.notification_group_updates),
}

/**
 * Every channel this app owns, and the single source of truth for its settings.
 * Nothing creates a channel outside this enum, and the poster refuses to post to a channel that isn't here.
 *
 * **Id versioning.** A channel's importance, sound and vibration belong to the user once it has been created —
 * the system ignores any later change from code. So changing one means retiring
 * the id and creating a new one: bump the `_v1` suffix and add the old id to
 * [it.attendance100.mybicocca.data.notification.NotificationChannelRegistrar.RETIRED_CHANNEL_IDS]
 * so it stops cluttering settings. The suffix exists from the first release precisely because
 * adding it later would mean every existing install keeps an unversioned id forever.
 */
enum class NotificationChannelId(
    val id: String,
    val group: NotificationChannelGroupId,
    val importance: NotificationImportance,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
) {
    /** Ongoing download progress. Silent by design: it updates many times and never needs a buzz. */
    UPDATE_PROGRESS(
        id = "update_progress_v1",
        group = NotificationChannelGroupId.UPDATES,
        importance = NotificationImportance.LOW,
        nameRes = R.string.notification_channel_update_progress_name,
        descriptionRes = R.string.notification_channel_update_progress_desc,
    ),

    /** "Update available" and "Ready to install" — the two that are worth one alert each. */
    UPDATE_ACTIONABLE(
        id = "update_actionable_v1",
        group = NotificationChannelGroupId.UPDATES,
        importance = NotificationImportance.DEFAULT,
        nameRes = R.string.notification_channel_update_actionable_name,
        descriptionRes = R.string.notification_channel_update_actionable_desc,
    ),
}
