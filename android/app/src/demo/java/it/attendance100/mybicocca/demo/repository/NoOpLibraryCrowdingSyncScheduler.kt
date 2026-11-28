package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.domain.repository.LibraryCrowdingSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

/** Demo no-op scheduler: the demo build never schedules background WorkManager sync jobs. */
@Singleton
class NoOpLibraryCrowdingSyncScheduler @Inject constructor() : LibraryCrowdingSyncScheduler {
    override fun schedule() = Unit
    override fun cancel() = Unit
}
