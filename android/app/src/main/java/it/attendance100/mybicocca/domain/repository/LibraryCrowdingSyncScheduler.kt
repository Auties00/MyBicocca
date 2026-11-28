package it.attendance100.mybicocca.domain.repository

/** Schedules or cancels the initial daily background library crowding collection worker. */
interface LibraryCrowdingSyncScheduler {
    fun schedule()
    fun cancel()
}
