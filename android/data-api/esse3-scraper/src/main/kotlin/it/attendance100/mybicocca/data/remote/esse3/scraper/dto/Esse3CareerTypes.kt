package it.attendance100.mybicocca.data.remote.esse3.scraper.dto

import java.time.LocalDate

/**
 * Academic record (libretto) containing all courses and statistics.
 */
data class Esse3AcademicRecord(
    val courses: List<Esse3Course>,
    val unweightedGpa: Double,
    val weightedGpa: Double,
)

/**
 * A single course entry in the academic record.
 */
data class Esse3Course(
    val code: String,
    val name: String,
    val urlPath: String,
    val year: Int,
    val credits: Int,
    val academicYear: String,
    val grade: Esse3Grade?,
    val examDate: LocalDate?,
    val examAttemptsUrlPath: String?
)

/**
 * Detailed information about a course.
 */
data class Esse3CourseDetails(
    val code: String,
    val name: String,
    val courseCode: String,
    val courseName: String,
    val degreeCode: String,
    val degreeDescription: String,
    val year: Int,
    val status: Esse3CourseStatus,
    val examDate: LocalDate?,
    val grade: Esse3Grade?,
    val notes: String,
    val units: List<Esse3CourseUnit>
)

/**
 * Status of a course in the academic record details.
 */
sealed class Esse3CourseStatus {
    data object NotAttended : Esse3CourseStatus()
    data class Attended(val year: String) : Esse3CourseStatus()
    data class Passed(val year: String, val attendedYear: String) : Esse3CourseStatus()
}

/**
 * A didactic unit (module) within a course.
 */
data class Esse3CourseUnit(
    val name: String,
    val activityType: String,
    val formationType: String,
    val sector: String,
    val credits: Int,
    val duration: Int?
)

/**
 * An exam attempt for a course.
 */
data class Esse3ExamAttempt(
    val examDate: LocalDate?,
    val examType: String,
    val outcome: Esse3ExamAttemptOutcome,
    val verbalizationDate: LocalDate?
)

/**
 * Outcome of an exam attempt.
 */
sealed class Esse3ExamAttemptOutcome {
    data class Passed(val grade: Esse3Grade) : Esse3ExamAttemptOutcome()
    data object Failed : Esse3ExamAttemptOutcome()
    data object Absent : Esse3ExamAttemptOutcome()
    data object Withdrawn : Esse3ExamAttemptOutcome()
    data object Booked : Esse3ExamAttemptOutcome()
}

/**
 * Grade representation.
 */
sealed class Esse3Grade {
    /**
     * Numeric grade (18-30 or 30L for cum laude).
     */
    data class Numeric(
        val value: Int,
        val cumLaude: Boolean
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
     * Approved without numeric grade (APPR - Approvato).
     */
    data object Approved : Esse3Grade() {
        override fun toString(): String = "APPR"
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
                trimmed == "APPR" || trimmed == "APPROVATO" -> Approved
                trimmed == "ASS" || trimmed == "ASSENTE" -> Absent
                trimmed == "INS" || trimmed == "INSUFFICIENTE" -> Failed
                trimmed == "RIT" || trimmed == "RITIRATO" -> Withdrawn
                trimmed == "30L" || trimmed == "30 E LODE" -> Numeric(30, true)
                trimmed.toIntOrNull() != null -> Numeric(trimmed.toInt(), false)
                else -> null
            }
        }
    }
}

/**
 * Study plan information.
 */
data class Esse3StudyPlan(
    val status: Esse3PlanStatus,
    val type: String,
    val lastModified: LocalDate,
    val offerYear: Int,
    val regulationYear: Int,
    val courses: List<Esse3PlannedCourse>
)

/**
 * A course in the study plan.
 */
data class Esse3PlannedCourse(
    val code: String,
    val description: String,
    val year: Int,
    val university: String
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
        fun fromString(value: String): Esse3PlanStatus? {
            return when (value.trim().uppercase()) {
                "APPROVATO" -> APPROVED
                "IN ATTESA" -> PENDING
                "RIFIUTATO" -> REJECTED
                "BOZZA" -> DRAFT
                else -> null
            }
        }
    }
}