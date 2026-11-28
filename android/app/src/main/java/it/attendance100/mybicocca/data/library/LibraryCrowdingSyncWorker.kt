package it.attendance100.mybicocca.data.library

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.domain.usecase.library.CollectLibraryCrowdingUseCase
import it.attendance100.mybicocca.domain.usecase.library.LibraryCrowdingCollectionOutcome

/**
 * Daily background collector that populates the seven-day crowding history for every catalogued
 * library even before the student opens Biblioteca. All stop/retry logic lives in
 * [CollectLibraryCrowdingUseCase].
 */
@HiltWorker
class LibraryCrowdingSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val collectLibraryCrowding: CollectLibraryCrowdingUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (collectLibraryCrowding()) {
        LibraryCrowdingCollectionOutcome.Continue,
        LibraryCrowdingCollectionOutcome.Completed -> Result.success()
        LibraryCrowdingCollectionOutcome.Retry -> Result.retry()
    }
}
