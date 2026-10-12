package it.attendance100.mybicocca.data.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.domain.repository.UpdateRepository

@HiltWorker
class AppUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UpdateRepository,
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

            repository.scheduleAutoDownload()

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
