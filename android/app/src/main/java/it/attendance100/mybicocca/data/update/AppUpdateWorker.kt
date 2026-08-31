package it.attendance100.mybicocca.data.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class AppUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UpdateRepository,
    private val apkDownloader: ApkDownloader,
    private val updateStateStore: UpdateStateStore
) : CoroutineWorker(context, params) {

    // TODO(update-notifications): see /UPDATE_NOTIFICATIONS_PLAN.md — the downloads below have no
    // setForeground()/progress notification, so the OS can kill them mid-download while
    // backgrounded, and a finished download here still isn't reflected to MainShell, which may
    // redundantly redownload once the app reopens and drains the buffered event.
    override suspend fun doWork(): Result {
        try {
            repository.checkForUpdates(force = true, announce = true)

            val stableState = updateStateStore.state.first()
            val stableAutoDownload = updateStateStore.stableAutoDownload.first()
            if (stableState.available && stableState.release != null && stableAutoDownload) {
                apkDownloader.startDownload(stableState.release)
                apkDownloader.downloadState.first { it is DownloadState.Success || it is DownloadState.Error }
                // Stable never auto-installs; the user installs manually once notified.
            }

            val nightlyEnabled = updateStateStore.nightlyEnabled.first()
            if (nightlyEnabled) {
                val nightlyState = updateStateStore.nightlyState.first()
                val nightlyAutoDownload = updateStateStore.nightlyAutoDownload.first()
                if (nightlyState.available && nightlyState.release != null && nightlyAutoDownload) {
                    apkDownloader.startDownload(nightlyState.release)
                    val terminalState = apkDownloader.downloadState
                        .first { it is DownloadState.Success || it is DownloadState.Error }
                    if (terminalState is DownloadState.Success && updateStateStore.nightlyAutoInstall.first()) {
                        apkDownloader.installApk(terminalState.file, silent = true)
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
