package it.attendance100.mybicocca.data.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
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

    override suspend fun doWork(): Result {
        try {
            // Trigger fresh checks for both Stable and Nightly
            repository.checkForUpdates(force = true)

            // 1. Process Stable Update
            val stableState = updateStateStore.state.first()
            val stableAutoDownload = updateStateStore.stableAutoDownload.first()

            if (stableState.available && stableState.release != null) {
                if (stableAutoDownload) {
                    apkDownloader.startDownload(stableState.release)
                    val terminalState = apkDownloader.downloadState
                        .first { it is DownloadState.Success || it is DownloadState.Error }
                    if (terminalState is DownloadState.Success) {
                        // 5. Stable never auto-installs. Notify user.
                        // TODO: Implement System Notification Manager to notify user of ready update
                    }
                }
            }

            // 2. Process Nightly Update if Beta is enabled
            val nightlyEnabled = updateStateStore.nightlyEnabled.first()
            if (nightlyEnabled) {
                val nightlyState = updateStateStore.nightlyState.first()
                if (nightlyState.available && nightlyState.release != null) {
                    val nightlyAutoDownload = updateStateStore.nightlyAutoDownload.first()
                    if (nightlyAutoDownload) {
                        apkDownloader.startDownload(nightlyState.release)
                        val terminalState = apkDownloader.downloadState
                            .first { it is DownloadState.Success || it is DownloadState.Error }

                        if (terminalState is DownloadState.Success) {
                            val nightlyAutoInstall = updateStateStore.nightlyAutoInstall.first()
                            if (nightlyAutoInstall) {
                                // 4. Silent install
                                apkDownloader.installApk(terminalState.file, silent = true)
                            } else {
                                // 5. Do nothing, wait for user to open app
                                // TODO: Implement System Notification Manager to notify user of ready update
                            }
                        }
                    }
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            // Log to Crashlytics or similar in production
            return Result.failure()
        }
    }
}
