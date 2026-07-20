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

/** How pressing a deadline is; drives the timeline accent and the banner's urgent count. */
enum class DeadlineUrgency { Urgent, Upcoming }

/**
 * One entry on the scadenzario timeline. [kicker] is the short uppercase category label
 * (e.g. "Esiti"), [title] the headline, [detail] the supporting line; [onClick] routes to
 * the owning sub-screen.
 */
data class RegistryDeadline(
    val date: LocalDate,
    val kicker: String,
    val title: String,
    val detail: String?,
    val urgency: DeadlineUrgency,
    val onClick: () -> Unit,
    /**
     * True when the date is a cutoff to act by ("Entro N giorni"); false when it is an
     * event that happens then ("Tra N giorni").
     */
    val byDeadline: Boolean = false,
)

/** Anything due within this many days counts as urgent and feeds the banner's urgent count. */
private const val URGENT_DAYS = 7L

/** How far ahead the timeline looks ("prossimi 30 giorni" in the header copy). */
private const val LOOK_AHEAD_DAYS = 30L

/**
 * Overdue items up to this many days back still surface, so a missed acceptance or
 * payment doesn't silently vanish from the timeline.
 */
private const val LOOK_BACK_DAYS = 7L

fun RegistryDeadline.isUrgent(): Boolean = urgency == DeadlineUrgency.Urgent

/**
 * Collapses the four feature streams into a single chronological deadline spine, clamped
 * to the look-back/look-ahead window and sorted by date. [today] is injected so the
 * computation stays pure and testable; the callbacks route each entry to its owning
 * sub-screen. The sources, each mapped to one kicker:
 * - exam outcomes still awaiting the student's accept/reject decision — the most
 *   time-critical entries;
 * - outstanding tuition instalments, where an expired instalment is always urgent
 *   regardless of the date window;
 * - upcoming booked-exam sittings — the bookings feed also carries the full past
 *   register, and a sat appello is not a deadline;
 * - booking windows about to close, collapsed to one entry per activity keeping the
 *   soonest closing date, and restricted to activities in the student's piano di studi
 *   when [studyPlanCodes] is known — Esse3's bookable list also carries calls for
 *   libretto rows outside the current plan, which are noise here.
 */
fun buildRegistryDeadlines(
    today: LocalDate,
    examResults: List<ExamResult>,
    invoices: List<TaxInvoice>,
    bookings: List<BookedExam>,
    examCalls: List<ExamCall>,
    onOpenExamResults: () -> Unit,
    onOpenTaxes: () -> Unit,
    onOpenBookedExams: () -> Unit,
    studyPlanCodes: Set<String>? = null,
): List<RegistryDeadline> {
    val deadlines = mutableListOf<RegistryDeadline>()

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

    invoices.asSequence()
        .filter { it.status == TaxStatus.PENDING || it.status == TaxStatus.EXPIRED }
        .forEach { invoice ->
            val due = invoice.expiration ?: return@forEach
            deadlines += RegistryDeadline(
                date = due,
                kicker = "Tasse",
                title = invoice.title,
                detail = "${formatAmount(invoice.amount)} · PagoPA",
                urgency = if (invoice.status == TaxStatus.EXPIRED) DeadlineUrgency.Urgent else urgencyFor(today, due),
                onClick = onOpenTaxes,
            )
        }

    bookings.asSequence()
        .mapNotNull { booking -> booking.examDateTime?.toLocalDate()?.let { it to booking } }
        .filter { (date, _) -> !date.isBefore(today) }
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

    examCalls.asSequence()
        .filter { it.isInStudyPlan(studyPlanCodes) }
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

/**
 * Whether the call's activity belongs to the student's piano di studi. Fail-open: with no
 * plan available ([planCodes] null or empty) or no activity code on the call there is
 * nothing to check against, so the call stays visible rather than silently vanishing.
 */
private fun ExamCall.isInStudyPlan(planCodes: Set<String>?): Boolean {
    if (planCodes.isNullOrEmpty()) return true
    val code = activityCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return true
    return code in planCodes
}

private fun urgencyFor(today: LocalDate, date: LocalDate): DeadlineUrgency {
    val days = ChronoUnit.DAYS.between(today, date)
    return if (days <= URGENT_DAYS) DeadlineUrgency.Urgent else DeadlineUrgency.Upcoming
}

private fun formatAmount(amount: Double): String =
    "€ " + String.format(java.util.Locale.getDefault(), "%,.2f", amount)
