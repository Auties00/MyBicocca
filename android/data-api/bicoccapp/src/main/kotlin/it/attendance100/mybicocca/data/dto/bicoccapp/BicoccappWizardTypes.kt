package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing available wizard categories.
 */
@Serializable
data class BicoccappWizardCategoriesResponse(
    /**
     * List of categories.
     */
    @SerialName("categories")
    val categories: List<BicoccappWizardCategory> = emptyList()
)

/**
 * Represents a category in the wizard (e.g., Department or Faculty).
 */
@Serializable
data class BicoccappWizardCategory(
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
data class BicoccappWizardDegreesResponse(
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
data class BicoccappWizardLessonsResponse(
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
    val teachers: List<BicoccappWizardTeacher> = emptyList()
)

/**
 * Represents a teacher in the wizard context.
 */
@Serializable
data class BicoccappWizardTeacher(
    /**
     * Surname of the teacher.
     */
    @SerialName("teacher_surname")
    val surname: String,

    /**
     * Email of the teacher.
     */
    @SerialName("teacher_email")
    val email: String? = null,

    /**
     * Code of the teacher.
     */
    @SerialName("teacher_code")
    val code: String,

    /**
     * Name of the teacher.
     */
    @SerialName("teacher_name")
    val name: String,

    /**
     * ID of the teacher.
     */
    @SerialName("teacher_id")
    val id: Int
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