package it.attendance100.mybicocca.data.library

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingSyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Live scheduler binding for [LibraryCrowdingSyncWorker]: daily periodic unique work with KEEP. */
@Singleton
class WorkManagerLibraryCrowdingSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : LibraryCrowdingSyncScheduler {

    override fun schedule() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<LibraryCrowdingSyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "library_crowding_initial_collection"
    }
}
