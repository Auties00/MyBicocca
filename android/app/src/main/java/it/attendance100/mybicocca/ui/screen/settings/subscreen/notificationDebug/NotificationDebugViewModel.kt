package it.attendance100.mybicocca.ui.screen.settings.subscreen.notificationDebug

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.notification.Alert
import it.attendance100.mybicocca.core.notification.GroupKey
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.core.notification.NotificationSpec
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.notification.AppNotifier
import it.attendance100.mybicocca.data.notification.UpdateNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the debug-only notification screen: one canned spec per shape the poster supports, fired
 * on demand.
 *
 * Its value is the feedback loop. The alternative way to see any of these is to wait for a real
 * release to be published, let a background check find it, and download it — minutes per attempt,
 * and several of the shapes (a denied permission, a throttled progress tick, a group summary)
 * are awkward to reach at all that way.
 */
@HiltViewModel
class NotificationDebugViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notifier: AppNotifier,
    private val updateStateStore: UpdateStateStore,
) : ViewModel() {

    private val _lastResult = MutableStateFlow("")
    val lastResult: StateFlow<String> = _lastResult.asStateFlow()

    fun canNotify(channel: NotificationChannelId): Boolean = notifier.canNotify(channel)

    fun updateAvailable() = post(
        "Update available",
        NotificationSpec(
            channel = NotificationChannelId.UPDATE_ACTIONABLE,
            id = NotificationId.UpdateAvailable,
            title = "Nuova versione disponibile",
            text = "v9.9.9 — tocca per aprire",
            bigText = "Riga espansa, per controllare che BigTextStyle sia applicato.",
            route = NotificationRoute.UpdatePage,
        ),
    )

    fun progress(percent: Int) =
        post("Progress $percent%", UpdateNotifications.downloadProgress(context, percent))

    fun indeterminateProgress() =
        post("Indeterminate", UpdateNotifications.downloadProgress(context, percent = null))

    /**
     * Routes to the real downloaded APK when there is one, so the install tap can be exercised
     * end to end; falls back to the update page when nothing has been downloaded yet.
     */
    fun readyToInstall() {
        viewModelScope.launch {
            val apkPath = updateStateStore.downloadedApk.first()?.path
            post(
                if (apkPath != null) "Ready to install (real APK)" else "Ready to install (no APK)",
                NotificationSpec(
                    channel = NotificationChannelId.UPDATE_ACTIONABLE,
                    id = NotificationId.UpdateReady,
                    title = "Aggiornamento pronto",
                    text = "Tocca per installare",
                    alert = Alert.Once,
                    route = apkPath?.let(NotificationRoute::InstallApk) ?: NotificationRoute.UpdatePage,
                ),
            )
        }
    }

    /** Two notifications sharing a group key, which should render under one generated summary. */
    fun grouped() {
        val group = GroupKey("debug-group")
        post(
            "Grouped 1",
            NotificationSpec(
                channel = NotificationChannelId.UPDATE_ACTIONABLE,
                id = NotificationId.Entity("debug", "one"),
                title = "Primo del gruppo",
                group = group,
            ),
        )
        post(
            "Grouped 2",
            NotificationSpec(
                channel = NotificationChannelId.UPDATE_ACTIONABLE,
                id = NotificationId.Entity("debug", "two"),
                title = "Secondo del gruppo",
                group = group,
            ),
        )
    }

    fun cancelProgress() {
        notifier.cancel(NotificationId.UpdateProgress)
        _lastResult.value = "Progress cancelled"
    }

    fun cancelAll() {
        NotificationChannelId.entries.forEach(notifier::cancelAll)
        _lastResult.value = "All channels cleared"
    }

    private fun post(label: String, spec: NotificationSpec) {
        _lastResult.value = if (notifier.post(spec)) {
            "$label — posted"
        } else {
            "$label — suppressed (throttled, or the channel can't notify)"
        }
    }
}
