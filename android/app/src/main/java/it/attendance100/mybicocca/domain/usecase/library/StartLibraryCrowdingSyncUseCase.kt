package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingSyncScheduler
import javax.inject.Inject

/**
 * Ensures the daily initial crowding-history collector is scheduled at app start unless it has
 * already finished permanently.
 */
class StartLibraryCrowdingSyncUseCase @Inject constructor(
    private val historyRepository: LibraryCrowdingHistoryRepository,
    private val scheduler: LibraryCrowdingSyncScheduler,
) {
    suspend operator fun invoke() {
        if (!historyRepository.isInitialCollectionDone()) {
            scheduler.schedule()
        }
    }
}
