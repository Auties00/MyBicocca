package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Whether this published outcome still awaits the student's accept/reject decision.
 *
 * Per the Esse3 calesa API, the student may reject (`presaVisione=R`) or accept (`A`)
 * only up to `dataRifEsitoStu` (mapped to [ExamResult.acknowledgmentDeadline]); once that
 * date passes Esse3 auto-verbalises the grade and no decision is possible. A decision is
 * therefore pending only when the outcome has not been acted on yet (presa visione N or
 * V) and the rejection window is still open (deadline today or later). When the deadline
 * is absent the outcome is not rejectable (e.g. auto-verbalised), so it never requires a
 * decision.
 */
fun ExamResult.requiresStudentDecision(today: LocalDate): Boolean {
    if (applicationListId == null) return false
    if (acknowledgment != AcknowledgmentStatus.NotViewed && acknowledgment != AcknowledgmentStatus.Viewed) {
        return false
    }
    val deadline = acknowledgmentDeadline ?: return false
    return !deadline.isBefore(today)
}

/**
 * A published exam outcome the student can review — what students know as the "Bacheca
 * Esiti". Sourced live from Esse3's bookings list filtered to rows with a published
 * outcome, and rendered by the exam-results sheet in the registry tab, where the student
 * can accept or reject grades that are still within their decision window.
 *
 * @property key Identity of the exam call the outcome belongs to.
 * @property applicationListId Esse3 prenotazione id, required by the accept/reject
 *   (presa visione) call; null when the server omits it, which makes the outcome
 *   read-only.
 * @property publicationId Esse3 `pubblId`; its presence is what marks the outcome as
 *   published.
 * @property activityDescription Name of the examined activity, falling back to the call
 *   description when the libretto name is blank.
 * @property examDateTime Date and time of the shift the student sat.
 * @property grade The published outcome.
 * @property acknowledgment The student's presa visione state for this outcome.
 * @property publishedNote Public note the teacher attached to the publication.
 * @property acknowledgmentDeadline Last day the student may accept or reject, from Esse3
 *   `dataRifEsitoStu` (a DD/MM/YYYY date with a fixed 23:59:59 time component).
 */
data class ExamResult(
    val key: ExamCallKey,
    val applicationListId: Long?,
    val publicationId: Long?,
    val activityDescription: String?,
    val examDateTime: LocalDateTime?,
    val grade: ExamGrade,
    val acknowledgment: AcknowledgmentStatus,
    val publishedNote: String?,
    val acknowledgmentDeadline: LocalDate?,
)

/**
 * The outcome of an exam attempt as Esse3 publishes it. [Unknown] is the sentinel for
 * outcomes that are missing or not yet interpretable (e.g. unpublished).
 */
sealed interface ExamGrade {
    /**
     * A numeric grade. For Italian exams the passing range is 18–30, and 31
     * conventionally encodes "30 cum laude"; the raw value is exposed and the UI decides
     * how to render it.
     */
    data class Numeric(val value: Int) : ExamGrade
    data object Passed : ExamGrade
    data object NotPassed : ExamGrade
    data object Withdrew : ExamGrade
    data object Absent : ExamGrade
    data object Unknown : ExamGrade
}

/**
 * The student's presa visione state on a published outcome: untouched ([NotViewed]),
 * opened but undecided ([Viewed]), or decided ([Accepted]/[Rejected]). [Unknown] covers
 * any unrecognised Esse3 code.
 */
enum class AcknowledgmentStatus {
    NotViewed,
    Viewed,
    Accepted,
    Rejected,
    Unknown,
}
