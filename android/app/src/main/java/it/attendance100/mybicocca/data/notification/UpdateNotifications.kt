package it.attendance100.mybicocca.data.notification

import android.content.Context
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.notification.ActionIntent
import it.attendance100.mybicocca.core.notification.Alert
import it.attendance100.mybicocca.core.notification.NotificationAction
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.core.notification.NotificationSpec
import it.attendance100.mybicocca.core.notification.Progress

/** The specs the update flow posts, kept together so their wording and routing stay consistent. */
object UpdateNotifications {

    const val ACTION_CANCEL_DOWNLOAD = "it.attendance100.mybicocca.action.CANCEL_DOWNLOAD"

    /**
     * The ongoing download notification, which also serves as the foreground service's own.
     *
     * [percent] null means the download hasn't reported anything yet, shown as indeterminate
     * rather than a bar sitting at zero.
     */
    fun downloadProgress(context: Context, percent: Int?): NotificationSpec = NotificationSpec(
        channel = NotificationChannelId.UPDATE_PROGRESS,
        id = NotificationId.UpdateProgress,
        title = context.getString(R.string.notification_update_downloading_title),
        text = percent
            ?.let { context.getString(R.string.notification_update_downloading_text, it) }
            ?: context.getString(R.string.notification_update_preparing),
        ongoing = true,
        autoCancel = false,
        // Silent throughout: this updates many times and never warrants a buzz.
        alert = Alert.Never,
        progress = percent?.let(Progress::Determinate) ?: Progress.Indeterminate,
        // A foreground service's notification can't be swiped away, so the action is the only way
        // out of a download the user no longer wants.
        actions = listOf(
            NotificationAction(
                title = context.getString(R.string.notification_update_cancel),
                intent = ActionIntent.Broadcast(ACTION_CANCEL_DOWNLOAD),
            ),
        ),
        route = NotificationRoute.UpdatePage,
    )
}
