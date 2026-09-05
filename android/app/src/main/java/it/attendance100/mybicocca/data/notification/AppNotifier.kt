package it.attendance100.mybicocca.data.notification

import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.notification.Alert
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationImportance
import it.attendance100.mybicocca.core.notification.NotificationSpec
import it.attendance100.mybicocca.core.notification.Progress
import it.attendance100.mybicocca.core.notification.ProgressThrottle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one way a notification reaches the tray.
 *
 * Named for what it does rather than `NotificationManager`, which would collide with the platform
 * class every file here already imports.
 *
 * Centralising the post is the point: alert semantics, progress throttling, grouping and
 * trampoline-safe routing are each easy to get subtly wrong and impossible to notice from a call
 * site, so no feature builds a `NotificationCompat.Builder` itself.
 */
@Singleton
class AppNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: NotificationPermissions,
    private val router: NotificationRouter,
) {

    private val manager = NotificationManagerCompat.from(context)
    private val throttle = ProgressThrottle()

    /** Delegates to [NotificationPermissions.canNotify]; see there for why it never gates work. */
    fun canNotify(channel: NotificationChannelId): Boolean = permissions.canNotify(channel)

    /** Whether a [NotificationSpec.promoted] spec would actually reach the status-bar chip. */
    fun canPromoteOngoing(): Boolean = permissions.canPromoteOngoing()

    /**
     * Posts [spec], or doesn't. Returns whether it reached the tray, which callers may log but
     * should not act on: a false means the user won't see this, never that the work behind it
     * failed.
     */
    fun post(spec: NotificationSpec): Boolean {
        if (!permissions.canNotify(spec.channel)) return false

        synchronized(throttle) {
            if (!throttle.shouldPost(spec, SystemClock.elapsedRealtime())) return false
        }

        manager.notify(spec.id.value, build(spec))
        spec.group?.let { postSummary(spec) }
        return true
    }

    fun cancel(id: NotificationId) {
        manager.cancel(id.value)
        synchronized(throttle) { throttle.reset(id) }
    }

    /** Cancels every notification this app has posted to [channel]. */
    fun cancelAll(channel: NotificationChannelId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.activeNotifications
            .filter { it.notification.channelId == channel.id }
            .forEach { manager.cancel(it.id) }
    }

    /**
     * Builds the platform notification for [spec] without posting it. Public for the caller that
     * owns a notification's lifetime itself, rather than handing it to the tray.
     */
    fun build(spec: NotificationSpec): Notification {
        val builder = NotificationCompat.Builder(context, spec.channel.id)
            .setSmallIcon(spec.smallIcon)
            .setContentTitle(spec.title)
            .setContentText(spec.text)
            .setOngoing(spec.ongoing)
            .setAutoCancel(spec.autoCancel)
            .setLocalOnly(spec.localOnly)
            // Below API 26 there are no channels, so importance has to ride on the builder.
            .setPriority(spec.channel.importance.compatPriority())

        when (spec.alert) {
            Alert.Every -> Unit
            Alert.Once -> builder.setOnlyAlertOnce(true)
            Alert.Never -> builder.setSilent(true)
        }

        spec.bigText?.let { builder.setStyle(NotificationCompat.BigTextStyle().bigText(it)) }
        spec.color?.let { builder.setColor(it).setColorized(spec.colorized) }
        spec.timeoutAfterMs?.let(builder::setTimeoutAfter)

        when (val progress = spec.progress) {
            null -> Unit
            Progress.Indeterminate -> builder.setProgress(0, 0, true)
            is Progress.Determinate ->
                builder.setProgress(PROGRESS_MAX, progress.percent.coerceIn(0, PROGRESS_MAX), false)
        }

        // Android 16 promotes only through ProgressStyle, so a promoted spec gets it *as well as*
        // the plain bar above: below API 36 the style is inert and the bar is what renders, above
        // it the style wins. Applied after bigText, since a promoted progress notification is a
        // progress bar first and a spec asking for both is asking for the wrong thing.
        if (spec.promoted) {
            builder.setRequestPromotedOngoing(true)
            spec.shortCriticalText?.let(builder::setShortCriticalText)
            if (Build.VERSION.SDK_INT >= PROMOTED_ONGOING_SDK) {
                spec.progress?.let { builder.setStyle(progressStyle(it)) }
            }
        }

        spec.chronometer?.let {
            builder.setUsesChronometer(true)
                .setWhen(it.baseWallClockMs)
                .setChronometerCountDown(it.countDown)
        }

        spec.route?.let { builder.setContentIntent(router.contentIntent(it, spec.id.value)) }

        spec.actions.forEachIndexed { index, action ->
            builder.addAction(
                NotificationCompat.Action.Builder(
                    action.icon,
                    action.title,
                    // Distinct request code per action, or PendingIntent equality would collapse
                    // two actions on one notification into whichever was created first.
                    router.actionIntent(action.intent, actionRequestCode(spec.id, index)),
                ).build()
            )
        }

        spec.group?.let { builder.setGroup(it.key) }

        return builder.build()
    }

    /**
     * The bundle header Android shows above grouped notifications. Without one, a group of four
     * renders as four separate rows on some versions and an unlabelled stack on others.
     */
    private fun postSummary(spec: NotificationSpec) {
        val group = spec.group ?: return
        val summary = NotificationCompat.Builder(context, spec.channel.id)
            .setSmallIcon(spec.smallIcon)
            .setGroup(group.key)
            .setGroupSummary(true)
            .setPriority(spec.channel.importance.compatPriority())
            .setLocalOnly(spec.localOnly)
            .setSilent(true)
            .build()

        manager.notify(summaryIdFor(group.key), summary)
    }

    /**
     * The Android 16 progress bar. One segment spanning the whole track: segments exist to colour
     * distinct legs of a journey (a delivery's pickup, transit, arrival), and a download has one.
     */
    private fun progressStyle(progress: Progress): NotificationCompat.ProgressStyle =
        NotificationCompat.ProgressStyle()
            .setProgressSegments(listOf(NotificationCompat.ProgressStyle.Segment(PROGRESS_MAX)))
            .also { style ->
                when (progress) {
                    is Progress.Determinate ->
                        style.setProgress(progress.percent.coerceIn(0, PROGRESS_MAX))

                    Progress.Indeterminate -> style.setProgressIndeterminate(true)
                }
            }

    private companion object {
        const val PROGRESS_MAX = 100

        /** Android 16, where `ProgressStyle` and promoted ongoing notifications arrive. */
        const val PROMOTED_ONGOING_SDK = 36

        /**
         * Distinct per action *within one notification*, which is what the actions on a single
         * notification need: without it, PendingIntent equality collapses two buttons into
         * whichever was created first.
         *
         * Across notifications it is a hash, not an injection - the Long arithmetic keeps the
         * result stable and non-negative, but squeezing (id, index) into an Int cannot be
         * collision-free, and multiplying by 100 loses two more bits. Two entity notifications
         * whose ids differ by exactly 2^29 would share request codes. Live entity notifications
         * number in the dozens, so that is a theoretical loss rather than one to design around.
         */
        fun actionRequestCode(id: NotificationId, index: Int): Int =
            ((id.value.toLong() * 100L + index) and Int.MAX_VALUE.toLong()).toInt()

        /** Summaries live in the entity id space, keyed by group, so they can't hit a real slot. */
        fun summaryIdFor(groupKey: String): Int = NotificationId.idFor("group-summary", groupKey)
    }
}

/** Importance has no channel to live on below API 26, so it maps onto builder priority instead. */
internal fun NotificationImportance.compatPriority(): Int = when (this) {
    NotificationImportance.MIN -> NotificationCompat.PRIORITY_MIN
    NotificationImportance.LOW -> NotificationCompat.PRIORITY_LOW
    NotificationImportance.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
    NotificationImportance.HIGH -> NotificationCompat.PRIORITY_HIGH
}
