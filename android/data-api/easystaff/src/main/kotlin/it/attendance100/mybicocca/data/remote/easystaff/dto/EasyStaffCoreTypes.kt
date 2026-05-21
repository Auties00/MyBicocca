package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a teacher entity within the EasyStaff system.
 *
 * @param id Internal system identifier for the teacher record.
 * @param fullName The full display name of the teacher (e.g., "ABBOTTO ALESSANDRO").
 * @param code The unique administrative staff identifier.
 * @param facultyId The numerical identifier for the associated faculty.
 * @param primaryFacultyCode The alphanumeric code representing the teacher's primary faculty.
 * @param activePeriods List of active teaching periods/semesters associated with this teacher.
 * @param courses List of degree courses where this teacher is currently assigned.
 */
@Serializable
data class EasyStaffTeacher(
    @SerialName("id")
    val id: Int,

    @SerialName("label")
    val fullName: String,

    @SerialName("valore")
    val code: String,

    @SerialName("facolta_id")
    val facultyId: Int,

    @SerialName("facolta_code")
    val primaryFacultyCode: String,

    @SerialName("periodi")
    val activePeriods: List<EasyStaffTeacherPeriod> = emptyList(),

    @SerialName("teacher_cdl")
    val courses: List<EasyStaffTeacherCourse> = emptyList()
)

/**
 * Defines a specific academic period or semester for a specific faculty area.
 *
 * @param id Internal identifier for this specific period entry.
 * @param description Descriptive name of the period (e.g., "Primo semestre").
 * @param code Combined code representing the semester and faculty (e.g., "S1|CHIM").
 * @param academicYearId Identifier for the academic year this period belongs to.
 * @param facultyCode The faculty code specific to this period entry.
 */
@Serializable
data class EasyStaffTeacherPeriod(
    @SerialName("id")
    val id: Int,

    @SerialName("label")
    val description: String,

    @SerialName("valore")
    val code: String,

    @SerialName("aa_id")
    val academicYearId: Int,

    @SerialName("facolta_code")
    val facultyCode: String
)

/**
 * Represents a Degree Course (Corso di Laurea) and its associated curriculum paths.
 *
 * @param name Full name of the degree program.
 * @param code Official university code for the degree program.
 * @param yearsOfStudy List of specific curriculum paths or tracks within this degree course.
 */
@Serializable
data class EasyStaffTeacherCourse(
    @SerialName("CorsoLaureaNome")
    val name: String,

    @SerialName("CorsoLaureaCodice")
    val code: String,

    @SerialName("elencoCurriculum")
    val yearsOfStudy: List<EasyStaffTeacherCourseYearOfStudy> = emptyList(),
)

/**
 * Represents a specific curriculum path, often including year and track information.
 *
 * @param code Code identifying the curriculum and year (e.g., "GGG|1").
 * @param name Display name for the curriculum path.
 */
@Serializable
data class EasyStaffTeacherCourseYearOfStudy(
    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String
)

/**
 * Represents an academic year in the format YYYY/YYYY+1 (e.g., 2024/2025).
 *
 * @property startYear The starting year of the academic year
 */
@Serializable
data class EasyStaffAcademicYear(
    @SerialName("valore")
    val startYear: Int
) {
    /**
     * The ending year of the academic year.
     */
    val endYear: Int get() = startYear + 1

    /**
     * The display label (e.g., "2024/2025").
     */
    val label: String get() = "$startYear/$endYear"

    /**
     * The value used in API requests (e.g., "2024").
     */
    val value: String get() = startYear.toString()

    override fun toString(): String = label
}

/**
 * Represents a teaching area (faculty/school) in the university.
 *
 * @property code The internal code used in API requests
 * @property name The display name
 */
@Serializable
data class EasyStaffTeachingArea(
    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String
) {
    override fun toString(): String = name
}