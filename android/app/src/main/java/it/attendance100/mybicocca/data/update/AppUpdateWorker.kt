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

    // The worker never installs: it only gets an update as far as "downloaded and ready", and the
    // user starts the install themselves from the foreground.
    // TODO(update-notifications): see /NOTIFICATIONS_PLAN.md — the downloads below still have no
    // setForeground()/progress notification, so the OS can kill them mid-download. Note they are
    // now cancelled with this worker too, where they used to run on past it on the application
    // scope: that is the point (the worker owns what it waits on) but it does mean a stopped
    // worker stops the download until setForeground lands.
    override suspend fun doWork(): Result {
        try {
            repository.checkForUpdates(force = true, announce = true)

            val stable = updateStateStore.state.first().availableRelease()
            if (stable != null && updateStateStore.stableAutoDownload.first()) {
                apkDownloader.download(stable)
            }

            if (updateStateStore.nightlyEnabled.first()) {
                val nightly = updateStateStore.nightlyState.first().availableRelease()
                if (nightly != null && updateStateStore.nightlyAutoDownload.first()) {
                    apkDownloader.download(nightly)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
