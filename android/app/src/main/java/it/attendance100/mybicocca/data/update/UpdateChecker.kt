package it.attendance100.mybicocca.data.update

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Fires the once-a-day update check. Observes the process lifecycle (like the app-lock manager)
 * and, on every foreground, asks the repository for a non-forced check — which is a no-op while
 * the daily freshness window holds, so a frequently-reopened app still hits the network at most
 * once per day. The work runs on the application scope so it outlives the brief foreground event
 * and never blocks startup; failures are swallowed, since a missed update check is harmless and
 * surfaces nothing to the user.
 */
@Singleton
class UpdateChecker @Inject constructor(
    private val repository: UpdateRepository,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver {

    /**
     * Registers the process-lifecycle observer and the periodic background check. Called from the
     * activity's onCreate; safe to call again on every activity recreation, since re-adding the
     * same singleton observer is a no-op and re-collecting the interval setting just re-emits the
     * current value.
     */
    fun start() {
        // Reactive so a change to the check-interval setting (see the Update Settings slider)
        // reschedules the periodic worker immediately, not just on the next app start.
        scope.launch {
            repository.observeCheckIntervalMinutes()
                .distinctUntilChanged()
                .collectLatest { intervalMinutes -> enqueuePeriodicWork(intervalMinutes) }
        }

        scope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@UpdateChecker)
        }
    }

    private fun enqueuePeriodicWork(intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // WorkManager silently clamps anything below its own 15-minute floor, so intervalMinutes
        // is only meaningful at or above that.
        val workRequest = PeriodicWorkRequestBuilder<AppUpdateWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.MINUTES
            )
            .build()

        // UPDATE (not KEEP) so a changed interval — from the settings slider, or a schedule left
        // over from an older build — replaces whatever is currently enqueued instead of running
        // forever under the stale one.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AppUpdateWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        scope.launch { runCatching { repository.checkForUpdates(force = false) } }
    }
}
