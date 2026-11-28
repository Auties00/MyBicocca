package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.library.LibraryCrowdingHistoryStore
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Live crowding-history repository backed by the dedicated JSON Preferences DataStore. */
@Singleton
class LibraryCrowdingHistoryRepositoryImpl @Inject constructor(
    private val store: LibraryCrowdingHistoryStore,
) : LibraryCrowdingHistoryRepository {

    override fun observeHistory(libraryId: String): Flow<Map<DayOfWeek, LibraryWeekdayCrowding>> =
        store.observeHistory(libraryId)

    override suspend fun getHistory(libraryId: String): Map<DayOfWeek, LibraryWeekdayCrowding> =
        store.getHistory(libraryId)

    override suspend fun upsert(libraryId: String, entry: LibraryWeekdayCrowding) =
        store.upsert(libraryId, entry)

    override suspend fun pruneExpired() = store.pruneExpired()

    override suspend fun firstCollectionRunAt(): Instant? = store.firstCollectionRunAt()

    override suspend fun recordFirstCollectionRunAt(at: Instant) = store.recordFirstCollectionRunAt(at)

    override suspend fun isInitialCollectionDone(): Boolean = store.isInitialCollectionDone()

    override suspend fun markInitialCollectionDone() = store.markInitialCollectionDone()
}
