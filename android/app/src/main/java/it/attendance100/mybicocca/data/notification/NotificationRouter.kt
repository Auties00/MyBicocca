package it.attendance100.mybicocca.data.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.MyBicoccaActivity
import it.attendance100.mybicocca.core.notification.ActionIntent
import it.attendance100.mybicocca.core.notification.NotificationRoute
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a [NotificationRoute] into a `PendingIntent`.
 *
 * Every route launches [MyBicoccaActivity] **directly**. Since Android 12 a notification's tap
 * intent may not land on a `BroadcastReceiver` or `Service` that then starts an Activity — the
 * system drops it and the tap does nothing, with no error anywhere. Routing through one type that
 * only ever builds `getActivity` intents is what makes that impossible to get wrong.
 *
 * The Activity is `singleTask`, so a tap while the app is already running delivers to the existing
 * instance through `onNewIntent` rather than starting a second one.
 */
@Singleton
class NotificationRouter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun contentIntent(route: NotificationRoute, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MyBicoccaActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtras(route)
            },
            PENDING_INTENT_FLAGS,
        )

    fun actionIntent(intent: ActionIntent, requestCode: Int): PendingIntent = when (intent) {
        is ActionIntent.Open -> contentIntent(intent.route, requestCode)
        is ActionIntent.Broadcast -> PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(intent.action).apply {
                setPackage(context.packageName)
                intent.extras.forEach { (key, value) -> putExtra(key, value) }
            },
            PENDING_INTENT_FLAGS,
        )
    }

    private fun Intent.putExtras(route: NotificationRoute) {
        when (route) {
            NotificationRoute.UpdatePage -> putExtra(EXTRA_ROUTE, ROUTE_UPDATE_PAGE)
            is NotificationRoute.InstallApk -> {
                putExtra(EXTRA_ROUTE, ROUTE_INSTALL_APK)
                putExtra(EXTRA_ROUTE_ARG, route.apkPath)
            }
        }
    }

    companion object {
        const val EXTRA_ROUTE = "it.attendance100.mybicocca.extra.NOTIFICATION_ROUTE"
        const val EXTRA_ROUTE_ARG = "it.attendance100.mybicocca.extra.NOTIFICATION_ROUTE_ARG"

        const val ROUTE_UPDATE_PAGE = "update_page"
        const val ROUTE_INSTALL_APK = "install_apk"

        // IMMUTABLE is required from API 31 and correct everywhere: nothing that receives one of
        // these intents may rewrite its contents. UPDATE_CURRENT so re-posting a slot refreshes
        // the extras rather than reusing the first tap's stale ones.
        private const val PENDING_INTENT_FLAGS =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        /** Reads back what [putExtras] wrote, or null when [intent] carries no route. */
        fun routeOf(intent: Intent?): NotificationRoute? = when (intent?.getStringExtra(EXTRA_ROUTE)) {
            ROUTE_UPDATE_PAGE -> NotificationRoute.UpdatePage
            ROUTE_INSTALL_APK ->
                intent.getStringExtra(EXTRA_ROUTE_ARG)?.let(NotificationRoute::InstallApk)

            else -> null
        }
    }
}
