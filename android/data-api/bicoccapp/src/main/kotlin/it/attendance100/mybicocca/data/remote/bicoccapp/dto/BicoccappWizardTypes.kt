package it.attendance100.mybicocca.data.remote.bicoccapp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing available departments.
 */
@Serializable
internal data class BicoccappWizardDepartmentsResponse(
    /**
     * List of categories.
     */
    @SerialName("categories")
    val departments: List<BicoccappWizardDepartment> = emptyList()
)

/**
 * Represents a category in the wizard (e.g., Department or Faculty).
 */
@Serializable
data class BicoccappWizardDepartment(
    /**
     * Name of the category.
     */
    @SerialName("name")
    val name: String,
    /**
     * Code identifier for the category.
     */
    @SerialName("code")
    val code: String,
)

/**
 * Response containing available degree types.
 */
@Serializable
internal data class BicoccappWizardDegreesResponse(
    /**
     * List of degree types.
     */
    @SerialName("degrees")
    val degrees: List<BicoccappWizardDegree> = emptyList()
)

/**
 * Represents a degree type (e.g., Bachelor, Master).
 */
@Serializable
data class BicoccappWizardDegree(
    /**
     * Name of the degree type.
     */
    @SerialName("name")
    val name: String,

    /**
     * Code identifier for the degree type.
     */
    @SerialName("code")
    val code: String,
)

/**
 * Response containing lessons grouped by year.
 */
@Serializable
internal data class BicoccappWizardLessonsResponse(
    /**
     * Map of lessons where key is the year and value is the list of lessons.
     */
    @SerialName("lessons")
    val lessonsByYear: Map<String, List<BicoccappWizardLesson>> = emptyMap()
)

/**
 * Represents a lesson or course in the wizard.
 */
@Serializable
data class BicoccappWizardLesson(
    /**
     * Activity code.
     */
    @SerialName("activity_code")
    val activityCode: String,

    /**
     * Degree course code (CDS).
     */
    @SerialName("cds_code")
    val degreeCourseCode: String,

    /**
     * Name of the lesson.
     */
    @SerialName("lesson_name")
    val lessonName: String,

    /**
     * General course code.
     */
    @SerialName("course_code")
    val courseCode: String,

    /**
     * Partition code (e.g., A-L).
     */
    @SerialName("partition")
    val partition: String,

    /**
     * List of teachers for this lesson.
     */
    @SerialName("teachers")
    @Serializable(with = BicoccappTeachersSerializer::class)
    val teachers: List<BicoccappTeacher> = emptyList()
)

/**
 * Response containing courses
 */
@Serializable
internal data class BicoccappWizardCoursesResponse(
    /**
     * List of courses
     */
    @SerialName("courses")
    val courses: List<BicoccappWizardCourse> = emptyList()
)

/**
 * Represents a wizard course option.
 */
@Serializable
data class BicoccappWizardCourse(
    /**
     * Name of the course.
     */
    @SerialName("name")
    val name: String,

    /**
     * Code of the course.
     */
    @SerialName("code")
    val code: String
)