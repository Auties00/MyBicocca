package it.attendance100.mybicocca.data.dto.easystaff

import java.time.LocalDate
import java.time.LocalTime

/**
 * Search mode for exam calendar queries.
 */
enum class EasyStaffExamSearchMode(val includeValue: String, val formType: String) {
    /**
     * Search by study program (corso di studio).
     */
    BY_STUDY_PROGRAM("corso", "corso"),

    /**
     * Search by teacher (docente).
     */
    BY_TEACHER("docente", "docente"),

    /**
     * Search by subject/course (insegnamento/attività).
     */
    BY_SUBJECT("attivita", "attivita");

    companion object {
        /**
         * Gets a search mode by its include value.
         */
        fun fromIncludeValue(value: String): EasyStaffExamSearchMode? {
            return entries.find { it.includeValue == value }
        }
    }
}

/**
 * Represents a scheduled exam session.
 *
 * @property subjectName The name of the subject being examined
 * @property subjectCode The subject code
 * @property date The date of the exam
 * @property startTime The start time
 * @property endTime The end time (may be null if not specified)
 * @property room The room where the exam takes place
 * @property building The building containing the room
 * @property teachers List of teachers/examiners
 * @property notes Additional information about the exam
 * @property examType The type of exam (written, oral, etc.)
 */
data class EasyStaffScheduledExam(
    val subjectName: String,
    val subjectCode: String?,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val room: String?,
    val building: String?,
    val teachers: List<String>,
    val notes: String?,
    val examType: EasyStaffExamType?
)

/**
 * Type of exam.
 */
enum class EasyStaffExamType(val italianName: String) {
    WRITTEN("Scritto"),
    ORAL("Orale"),
    WRITTEN_AND_ORAL("Scritto e Orale"),
    PRACTICAL("Pratico"),
    PROJECT("Progetto"),
    LAB("Laboratorio"),
    OTHER("Altro");

    companion object {
        /**
         * Parses an exam type from Italian text.
         *
         * @param text The Italian exam type text
         * @return The matching [EasyStaffExamType]
         */
        fun fromItalian(text: String): EasyStaffExamType {
            val normalized = text.trim().lowercase()
            return when {
                normalized.contains("scritto e orale") || normalized.contains("scritto/orale") -> WRITTEN_AND_ORAL
                normalized.contains("scritto") -> WRITTEN
                normalized.contains("orale") -> ORAL
                normalized.contains("pratico") -> PRACTICAL
                normalized.contains("progetto") -> PROJECT
                normalized.contains("laboratorio") || normalized.contains("lab") -> LAB
                else -> OTHER
            }
        }
    }
}

/**
 * Parameters for searching exams by study program.
 *
 * @property academicYear The academic year
 * @property teachingAreaCode The teaching area code
 * @property studyProgramCode The study program code
 * @property yearsOfStudy The years of study to include
 * @property startDate The start date for the search range
 * @property endDate The end date for the search range
 */
data class EasyStaffExamsByProgramQuery(
    val academicYear: EasyStaffAcademicYear,
    val teachingAreaCode: String,
    val studyProgramCode: String,
    val yearsOfStudy: List<String>,
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Parameters for searching exams by teacher.
 *
 * @property academicYear The academic year
 * @property teacherId The teacher's ID
 * @property startDate The start date for the search range
 * @property endDate The end date for the search range
 */
data class EasyStaffExamsByTeacherQuery(
    val academicYear: EasyStaffAcademicYear,
    val teacherId: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Parameters for searching exams by subject.
 *
 * @property academicYear The academic year
 * @property teachingAreaCode The teaching area code
 * @property subjectId The subject ID
 * @property startDate The start date for the search range
 * @property endDate The end date for the search range
 */
data class EasyStaffExamsBySubjectQuery(
    val academicYear: EasyStaffAcademicYear,
    val teachingAreaCode: String,
    val subjectId: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Results from an exam search.
 *
 * @property exams The list of matching exams
 * @property searchSummary A summary of the search parameters
 */
data class EasyStaffExamSearchResults(
    val exams: List<EasyStaffScheduledExam>,
    val searchSummary: String
)
