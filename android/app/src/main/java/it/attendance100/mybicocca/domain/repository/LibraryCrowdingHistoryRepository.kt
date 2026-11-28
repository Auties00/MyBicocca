package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.Instant

/**
 * Local persistence for per-library, per-weekday crowding history and the initial background
 * collection lifecycle. Entries older than 28 days are ignored on read and pruned on write.
 */
interface LibraryCrowdingHistoryRepository {

    /** Streams valid (at most 28 days old) weekday crowding entries for [libraryId]. */
    fun observeHistory(libraryId: String): Flow<Map<DayOfWeek, LibraryWeekdayCrowding>>

    /** Reads valid (at most 28 days old) weekday crowding entries for [libraryId]. */
    suspend fun getHistory(libraryId: String): Map<DayOfWeek, LibraryWeekdayCrowding>

    /** Upserts [entry] for [libraryId] and prunes entries older than 28 days. */
    suspend fun upsert(libraryId: String, entry: LibraryWeekdayCrowding)

    /** Prunes entries older than 28 days across all stored libraries. */
    suspend fun pruneExpired()

    /** Timestamp of the initial background collector's first run, or null if not yet run. */
    suspend fun firstCollectionRunAt(): Instant?

    /** Records [at] as the first collection run timestamp if not already recorded. */
    suspend fun recordFirstCollectionRunAt(at: Instant)

    /** Whether initial background crowding collection has completed and stopped permanently. */
    suspend fun isInitialCollectionDone(): Boolean

    /** Marks initial background crowding collection as permanently completed. */
    suspend fun markInitialCollectionDone()
}
