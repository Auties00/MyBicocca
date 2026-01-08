package it.attendance100.mybicocca.data.dto.easystaff

import java.time.LocalDate
import java.time.LocalTime

/**
 * Search mode for lesson schedule queries.
 */
enum class EasyStaffScheduleSearchMode(val includeValue: String, val formType: String) {
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
        fun fromIncludeValue(value: String): EasyStaffScheduleSearchMode? {
            return entries.find { it.includeValue == value }
        }
    }
}

/**
 * Represents a teacher in the system.
 *
 * @property id The teacher's internal ID
 * @property name The teacher's full name
 */
data class EasyStaffTeacher(
    val id: String,
    val name: String
) {
    override fun toString(): String = name
}

/**
 * Represents a subject/course (insegnamento) that can be scheduled.
 *
 * @property id The subject's internal ID
 * @property code The subject code
 * @property name The subject name
 * @property teacherName The primary teacher's name
 * @property periodId The teaching period ID
 */
data class EasyStaffSubject(
    val id: String,
    val code: String,
    val name: String,
    val teacherName: String,
    val periodId: String
) {
    override fun toString(): String = name
}

/**
 * A scheduled lesson/event in the timetable.
 *
 * @property title The event title (usually the subject name)
 * @property date The date of the lesson
 * @property startTime The start time
 * @property endTime The end time
 * @property room The room where the lesson takes place
 * @property building The building containing the room
 * @property teachers List of teachers for this lesson
 * @property eventType The type of event
 * @property notes Additional notes or description
 * @property subjectCode The subject code if available
 */
data class EasyStaffScheduledLesson(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String?,
    val building: String?,
    val teachers: List<String>,
    val eventType: EasyStaffLessonEventType,
    val notes: String?,
    val subjectCode: String?
)

/**
 * Type of lesson/event.
 */
enum class EasyStaffLessonEventType(val italianName: String) {
    LESSON("Lezione"),
    EXERCISE("Esercitazione"),
    LAB("Laboratorio"),
    SEMINAR("Seminario"),
    TUTORING("Tutorato"),
    EXAM("Esame"),
    OTHER("Altro");

    companion object {
        /**
         * Parses an event type from Italian text.
         *
         * @param text The Italian event type text
         * @return The matching [EasyStaffLessonEventType]
         */
        fun fromItalian(text: String): EasyStaffLessonEventType {
            val normalized = text.trim().lowercase()
            return when {
                normalized.contains("lezione") -> LESSON
                normalized.contains("esercitazione") -> EXERCISE
                normalized.contains("laboratorio") || normalized.contains("lab") -> LAB
                normalized.contains("seminario") -> SEMINAR
                normalized.contains("tutorato") || normalized.contains("tutoring") -> TUTORING
                normalized.contains("esame") -> EXAM
                else -> OTHER
            }
        }
    }
}

/**
 * A weekly schedule containing lessons for multiple days.
 *
 * @property weekStartDate The Monday of the displayed week
 * @property weekEndDate The Sunday of the displayed week
 * @property studyProgram The study program if searching by program
 * @property yearOfStudy The year of study if applicable
 * @property lessons All lessons in the week
 */
data class EasyStaffWeeklySchedule(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val studyProgram: EasyStaffStudyProgram?,
    val yearOfStudy: EasyStaffYearOfStudy?,
    val lessons: List<EasyStaffScheduledLesson>
) {
    /**
     * Gets lessons for a specific day of the week.
     *
     * @param date The date to filter by
     * @return Lessons on that date, sorted by start time
     */
    fun getLessonsForDay(date: LocalDate): List<EasyStaffScheduledLesson> {
        return lessons.filter { it.date == date }.sortedBy { it.startTime }
    }

    /**
     * Gets lessons grouped by day.
     *
     * @return Map of date to lessons
     */
    fun getLessonsByDay(): Map<LocalDate, List<EasyStaffScheduledLesson>> {
        return lessons.groupBy { it.date }.mapValues { (_, lessons) ->
            lessons.sortedBy { it.startTime }
        }
    }
}

/**
 * Parameters for searching lesson schedules by study program.
 *
 * @property academicYear The academic year
 * @property teachingAreaCode The teaching area code
 * @property studyProgramCode The study program code
 * @property yearsOfStudy The years of study to include
 * @property weekStartDate The starting date for the schedule
 * @property displayMode The display mode for results
 * @property teachingPeriodCode Optional teaching period filter
 */
data class EasyStaffScheduleByProgramQuery(
    val academicYear: EasyStaffAcademicYear,
    val teachingAreaCode: String,
    val studyProgramCode: String,
    val yearsOfStudy: List<String>,
    val weekStartDate: LocalDate,
    val displayMode: EasyStaffScheduleDisplayMode = EasyStaffScheduleDisplayMode.WEEKLY_AGENDA,
    val teachingPeriodCode: String? = null
)

/**
 * Parameters for searching lesson schedules by teacher.
 *
 * @property academicYear The academic year
 * @property teacherId The teacher's ID
 * @property weekStartDate The starting date for the schedule
 */
data class EasyStaffScheduleByTeacherQuery(
    val academicYear: EasyStaffAcademicYear,
    val teacherId: String,
    val weekStartDate: LocalDate
)

/**
 * Parameters for searching lesson schedules by subject.
 *
 * @property academicYear The academic year
 * @property teachingAreaCode The teaching area code
 * @property subjectId The subject ID
 * @property weekStartDate The starting date for the schedule
 */
data class EasyStaffScheduleBySubjectQuery(
    val academicYear: EasyStaffAcademicYear,
    val teachingAreaCode: String,
    val subjectId: String,
    val weekStartDate: LocalDate
)

/**
 * Options available for schedule searches, loaded from the server.
 *
 * @property academicYears Available academic years
 * @property teachingAreas Available teaching areas
 */
data class EasyStaffScheduleSearchOptions(
    val academicYears: List<EasyStaffAcademicYear>,
    val teachingAreas: List<EasyStaffTeachingArea>
)

/**
 * Study programs available for a teaching area.
 *
 * @property studyPrograms List of available study programs with their details
 */
data class EasyStaffStudyProgramsForArea(
    val studyPrograms: List<EasyStaffStudyProgramDetails>
)

/**
 * Detailed study program information including years and subjects.
 *
 * @property code The program code
 * @property name The program name
 * @property degreeType The degree type
 * @property internalId The internal database ID
 * @property teachingAreaCode The teaching area code
 * @property years The available years of study
 * @property teachingPeriods The available teaching periods
 * @property defaultDisplayMode The default schedule display mode
 */
data class EasyStaffStudyProgramDetails(
    val code: String,
    val name: String,
    val degreeType: EasyStaffDegreeType,
    val internalId: String,
    val teachingAreaCode: String,
    val years: List<EasyStaffYearOfStudyDetails>,
    val teachingPeriods: List<EasyStaffTeachingPeriod>,
    val defaultDisplayMode: EasyStaffScheduleDisplayMode
)

/**
 * Detailed year of study including available subjects.
 *
 * @property value The API value (e.g., "GGG|1")
 * @property year The numeric year
 * @property label The display label
 * @property trackName The curriculum/track name
 * @property subjects Subjects available in this year
 */
data class EasyStaffYearOfStudyDetails(
    val value: String,
    val year: Int,
    val label: String,
    val trackName: String,
    val subjects: List<EasyStaffSubject>
)
