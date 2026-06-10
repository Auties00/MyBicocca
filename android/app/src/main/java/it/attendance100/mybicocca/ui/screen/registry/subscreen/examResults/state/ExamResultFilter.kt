package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state

import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.requiresStudentDecision
import java.time.LocalDate

/**
 * The two sub-lists the Esiti modal splits into, in pager order. Pending = outcomes still
 * awaiting the student's accept/reject within an open decision window; Archived =
 * everything already settled (accepted, rejected, or auto-verbalised). Both halves come
 * from the same published-esiti feed.
 */
enum class ExamResultFilter(val label: String) {
    Pending("In sospeso"),
    Archived("Archiviati"),
}


fun ExamResult.examResultFilter(today: LocalDate): ExamResultFilter =
    if (requiresStudentDecision(today)) ExamResultFilter.Pending else ExamResultFilter.Archived

/**
 * Partitions once into both buckets (always present, possibly empty): pending sorted by
 * soonest decision deadline, archived kept in the server's most-recent-first order.
 */
fun List<ExamResult>.groupByFilter(today: LocalDate): Map<ExamResultFilter, List<ExamResult>> {
    val grouped = groupBy { it.examResultFilter(today) }
    return ExamResultFilter.entries.associateWith { filter ->
        val items = grouped[filter].orEmpty()
        when (filter) {
            ExamResultFilter.Pending -> items.sortedBy { it.acknowledgmentDeadline }
            ExamResultFilter.Archived -> items
        }
    }
}
