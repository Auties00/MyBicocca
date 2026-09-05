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
     * "A new version is available", for a user who isn't in the app to see the snackbar.
     *
     * Tapping opens the update page rather than starting anything: the download is still the
     * user's call, and the page is where they make it.
     */
    fun updateAvailable(context: Context, versionName: String): NotificationSpec = NotificationSpec(
        channel = NotificationChannelId.UPDATE_ACTIONABLE,
        id = NotificationId.UpdateAvailable,
        title = context.getString(R.string.notification_update_available_title),
        text = context.getString(R.string.notification_update_available_text, versionName),
        route = NotificationRoute.UpdatePage,
    )

    /**
     * "A new nightly build is available", posted to its own slot so it can coexist with a
     * concurrent stable-channel notification rather than overwriting it.
     */
    fun nightlyUpdateAvailable(context: Context, versionName: String): NotificationSpec = NotificationSpec(
        channel = NotificationChannelId.UPDATE_ACTIONABLE,
        id = NotificationId.NightlyUpdateAvailable,
        title = context.getString(R.string.notification_update_available_title),
        text = context.getString(R.string.notification_update_available_text, versionName),
        route = NotificationRoute.UpdatePage,
    )

    /**
     * "The update is downloaded, tap to install."
     *
     * Routed to [NotificationRoute.InstallApk] rather than straight at the system installer: a
     * notification's tap cannot run code, so going direct would skip `installApk` and with it the
     * bookkeeping that detects a dismissed installer dialog. The route goes through the app, which
     * costs a brief flash of the app and keeps decline detection working.
     *
     * It survives being tapped ([autoCancel] off) because declining the install leaves a perfectly
     * good APK behind and the offer should still stand. [Alert.Once] is what stops the re-post
     * that follows a decline from buzzing again.
     */
    fun updateReady(context: Context, versionName: String, apkPath: String): NotificationSpec =
        NotificationSpec(
            channel = NotificationChannelId.UPDATE_ACTIONABLE,
            id = NotificationId.UpdateReady,
            title = context.getString(R.string.notification_update_ready_title),
            text = context.getString(R.string.notification_update_ready_text, versionName),
            autoCancel = false,
            alert = Alert.Once,
            route = NotificationRoute.InstallApk(apkPath),
        )

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
        // An APK download is the case Live Updates were designed for: long, ongoing, and with a
        // number worth glancing at. Inert below Android 16, where it stays an ordinary bar.
        promoted = true,
        shortCriticalText = percent?.let { context.getString(R.string.notification_update_percent, it) },
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
