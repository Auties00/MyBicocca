package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state

import it.attendance100.mybicocca.domain.model.exam.BookedExam
import java.time.LocalDate
import java.time.LocalDateTime

// The two sub-lists the Prenotazioni modal splits into, in footer/pager order.
// Attive = appelli still to sit (today or later, plus any with no date yet);
// Passate = appelli already sat, the read-only register that carries the outcome.
enum class BookedFilter(val label: String) {
    Active("Attive"),
    Past("Passate"),
}

// A booking is Past once its appello date is strictly before today. Undated bookings
// (no dataOraTurno yet) are treated as Active — they can't be in the past.
fun BookedExam.bookedFilter(today: LocalDate): BookedFilter {
    val date = examDateTime?.toLocalDate() ?: return BookedFilter.Active
    return if (date.isBefore(today)) BookedFilter.Past else BookedFilter.Active
}

// Partition once into both buckets (always present, possibly empty), each sorted for its
// bucket: Attive by soonest appello first, Passate most-recent first.
fun List<BookedExam>.groupByFilter(today: LocalDate): Map<BookedFilter, List<BookedExam>> {
    val grouped = groupBy { it.bookedFilter(today) }
    return BookedFilter.entries.associateWith { filter ->
        val items = grouped[filter].orEmpty()
        when (filter) {
            BookedFilter.Active -> items.sortedBy { it.examDateTime ?: LocalDateTime.MAX }
            BookedFilter.Past -> items.sortedByDescending { it.examDateTime ?: LocalDateTime.MIN }
        }
    }
}
