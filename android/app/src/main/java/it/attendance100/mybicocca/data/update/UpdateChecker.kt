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
 * recreation — internally guarded to run its setup once per process.
 */
@Singleton
class UpdateChecker @Inject constructor(
    private val repository: UpdateRepository,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val apkDownloader: ApkDownloader,
) : DefaultLifecycleObserver {

    // start() is called from the activity's onCreate, which can re-run within the same process
    // (e.g. the OS recreating the activity after reclaiming memory while backgrounded) even though
    // this singleton outlives it — without this guard, each call launches another never-cancelled
    // collector on the application scope, so a later interval change fires enqueuePeriodicWork once
    // per launched collector instead of once.
    private var started = false

    fun start() {
        if (started) return
        started = true

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

        // REPLACE (not UPDATE): UPDATE is meant to preserve identity across an in-place change, but
        // a periodic work's platform-level JobScheduler entry can't actually be mutated once
        // scheduled, and in practice the old entry wasn't reliably cleaned up on reschedule
        // (observed via Background Task Inspector: a new SystemJobService row per interval change,
        // old ones never clearing). REPLACE explicitly cancels-and-deletes before enqueueing fresh
        // — losing an in-progress run isn't a real cost for a lightweight periodic check.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AppUpdateWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        // Coming back from the system installer with an install still pending means it was
        // dismissed; this is the only signal ACTION_VIEW gives us that it happened.
        apkDownloader.onAppForegrounded()
        scope.launch { runCatching { repository.checkForUpdates(force = false) } }
    }

    override fun onStop(owner: LifecycleOwner) {
        apkDownloader.onAppBackgrounded()
    }
}
