package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.model.library.toCrowdingSlot
import it.attendance100.mybicocca.domain.repository.LibraryCrowdingHistoryRepository
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Upserts today's crowding history entry from a freshly fetched [LibraryLiveStatus].
 *
 * The entry's calendar date and weekday come strictly from the server payload (forecast ranges or
 * timetable), never the device clock. An empty forecast is stored as closed only when the
 * timetable confirms today is closed; otherwise an empty forecast is not stored.
 */
class RecordLibraryCrowdingUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val historyRepository: LibraryCrowdingHistoryRepository,
) {
    suspend operator fun invoke(
        libraryId: String,
        liveStatus: LibraryLiveStatus,
        knownWeekHours: LibraryWeekHours? = null,
    ) {
        if (liveStatus.todayForecast.isNotEmpty()) {
            val date = liveStatus.forecastDate ?: return
            val slots = liveStatus.todayForecast.map { it.toCrowdingSlot() }
            historyRepository.upsert(
                libraryId = libraryId,
                entry = LibraryWeekdayCrowding(
                    weekday = date.dayOfWeek,
                    date = date,
                    slots = slots,
                    isClosed = false,
                ),
            )
            return
        }

        val hours = knownWeekHours ?: try {
            libraryRepository.getWeekHours(libraryId, weekOffset = 0)
        } catch (cause: CancellationException) {
            throw cause
        } catch (_: Throwable) {
            return
        }

        val todayEntry = liveStatus.forecastDate?.let { date ->
            hours.days.firstOrNull { it.day == date }
        } ?: hours.days.firstOrNull { it.isToday }
            ?: return

        if (todayEntry.ranges.isEmpty()) {
            historyRepository.upsert(
                libraryId = libraryId,
                entry = LibraryWeekdayCrowding(
                    weekday = todayEntry.day.dayOfWeek,
                    date = todayEntry.day,
                    slots = emptyList(),
                    isClosed = true,
                ),
            )
        }
    }
}
