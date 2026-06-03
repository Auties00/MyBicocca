package it.attendance100.mybicocca.ui.screen.registry.state

import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.requiresStudentDecision
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DeadlineUrgency { Urgent, Upcoming }

// One entry on the scadenzario timeline. `kicker` is the short uppercase category
// label (e.g. "Esiti"), `title` the headline, `detail` the supporting line.
data class RegistryDeadline(
    val date: LocalDate,
    val kicker: String,
    val title: String,
    val detail: String?,
    val urgency: DeadlineUrgency,
    val onClick: () -> Unit,
    // true → the date is a cutoff to act by ("Entro N giorni"); false → an event that
    // happens then ("Tra N giorni").
    val byDeadline: Boolean = false,
)

// Anything due within this many days counts as "urgent" and drives the header badge.
private const val URGENT_DAYS = 7L

// Window the timeline looks ahead over ("prossimi 30 giorni" in the header). Overdue
// items (a few days back) still surface so a missed acceptance / payment doesn't vanish.
private const val LOOK_AHEAD_DAYS = 30L
private const val LOOK_BACK_DAYS = 7L

fun RegistryDeadline.isUrgent(): Boolean = urgency == DeadlineUrgency.Urgent

// Collapses the four feature streams into a single chronological deadline spine.
// Callbacks route each entry to the owning sub-screen. `today` is injected so the
// computation stays pure and testable.
fun buildRegistryDeadlines(
    today: LocalDate,
    examResults: List<ExamResult>,
    invoices: List<TaxInvoice>,
    bookings: List<BookedExam>,
    examCalls: List<ExamCall>,
    onOpenExamResults: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenBookedExams: () -> Unit,
): List<RegistryDeadline> {
    val deadlines = mutableListOf<RegistryDeadline>()

    // Exam outcomes still awaiting accept/reject — the most time-critical item.
    examResults.asSequence()
        .filter { it.requiresStudentDecision(today) }
        .forEach { result ->
            val due = result.acknowledgmentDeadline ?: return@forEach
            val grade = (result.grade as? ExamGrade.Numeric)?.value
            deadlines += RegistryDeadline(
                date = due,
                kicker = "Esiti",
                title = result.activityDescription ?: "Esito d'esame",
                detail = grade?.let { "Voto $it · da accettare o rifiutare" }
                    ?: "Esito da accettare o rifiutare",
                urgency = urgencyFor(today, due),
                onClick = onOpenExamResults,
            )
        }

    // Outstanding tuition instalments.
    invoices.asSequence()
        .filter { it.status == TaxStatus.PENDING || it.status == TaxStatus.EXPIRED }
        .forEach { invoice ->
            val due = invoice.expiration ?: return@forEach
            deadlines += RegistryDeadline(
                date = due,
                kicker = "Tasse",
                title = invoice.title,
                detail = "${formatAmount(invoice.amount)} · PagoPA",
                // An expired instalment is always urgent regardless of the date window.
                urgency = if (invoice.status == TaxStatus.EXPIRED) DeadlineUrgency.Urgent else urgencyFor(today, due),
                onClick = onOpenTaxes,
            )
        }

    // Exams the student has already booked.
    bookings.asSequence()
        .mapNotNull { booking -> booking.examDateTime?.toLocalDate()?.let { it to booking } }
        .forEach { (date, booking) ->
            deadlines += RegistryDeadline(
                date = date,
                kicker = "Appello",
                title = booking.activityDescription ?: "Esame prenotato",
                detail = listOfNotNull(
                    booking.examDateTime?.toLocalTime()?.let { "Ore %02d:%02d".format(it.hour, it.minute) },
                    booking.classroomDescription,
                ).joinToString(" · ").ifBlank { null },
                urgency = urgencyFor(today, date),
                onClick = onOpenBookedExams,
            )
        }

    // Booking windows about to close — one entry per activity, soonest deadline kept.
    examCalls.asSequence()
        .mapNotNull { call -> call.enrollmentWindow.closesAt?.let { it to call } }
        .filter { (closes, _) -> !closes.isBefore(today) }
        .groupBy { (_, call) -> call.activityDescription ?: call.activityCode ?: "" }
        .forEach { (_, group) ->
            val (closes, call) = group.minByOrNull { it.first } ?: return@forEach
            deadlines += RegistryDeadline(
                date = closes,
                kicker = "Prenotazioni",
                title = call.activityDescription ?: "Appello d'esame",
                detail = "Chiusura iscrizioni",
                urgency = urgencyFor(today, closes),
                onClick = onOpenBookedExams,
                byDeadline = true,
            )
        }

    val from = today.minusDays(LOOK_BACK_DAYS)
    val to = today.plusDays(LOOK_AHEAD_DAYS)
    return deadlines
        .filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
        .sortedBy { it.date }
}

private fun urgencyFor(today: LocalDate, date: LocalDate): DeadlineUrgency {
    val days = ChronoUnit.DAYS.between(today, date)
    return if (days <= URGENT_DAYS) DeadlineUrgency.Urgent else DeadlineUrgency.Upcoming
}

private fun formatAmount(amount: Double): String =
    "€ " + String.format(java.util.Locale.ITALIAN, "%,.2f", amount)
