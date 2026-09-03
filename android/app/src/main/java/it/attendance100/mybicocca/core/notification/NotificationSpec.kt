package it.attendance100.mybicocca.core.notification

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import it.attendance100.mybicocca.R

/** How loudly a post announces itself. */
enum class Alert {
    /** Alerts on every post, including a re-post to a slot already showing something. */
    Every,

    /** Alerts on the first post to a slot; later posts to it update silently. */
    Once,

    /** Never alerts. */
    Never,
}

sealed interface Progress {
    data class Determinate(val percent: Int) : Progress
    data object Indeterminate : Progress
}

/**
 * A running timer in place of the timestamp.
 *
 * @property baseElapsedRealtimeMs the `SystemClock.elapsedRealtime()` the timer counts from or to.
 */
data class Chronometer(val baseElapsedRealtimeMs: Long, val countDown: Boolean = false)

/** Notifications sharing a key are bundled, with a summary the poster generates. */
data class GroupKey(val key: String)

/** Where a tap goes. Resolved into an Activity `PendingIntent`, never a raw `Intent`. */
sealed interface NotificationRoute {
    /** The in-app update page, the same destination the Settings update tile opens. */
    data object UpdatePage : NotificationRoute

    /** Hands [apkPath] to the system package installer, which asks the user to confirm. */
    data class InstallApk(val apkPath: String) : NotificationRoute
}

/** What an action button does when tapped. */
sealed interface ActionIntent {
    /** Opens the app at [route]. */
    data class Open(val route: NotificationRoute) : ActionIntent

    /**
     * Fires a broadcast for the app to handle without opening it — cancelling a download, say.
     * Legitimate here where a tap's own intent is not: the trampoline ban is about a receiver
     * *starting an Activity*, which this must never do.
     */
    data class Broadcast(val action: String, val extras: Map<String, String> = emptyMap()) : ActionIntent
}

data class NotificationAction(
    val title: String,
    val intent: ActionIntent,
    @DrawableRes val icon: Int = 0,
)

/**
 * What to show, decided separately from how to post it. Every feature builds one of these;
 * nothing calls `NotificationCompat.Builder` itself.
 *
 * A value type rather than a builder DSL so the whole surface is comparable in a test and
 * loggable, and so the poster can enforce the platform's rules — trampoline-safe routing,
 * alpha-only icons, permission gating, progress throttling — in one place instead of trusting
 * every call site to remember them.
 */
data class NotificationSpec(
    val channel: NotificationChannelId,
    val id: NotificationId,
    val title: String,
    val text: String? = null,
    /** Expanded body. Shown in place of [text] when the user expands the notification. */
    val bigText: String? = null,
    /** Must be alpha-only: the system keeps the shape and discards every colour. */
    @DrawableRes val smallIcon: Int = R.drawable.notification,
    @ColorInt val color: Int? = null,
    val colorized: Boolean = false,
    val alert: Alert = Alert.Once,
    val ongoing: Boolean = false,
    val autoCancel: Boolean = true,
    /** Self-dismisses after this long. Null leaves it until cancelled or dismissed. */
    val timeoutAfterMs: Long? = null,
    val progress: Progress? = null,
    val chronometer: Chronometer? = null,
    val route: NotificationRoute? = null,
    val actions: List<NotificationAction> = emptyList(),
    val group: GroupKey? = null,
    /** False lets the notification bridge to a paired watch. */
    val localOnly: Boolean = true,
) {
    /** Progress as a percentage, clamped, or null when this isn't a determinate-progress spec. */
    val progressPercent: Int?
        get() = (progress as? Progress.Determinate)?.percent?.coerceIn(0, 100)
}
