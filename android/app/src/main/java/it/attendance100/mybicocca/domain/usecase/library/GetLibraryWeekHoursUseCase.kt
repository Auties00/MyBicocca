package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Fetches the next seven days of opening hours starting today, combining this week's timetable
 * with next week's whenever fewer than seven days remain in the current week.
 */
class GetLibraryWeekHoursUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): LibraryWeekHours {
        val thisWeek = repository.getWeekHours(libraryId, weekOffset = 0)
        val today = thisWeek.days.firstOrNull { it.isToday }?.day ?: LocalDate.now(AppClock.clock)
        val fromThisWeek = thisWeek.days
            .filter { !it.day.isBefore(today) }
            .sortedBy { it.day }

        val combined = if (fromThisWeek.size >= DAYS_IN_WEEK) {
            fromThisWeek
        } else {
            val nextWeek = try {
                repository.getWeekHours(libraryId, weekOffset = 1).days
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Throwable) {
                emptyList()
            }
            (fromThisWeek + nextWeek)
                .filter { !it.day.isBefore(today) && it.day.isBefore(today.plusDays(DAYS_IN_WEEK.toLong())) }
                .distinctBy { it.day }
                .sortedBy { it.day }
        }

        val normalized = combined
            .take(DAYS_IN_WEEK)
            .map { dayHours -> dayHours.copy(isToday = dayHours.day == today) }
        return LibraryWeekHours(days = normalized)
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
