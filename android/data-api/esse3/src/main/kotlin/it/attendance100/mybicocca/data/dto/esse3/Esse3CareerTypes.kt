package it.attendance100.mybicocca.data.dto.esse3

import java.time.LocalDate

/**
 * Academic record (libretto) containing all courses and statistics.
 */
data class Esse3AcademicRecord(
    val courses: List<Esse3CourseEntry>,
    val statistics: Esse3RecordStatistics
)

/**
 * A single course entry in the academic record.
 */
data class Esse3CourseEntry(
    val code: String,
    val name: String,
    val year: Int,
    val credits: Int,
    val status: Esse3CourseStatus,
    val academicYear: String?,
    val grade: Esse3Grade?,
    val examDate: LocalDate?,
    val isRecognized: Boolean,
    val attempts: Int,
    val hasAvailableExams: Boolean,
    val questionnaireRequired: Boolean = false
)

/**
 * Grade representation.
 */
sealed class Esse3Grade {
    /**
     * Numeric grade (18-30 or 30L for cum laude).
     */
    data class Numeric(
        val value: Int,
        val cumLaude: Boolean = false
    ) : Esse3Grade() {
        override fun toString(): String = if (cumLaude) "30L" else value.toString()
    }

    /**
     * Passed without numeric grade (IDO - Idoneo).
     */
    data object Passed : Esse3Grade() {
        override fun toString(): String = "IDO"
    }

    /**
     * Absent from exam.
     */
    data object Absent : Esse3Grade() {
        override fun toString(): String = "ASS"
    }

    /**
     * Failed exam.
     */
    data object Failed : Esse3Grade() {
        override fun toString(): String = "INS"
    }

    /**
     * Withdrawn from exam.
     */
    data object Withdrawn : Esse3Grade() {
        override fun toString(): String = "RIT"
    }

    companion object {
        /**
         * Parses a grade string into an Esse3Grade.
         */
        fun parse(value: String): Esse3Grade? {
            val trimmed = value.trim().uppercase()
            return when {
                trimmed == "IDO" || trimmed == "IDONEO" -> Passed
                trimmed == "ASS" || trimmed == "ASSENTE" -> Absent
                trimmed == "INS" || trimmed == "INSUFFICIENTE" -> Failed
                trimmed == "RIT" || trimmed == "RITIRATO" -> Withdrawn
                trimmed == "30L" || trimmed == "30 E LODE" -> Numeric(30, true)
                trimmed.toIntOrNull() != null -> Numeric(trimmed.toInt())
                else -> null
            }
        }
    }
}

/**
 * Status of a course in the study plan.
 */
enum class Esse3CourseStatus {
    PLANNED,
    IN_PROGRESS,
    PASSED,
    FAILED;

    companion object {
        fun fromString(value: String): Esse3CourseStatus {
            return when (value.lowercase()) {
                "superato", "passed" -> PASSED
                "in corso", "in progress" -> IN_PROGRESS
                "non superato", "failed" -> FAILED
                else -> PLANNED
            }
        }
    }
}

/**
 * Statistics for the academic record.
 */
data class Esse3RecordStatistics(
    val arithmeticAverage: Double?,
    val weightedAverage: Double?,
    val totalCredits: Int,
    val earnedCredits: Int
)

/**
 * Study plan information.
 */
data class Esse3StudyPlan(
    val id: Long,
    val status: Esse3PlanStatus,
    val type: String,
    val lastModified: LocalDate?,
    val offerYear: Int?,
    val regulationYear: Int?,
    val courses: List<Esse3PlannedCourse>
)

/**
 * A course in the study plan.
 */
data class Esse3PlannedCourse(
    val code: String,
    val description: String,
    val status: String?,
    val credits: Int,
    val year: Int?,
    val university: String?
)

/**
 * Status of a study plan.
 */
enum class Esse3PlanStatus {
    APPROVED,
    PENDING,
    REJECTED,
    DRAFT;

    companion object {
        fun fromString(value: String): Esse3PlanStatus {
            return when (value.uppercase()) {
                "APPROVATO", "APPROVED" -> APPROVED
                "IN ATTESA", "PENDING" -> PENDING
                "RIFIUTATO", "REJECTED" -> REJECTED
                "BOZZA", "DRAFT" -> DRAFT
                else -> DRAFT
            }
        }
    }
}