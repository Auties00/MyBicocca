package it.attendance100.mybicocca.ui.screen.settings.subscreen.notificationDebug

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.notification.GroupKey
import it.attendance100.mybicocca.core.notification.NotificationChannelId
import it.attendance100.mybicocca.core.notification.NotificationId
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

    /**
     * Surfaced because the failure is otherwise invisible: a Live Update that isn't promoted looks
     * exactly like an ordinary progress notification, so "the chip never appeared" gives no clue
     * whether the permission is missing, the OEM declined, or the spec is shaped wrong.
     */
    fun canPromoteOngoing(): Boolean = notifier.canPromoteOngoing()

    fun updateAvailable() =
        post("Update available", UpdateNotifications.updateAvailable(context, DEBUG_VERSION))

    fun progress(percent: Int) =
        post("Progress $percent%", UpdateNotifications.downloadProgress(context, percent))

    fun indeterminateProgress() =
        post("Indeterminate", UpdateNotifications.downloadProgress(context, percent = null))

    /**
     * Fires the production spec against the real downloaded APK when there is one, so the install
     * tap can be exercised end to end. With nothing downloaded it still posts — the tap then lands
     * on a path that isn't there, which is itself worth seeing.
     */
    fun readyToInstall() {
        viewModelScope.launch {
            val apkPath = updateStateStore.downloadedApk.first()?.path
            post(
                if (apkPath != null) "Ready to install (real APK)" else "Ready to install (no APK)",
                UpdateNotifications.updateReady(context, DEBUG_VERSION, apkPath ?: "/no/such.apk"),
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

    private companion object {
        const val DEBUG_VERSION = "9.9.9"
    }

    private fun post(label: String, spec: NotificationSpec) {
        _lastResult.value = if (notifier.post(spec)) {
            "$label — posted"
        } else {
            "$label — suppressed (throttled, or the channel can't notify)"
        }
    }
}
