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
 * Owns both of this app's update-check triggers: a foreground non-forced check on every resume,
 * and [AppUpdateWorker] as a periodic background job. Safe to call [start] again on every activity
 * recreation — re-adding the same singleton observer is a no-op.
 */
@Singleton
class UpdateChecker @Inject constructor(
    private val repository: UpdateRepository,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver {

    fun start() {
        // Reactive so a check-interval change reschedules the worker immediately.
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

        // WorkManager clamps below its own 15-minute floor.
        val workRequest = PeriodicWorkRequestBuilder<AppUpdateWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.MINUTES
            )
            .build()

        // UPDATE so a changed interval replaces the currently-enqueued schedule instead of a stale
        // one running forever.
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
