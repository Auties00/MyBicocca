package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalDateTime

// A published outcome still awaiting the student's accept/reject decision. Per the Esse3
// calesa API, the student may reject (presaVisione=R) or accept (A) only up to
// `dataRifEsitoStu` (mapped to acknowledgmentDeadline); once that date passes Esse3
// auto-verbalises the grade and no decision is possible. So a decision is pending only
// when the outcome hasn't been acted on yet (presaVisione N or V) AND the rejection
// window is still open (deadline today or later). When the deadline is absent the
// outcome isn't rejectable (e.g. auto-verbalised), so it never requires a decision.
fun ExamResult.requiresStudentDecision(today: LocalDate): Boolean {
    if (applicationListId == null) return false
    if (acknowledgment != AcknowledgmentStatus.NotViewed && acknowledgment != AcknowledgmentStatus.Viewed) {
        return false
    }
    val deadline = acknowledgmentDeadline ?: return false
    return !deadline.isBefore(today)
}

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

sealed interface ExamGrade {
    // For Italian exams a numeric grade is 18-30; 31 conventionally means "30 cum laude".
    // We expose the raw value and let the UI decide how to render it.
    data class Numeric(val value: Int) : ExamGrade
    data object Passed : ExamGrade
    data object NotPassed : ExamGrade
    data object Withdrew : ExamGrade
    data object Absent : ExamGrade
    data object Unknown : ExamGrade
}

enum class AcknowledgmentStatus {
    NotViewed,
    Viewed,
    Accepted,
    Rejected,
    Unknown,
}
