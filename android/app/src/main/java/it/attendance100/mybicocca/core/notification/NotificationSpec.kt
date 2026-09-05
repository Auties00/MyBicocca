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
 * @property baseWallClockMs the `System.currentTimeMillis()` the timer counts from or to.
 *   Wall-clock, not `elapsedRealtime()`: the platform converts the notification's `when` into the
 *   chronometer's own base itself (`when + elapsedRealtime() - currentTimeMillis()`), so handing
 *   it an uptime reading puts the base decades in the past and the timer starts at that age.
 */
data class Chronometer(val baseWallClockMs: Long, val countDown: Boolean = false)

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
    /**
     * How to alert instead while the app is on screen. Null keeps [alert] whatever the app is
     * doing.
     *
     * [Alert.Never] is the usual choice for anything the shell also raises a snackbar for: the
     * user is being told already, so a second buzz is noise — but the tray entry is still worth
     * posting, because a snackbar is gone in seconds and a notification is what they come back to.
     */
    val foregroundAlert: Alert? = null,
    val ongoing: Boolean = false,
    val autoCancel: Boolean = true,
    /** Self-dismisses after this long. Null leaves it until cancelled or dismissed. */
    val timeoutAfterMs: Long? = null,
    val progress: Progress? = null,
    /**
     * Asks Android 16 to promote this into the status-bar chip and a richer lock-screen
     * presentation — a "Live Update". Only meaningful alongside [ongoing] and [progress]; below
     * API 36 it is inert and the notification stays an ordinary progress notification.
     */
    val promoted: Boolean = false,
    /**
     * The handful of characters the status-bar chip has room for, e.g. "42%". Shown only where
     * [promoted] takes effect; the chip is too small for [title] or [text].
     */
    val shortCriticalText: String? = null,
    val chronometer: Chronometer? = null,
    val route: NotificationRoute? = null,
    val actions: List<NotificationAction> = emptyList(),
    val group: GroupKey? = null,
    /** False lets the notification bridge to a paired watch. */
    val localOnly: Boolean = true,
) {
    /**
     * How this should actually announce itself right now. Kept here rather than in the poster so
     * the rule is comparable in a test without a device.
     */
    fun effectiveAlert(foregrounded: Boolean): Alert =
        if (foregrounded) foregroundAlert ?: alert else alert

    /** Progress as a percentage, clamped, or null when this isn't a determinate-progress spec. */
    val progressPercent: Int?
        get() = (progress as? Progress.Determinate)?.percent?.coerceIn(0, 100)
}
