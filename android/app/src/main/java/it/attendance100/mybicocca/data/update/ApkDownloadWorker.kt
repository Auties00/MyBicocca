package it.attendance100.mybicocca.data.update

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.core.notification.NotificationId
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.notification.AppNotifier
import it.attendance100.mybicocca.data.notification.UpdateNotifications
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DownloadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

/**
 * Downloads an update inside WorkManager's foreground service, which is what stops the OS
 * freezing and then killing the download seconds after the app is backgrounded.
 *
 * `dataSync` is not a service this app writes: `work-runtime` ships and registers
 * `SystemForegroundService`, and any worker calling `setForeground` runs inside it. The manifest
 * only declares the FGS type it runs under.
 *
 * The worker owns *lifetime*; [ApkDownloader] owns *mechanics*. That split only works because
 * `download` runs in the caller's coroutine, so the service covers exactly the work it protects
 * and cancelling this worker actually stops the download.
 */
@HiltWorker
class ApkDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val downloader: ApkDownloader,
    private val store: UpdateStateStore,
    private val notifier: AppNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo(percent = null)

    override suspend fun doWork(): Result {
        // Resolved here rather than carried in the input: a queued download can start long after
        // it was asked for, and the persisted state is what is still true by then.
        val release = resolveRelease()
        if (release == null) {
            // Nothing left to download — the update was installed or withdrawn between the
            // request and this run. Take back the "queued" the UI is showing on its behalf.
            downloader.clearEnqueued()
            return Result.success()
        }

        promoteToForeground()

        val outcome = downloader.download(release) { percent ->
            notifier.post(UpdateNotifications.downloadProgress(context, percent))
        }

        // Null means another download already holds the single-flight lock. Leave its notification
        // and its state alone: they belong to a download that is still running.
        if (outcome == null) return Result.success()

        notifier.cancel(NotificationId.UpdateProgress)
        clearServedRequest()

        return if (outcome is DownloadState.Success) Result.success() else Result.failure()
    }

    /**
     * Drops the explicit request now that it has been carried out. Only once the download has
     * actually ended: a worker killed mid-download is re-run, and re-running needs the release
     * still to be there.
     */
    private suspend fun clearServedRequest() {
        if (inputData.getString(KEY_SOURCE) == SOURCE_EXPLICIT) store.clearPendingDownloadRelease()
    }

    private suspend fun resolveRelease(): AppRelease? = when (inputData.getString(KEY_SOURCE)) {
        SOURCE_STABLE -> store.state.first().availableRelease()
        SOURCE_NIGHTLY -> store.nightlyState.first().availableRelease()
        SOURCE_EXPLICIT -> store.pendingDownloadRelease.first()
        else -> null
    }

    /**
     * The OS can refuse a foreground start outright — background-start restrictions, or the
     * `dataSync` budget being spent. Degrading to an ordinary worker means the download is
     * freezable again, which is worse than the alternative of not downloading at all, so the
     * failure is logged rather than fatal.
     *
     * The pause afterwards lets `Service.startForeground()` land before real work resumes.
     */
    private suspend fun promoteToForeground() {
        try {
            setForeground(getForegroundInfo())
            delay(500.milliseconds)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Foreground promotion refused; downloading unprotected", e)
        }
    }

    private fun foregroundInfo(percent: Int?): ForegroundInfo {
        val notification = notifier.build(UpdateNotifications.downloadProgress(context, percent))
        val id = NotificationId.UpdateProgress.value

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification)
        }
    }

    companion object {
        private const val TAG = "ApkDownloadWorker"

        /**
         * One key for every channel, not one per channel. [ApkDownloader] is single-flight
         * app-wide, so per-channel keys would let a second worker run whose download never
         * starts while its notification mirrors the first's bytes.
         */
        const val UNIQUE_WORK_NAME = "ApkDownloadWorker"

        const val KEY_SOURCE = "source"
        const val SOURCE_STABLE = "stable"
        const val SOURCE_NIGHTLY = "nightly"
        const val SOURCE_EXPLICIT = "explicit"

        fun enqueue(context: Context, source: String) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                // KEEP: a download already running is the one the user is watching. Replacing it
                // would restart from zero for no gain.
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ApkDownloadWorker>()
                    .setInputData(inputFor(source))
                    // Without this the worker runs offline, fails immediately and is done: a
                    // failed download is terminal, so the request would be spent rather than
                    // waiting for the connection it needs.
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build(),
            )
        }

        private fun inputFor(source: String): Data = workDataOf(KEY_SOURCE to source)
    }
}
