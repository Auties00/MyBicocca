package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingSlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** Shared synthetic crowding curves used by both demo live status and demo weekly history. */
internal object DemoLibraryCrowdingData {

    private val weekdayShape: List<Triple<LocalTime, LocalTime, Int>> = listOf(
        Triple(LocalTime.of(8, 30), LocalTime.of(9, 30), 22),
        Triple(LocalTime.of(9, 30), LocalTime.of(10, 0), 40),
        Triple(LocalTime.of(10, 0), LocalTime.of(10, 30), 54),
        Triple(LocalTime.of(10, 30), LocalTime.of(11, 0), 64),
        Triple(LocalTime.of(11, 0), LocalTime.of(12, 30), 72),
        Triple(LocalTime.of(12, 30), LocalTime.of(14, 0), 52),
        Triple(LocalTime.of(14, 0), LocalTime.of(15, 30), 68),
        Triple(LocalTime.of(15, 30), LocalTime.of(17, 0), 80),
        Triple(LocalTime.of(17, 0), LocalTime.of(18, 30), 66),
        Triple(LocalTime.of(18, 30), LocalTime.of(19, 30), 48),
        Triple(LocalTime.of(19, 30), LocalTime.of(20, 30), 34),
        Triple(LocalTime.of(20, 30), LocalTime.of(22, 0), 18),
    )

    private val saturdayShape: List<Triple<LocalTime, LocalTime, Int>> = listOf(
        Triple(LocalTime.of(9, 0), LocalTime.of(10, 0), 26),
        Triple(LocalTime.of(10, 0), LocalTime.of(11, 30), 48),
        Triple(LocalTime.of(11, 30), LocalTime.of(12, 30), 42),
        Triple(LocalTime.of(12, 30), LocalTime.of(13, 45), 24),
    )

    fun weekdaySlots(baselineOccupancy: Int, dayVariation: Int): List<LibraryCrowdingSlot> =
        weekdayShape.map { (start, end, base) ->
            val scaled = ((base + dayVariation) * baselineOccupancy / 64).coerceIn(5, 96)
            LibraryCrowdingSlot(start = start, end = end, occupancyPercentage = scaled)
        }

    fun saturdaySlots(baselineOccupancy: Int): List<LibraryCrowdingSlot> =
        saturdayShape.map { (start, end, base) ->
            val scaled = (base * baselineOccupancy / 64).coerceIn(5, 92)
            LibraryCrowdingSlot(start = start, end = end, occupancyPercentage = scaled)
        }
}

/**
 * In-memory crowding history pre-seeded with a full week (Monday through Sunday) for both demo
 * libraries relative to [DemoClock.now], with Sunday closed and no disk persistence.
 */
@Singleton
class DemoLibraryCrowdingHistoryRepository @Inject constructor() : LibraryCrowdingHistoryRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private val historyByLibrary = MutableStateFlow(
        mapOf(
            LibraryCatalog.CENTRALE_ID to buildWeek(baselineOccupancy = 64),
            LibraryCatalog.MEDICINA_ID to buildWeek(baselineOccupancy = 38),
        ),
    )

    override fun observeHistory(libraryId: String): Flow<Map<DayOfWeek, LibraryWeekdayCrowding>> =
        historyByLibrary.map { it[libraryId].orEmpty() }

    override suspend fun getHistory(libraryId: String): Map<DayOfWeek, LibraryWeekdayCrowding> =
        historyByLibrary.value[libraryId].orEmpty()

    override suspend fun upsert(libraryId: String, entry: LibraryWeekdayCrowding) {
        historyByLibrary.update { all ->
            val updated = all[libraryId].orEmpty() + (entry.weekday to entry)
            all + (libraryId to updated)
        }
    }

    override suspend fun pruneExpired() = Unit

    override suspend fun firstCollectionRunAt(): Instant? = DemoClock.fixed.instant()

    override suspend fun recordFirstCollectionRunAt(at: Instant) = Unit

    override suspend fun isInitialCollectionDone(): Boolean = true

    override suspend fun markInitialCollectionDone() = Unit

    private fun buildWeek(baselineOccupancy: Int): Map<DayOfWeek, LibraryWeekdayCrowding> {
        val variations = mapOf(
            DayOfWeek.MONDAY to -4,
            DayOfWeek.TUESDAY to 4,
            DayOfWeek.WEDNESDAY to 6,
            DayOfWeek.THURSDAY to 0,
            DayOfWeek.FRIDAY to -10,
        )
        return DayOfWeek.entries.associateWith { weekday ->
            val daysAgo = ((today.dayOfWeek.value - weekday.value) + 7) % 7
            val entryDate = today.minusDays(daysAgo.toLong())
            when (weekday) {
                DayOfWeek.SUNDAY -> LibraryWeekdayCrowding(
                    weekday = weekday,
                    date = entryDate,
                    slots = emptyList(),
                    isClosed = true,
                )
                DayOfWeek.SATURDAY -> LibraryWeekdayCrowding(
                    weekday = weekday,
                    date = entryDate,
                    slots = DemoLibraryCrowdingData.saturdaySlots(baselineOccupancy),
                    isClosed = false,
                )
                else -> LibraryWeekdayCrowding(
                    weekday = weekday,
                    date = entryDate,
                    slots = DemoLibraryCrowdingData.weekdaySlots(baselineOccupancy, variations[weekday] ?: 0),
                    isClosed = false,
                )
            }
        }
    }
}
