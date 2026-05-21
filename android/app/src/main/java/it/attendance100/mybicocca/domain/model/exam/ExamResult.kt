package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalDateTime

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
