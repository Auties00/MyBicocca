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

    override suspend fun doWork(): Result {
        try {
            // Trigger fresh checks for both Stable and Nightly. announce = true (the default, kept
            // explicit here deliberately): this worker is the primary reason announce exists at all
            // — it's the one path that can discover an update with nobody looking, so it's the one
            // that most needs the app-wide snackbar to actually fire once the app is next opened.
            //
            // TODO(update-notifications): if this worker's own auto-download/install below already
            // finished a release by the time the app reopens and drains the buffered event, MainShell
            // will still try to download it again (see /UPDATE_NOTIFICATIONS_PLAN.md) — nothing here
            // marks the release as "already handled" for that consumer. Worth folding into the same
            // notification work rather than solving twice.
            repository.checkForUpdates(force = true, announce = true)

            // Process Stable Update
            val stableState = updateStateStore.state.first()
            val stableAutoDownload = updateStateStore.stableAutoDownload.first()

            if (stableState.available && stableState.release != null) {
                if (stableAutoDownload) {
                    apkDownloader.startDownload(stableState.release)
                    val terminalState = apkDownloader.downloadState
                        .first { it is DownloadState.Success || it is DownloadState.Error }
                    if (terminalState is DownloadState.Success) {
                        // Stable never auto-installs. Notify user.
                        // TODO(update-notifications): see /UPDATE_NOTIFICATIONS_PLAN.md — also
                        // needs setForeground()/a progress notification so this download survives
                        // the app being backgrounded, not just the terminal "ready" notification.
                    }
                }
            }

            // Process Nightly Update if Beta is enabled
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
                                // Silent install
                                apkDownloader.installApk(terminalState.file, silent = true)
                            } else {
                                // Do nothing, wait for user to open app
                                // TODO(update-notifications): see /UPDATE_NOTIFICATIONS_PLAN.md — also
                                // needs setForeground()/a progress notification so this download survives
                                // the app being backgrounded, not just the terminal "ready" notification.
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
