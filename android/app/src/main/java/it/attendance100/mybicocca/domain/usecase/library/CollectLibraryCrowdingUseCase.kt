package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingSyncScheduler
import kotlinx.coroutines.CancellationException
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/** Decision produced by [CollectLibraryCrowdingUseCase] for the thin background worker. */
enum class LibraryCrowdingCollectionOutcome {
    /** Collection ran with at least one successful fetch and still needs more weekdays. */
    Continue,
    /** Every library has all seven weekdays or 21 days elapsed; worker was cancelled permanently. */
    Completed,
    /** Every fetch failed transiently; worker should ask WorkManager to retry. */
    Retry,
}

/**
 * Executes one daily pass of initial crowding-history collection for all catalogued libraries and
 * permanently stops the periodic worker once every library has all seven weekdays or 21 days have
 * passed since the first run.
 */
class CollectLibraryCrowdingUseCase @Inject constructor(
    private val getLiveStatus: GetLibraryLiveStatusUseCase,
    private val historyRepository: LibraryCrowdingHistoryRepository,
    private val scheduler: LibraryCrowdingSyncScheduler,
) {
    suspend operator fun invoke(): LibraryCrowdingCollectionOutcome {
        if (historyRepository.isInitialCollectionDone()) {
            scheduler.cancel()
            return LibraryCrowdingCollectionOutcome.Completed
        }

        val now = Instant.now(AppClock.clock)
        val firstRunAt = historyRepository.firstCollectionRunAt()
            ?: now.also { historyRepository.recordFirstCollectionRunAt(it) }

        val targetIds = LibraryCatalog.entries.map { it.id }

        if (isComplete(targetIds) || !now.isBefore(firstRunAt.plus(MAX_COLLECTION_WINDOW))) {
            historyRepository.markInitialCollectionDone()
            scheduler.cancel()
            return LibraryCrowdingCollectionOutcome.Completed
        }

        var succeeded = 0
        for (libraryId in targetIds) {
            try {
                getLiveStatus(libraryId)
                succeeded++
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Throwable) {
                // Keep collecting the other library even if one fails.
            }
        }

        historyRepository.pruneExpired()

        if (isComplete(targetIds)) {
            historyRepository.markInitialCollectionDone()
            scheduler.cancel()
            return LibraryCrowdingCollectionOutcome.Completed
        }

        return if (succeeded == 0) {
            LibraryCrowdingCollectionOutcome.Retry
        } else {
            LibraryCrowdingCollectionOutcome.Continue
        }
    }

    private suspend fun isComplete(libraryIds: List<String>): Boolean =
        libraryIds.all { id ->
            historyRepository.getHistory(id).keys.containsAll(DayOfWeek.entries)
        }

    companion object {
        val MAX_COLLECTION_WINDOW: Duration = Duration.ofDays(21)
    }
}
