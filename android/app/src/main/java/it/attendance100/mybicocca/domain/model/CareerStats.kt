package it.attendance100.mybicocca.domain.model

/**
 * Career statistics business model
 * Represents the student's academic progress and grades
 */
data class CareerStats(
    val mediaAritmetica: Float,
    val mediaPonderata: Float,
    val esamiSostenuti: Int,
    val esamiTotali: Int,
    val cfuAcquisiti: Int,
    val cfuTotali: Int,
    val grades: List<GradePoint>,
    val passedExams: List<Exam>,
    val remainingExams: List<Exam>,
)

/**
 * Exam grade point
 * Represents a grade point for a specific exam
 */
data class GradePoint(
    val value: Float,
    val date: String, // ISO 8601 date
    val name: String,
    val cfu: String,
    val isLode: Boolean,
)

/**
 * Exam model
 * Represents a student's exam
 */
data class Exam(
    val name: String,
    val cfu: String,
    val grade: String?,
    val date: String?,
    val status: String, // "S" or "F" (Superato o Frequentato)
    val isLode: Boolean,
)
