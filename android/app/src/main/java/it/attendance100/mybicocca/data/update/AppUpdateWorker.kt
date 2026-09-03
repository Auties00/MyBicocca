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
    private val updateStateStore: UpdateStateStore
) : CoroutineWorker(context, params) {

    // The worker never installs: it only gets an update as far as "downloaded and ready", and the
    // user starts the install themselves from the foreground.
    //
    // Downloading is handed to ApkDownloadWorker rather than done here, so it runs inside a
    // foreground service and survives being backgrounded. Result.success() therefore means "the
    // check ran and a download was scheduled", not "an update was downloaded" — nothing reads it
    // but WorkManager's retry logic, which is the right meaning for it anyway.
    override suspend fun doWork(): Result {
        try {
            repository.checkForUpdates(force = true, announce = true)

            autoDownloadSource()?.let { ApkDownloadWorker.enqueue(context, it) }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    /**
     * Which channel, if any, should be downloaded now — at most one.
     *
     * Both channels can have an update waiting at once, but there is a single downloaded-APK slot
     * app-wide, so downloading both would leave only the second one recorded and the first's file
     * orphaned. Enqueueing both is worse still: ApkDownloadWorker is unique work under KEEP, so
     * the second request is dropped outright rather than queued.
     *
     * Stable wins, matching the precedence `UpdateRepository.availableRelease()` already offers
     * updates in. The loser isn't starved: installing the winner clears its slot, and the next
     * check offers the other one.
     */
    private suspend fun autoDownloadSource(): String? {
        val stable = updateStateStore.state.first().availableRelease()
        if (stable != null && updateStateStore.stableAutoDownload.first()) {
            return ApkDownloadWorker.SOURCE_STABLE
        }

        if (!updateStateStore.nightlyEnabled.first()) return null

        val nightly = updateStateStore.nightlyState.first().availableRelease()
        if (nightly != null && updateStateStore.nightlyAutoDownload.first()) {
            return ApkDownloadWorker.SOURCE_NIGHTLY
        }

        return null
    }
}
