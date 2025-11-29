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

data class GradePoint(
  val value: Float,
  val date: String,
  val name: String,
  val cfu: String,
  val isCumLaude: Boolean,
)

// We need ExamItem here or import it. It's in DTOs.
// Ideally domain models shouldn't depend on DTOs.
// But for speed, I'll duplicate or move ExamItem to domain?
// Or just use a domain version.
// Let's check if ExamItem is available here. It's in DTO package.
// I should probably create a domain model for Exam.
// But the user just said "update the models".
// I'll create a domain Exam model to be clean.

data class Exam(
  val name: String,
  val cfu: String,
  val grade: String?,
  val date: String?,
  val status: String, // "S" or "F"
  val isCumLaude: Boolean,
)
