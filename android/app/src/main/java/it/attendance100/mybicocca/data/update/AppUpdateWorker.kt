package it.attendance100.mybicocca.data.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.core.version.isRunningBuild
import it.attendance100.mybicocca.data.local.settings.PersistedNightlyState
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.domain.model.update.AppRelease
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

    // The worker never installs: it only gets an update as far as "downloaded and ready", and the
    // user starts the install themselves from the foreground.
    // TODO(update-notifications): see /UPDATE_NOTIFICATIONS_PLAN.md — the downloads below have no
    // setForeground()/progress notification, so the OS can kill them mid-download while
    // backgrounded, and a finished download here still isn't reflected to MainShell, which may
    // redundantly redownload once the app reopens and drains the buffered event. Until that lands,
    // a background check surfaces nothing on its own; the next foreground open raises the snackbar.
    override suspend fun doWork(): Result {
        try {
            repository.checkForUpdates(force = true, announce = true)

            val stable = updateStateStore.state.first().availableRelease()
            if (stable != null && updateStateStore.stableAutoDownload.first()) {
                apkDownloader.startDownload(stable)
                apkDownloader.downloadState.first { it is DownloadState.Success || it is DownloadState.Error }
            }

            if (updateStateStore.nightlyEnabled.first()) {
                val nightly = updateStateStore.nightlyState.first().availableRelease()
                if (nightly != null && updateStateStore.nightlyAutoDownload.first()) {
                    apkDownloader.startDownload(nightly)
                    apkDownloader.downloadState.first { it is DownloadState.Success || it is DownloadState.Error }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}

/**
 * The release worth downloading, or null. Installing an update never clears the stored "available"
 * flag, so it stays set for the build that is now running — acting on it alone re-downloads and
 * re-offers the update the user just installed. This is the same reconciliation
 * `observeNightlyStatus` already applies before reporting a status to the UI.
 */
private fun PersistedUpdateState.availableRelease(): AppRelease? =
    release?.takeIf { available && !it.isRunningBuild() }

private fun PersistedNightlyState.availableRelease(): AppRelease? =
    release?.takeIf { available && !it.isRunningBuild() }
