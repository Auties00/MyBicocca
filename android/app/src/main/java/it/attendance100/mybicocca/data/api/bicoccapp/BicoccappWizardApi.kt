package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.remote.dto.bicoccapp.BicoccappWizardCategoriesResponse
import it.attendance100.mybicocca.data.remote.dto.bicoccapp.BicoccappWizardCoursesResponse
import it.attendance100.mybicocca.data.remote.dto.bicoccapp.BicoccappWizardDegreesResponse
import it.attendance100.mybicocca.data.remote.dto.bicoccapp.BicoccappWizardLessonsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * # BicoccApp Wizard API
 *
 * This interface provides endpoints for the course selection wizard, a guided
 * flow that helps students browse and select courses to add to their calendar.
 *
 * ## Wizard Flow
 *
 * The wizard follows a hierarchical selection process:
 *
 * 1. **Categories:** Select an academic area (e.g., Sciences, Economics, Law)
 * 2. **Degree Types:** Choose a degree level (Bachelor's, Master's, PhD)
 * 3. **Degree Programs:** Pick a specific degree program (Corso di Studi)
 * 4. **Lessons:** Browse and select individual courses
 *
 * Each step filters the available options for the next step.
 *
 * ## Academic Structure
 *
 * The University of Milano-Bicocca organizes courses hierarchically:
 *
 * ```
 * Category (Area)
 * └── Degree Type (Laurea Triennale, Magistrale, etc.)
 *     └── Degree Program (Corso di Studi)
 *         └── Lessons (Insegnamenti)
 * ```
 *
 * ## Authentication
 *
 * All endpoints require a valid authentication token.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Step 1: Get available categories
 * val categories = wizardApi.getCategories()
 *
 * // Step 2: Get degree types for selected category
 * val degrees = wizardApi.getDegreeTypes(categoryCode = "SCI")
 *
 * // Step 3: Get degree programs for category + degree type
 * val programs = wizardApi.getDegreePrograms(
 *     categoryCode = "SCI",
 *     degreeCode = "L"
 * )
 *
 * // Step 4: Get lessons for the selected program
 * val lessons = wizardApi.getCourseLessons(
 *     categoryCode = "SCI",
 *     degreeCode = "L",
 *     courseCode = "F0801Q"
 * )
 *
 * // Alternative: Get courses for the current user
 * val userCourses = wizardApi.getUserCourses()
 * ```
 */
interface BicoccappWizardApi {

    /**
     * Retrieves the list of academic area categories.
     *
     * Returns all academic departments/schools organized by category.
     * This is the first step in the wizard flow.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `wizard/categories`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappWizardCategoriesResponse] object contains:
     * - **Category list:** All academic areas with:
     *   - Category code (e.g., "SCI", "ECO", "GIU")
     *   - Category name (e.g., "Scienze", "Economia", "Giurisprudenza")
     *   - Description
     *   - Icon or color for UI display
     *
     * ## Available Categories
     * Common categories at Milano-Bicocca include:
     * - **Scienze:** Mathematics, Physics, Biology, Chemistry, Computer Science
     * - **Economia:** Economics, Business, Statistics
     * - **Giurisprudenza:** Law
     * - **Sociologia:** Sociology, Social Sciences
     * - **Medicina:** Medicine, Healthcare
     * - **Psicologia:** Psychology
     * - **Scienze della Formazione:** Education
     *
     * @return A [Response] containing [BicoccappWizardCategoriesResponse] with all academic areas.
     *
     * @see getDegreeTypes For the next step after selecting a category
     */
    @GET("wizard/categories")
    suspend fun getCategories(): Response<BicoccappWizardCategoriesResponse>

    /**
     * Retrieves available degree types for a specific academic category.
     *
     * Returns the types of degrees offered within the selected category.
     * This is the second step in the wizard flow.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `wizard/degrees`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappWizardDegreesResponse] object contains:
     * - **Degree type list:** Available degree levels with:
     *   - Degree code (e.g., "L", "LM", "PHD")
     *   - Degree name (e.g., "Laurea Triennale", "Laurea Magistrale")
     *   - Duration (years)
     *   - Credits required (CFU)
     *
     * ## Italian Degree Types
     * - **L (Laurea Triennale):** 3-year Bachelor's degree (180 CFU)
     * - **LM (Laurea Magistrale):** 2-year Master's degree (120 CFU)
     * - **LMCU (Laurea Magistrale a Ciclo Unico):** 5-6 year combined degree
     * - **PHD (Dottorato):** Doctoral program
     * - **Master:** Professional Master's programs
     *
     * @param categoryCode The category code from the previous step.
     *                     Example: "SCI" for Sciences, "ECO" for Economics.
     *                     Required to filter degree types to those offered
     *                     in the selected academic area.
     *
     * @return A [Response] containing [BicoccappWizardDegreesResponse] with available degree types.
     *
     * @see getCategories For obtaining category codes
     * @see getDegreePrograms For the next step after selecting a degree type
     */
    @GET("wizard/degrees")
    suspend fun getDegreeTypes(
        @Query("category_code") categoryCode: String? = null
    ): Response<BicoccappWizardDegreesResponse>

    /**
     * Lists degree programs available for a category and degree type combination.
     *
     * Returns all Corsi di Studio (degree programs) matching the selected
     * category and degree level. This is the third step in the wizard flow.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `wizard/courses`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappWizardCoursesResponse] object contains:
     * - **Program list:** Available degree programs with:
     *   - Course code (CdS code, e.g., "F0801Q")
     *   - Program name (e.g., "Informatica")
     *   - Department
     *   - Duration and total credits
     *   - Active/inactive status
     *   - Academic year
     *
     * ## Program Codes
     * Each Corso di Studio has a unique code that identifies it across
     * university systems. This code is used to fetch lessons and enroll.
     *
     * @param categoryCode The category code from step 1.
     *                     Filters programs to the selected academic area.
     *
     * @param degreeCode The degree type code from step 2.
     *                   Filters programs to the selected degree level
     *                   (e.g., "L" for Bachelor's, "LM" for Master's).
     *
     * @return A [Response] containing [BicoccappWizardCoursesResponse] with degree programs.
     *
     * @see getDegreeTypes For obtaining degree codes
     * @see getCourseLessons For the next step after selecting a program
     */
    @GET("wizard/courses")
    suspend fun getDegreePrograms(
        @Query("category_code") categoryCode: String? = null,
        @Query("degree_code") degreeCode: String? = null
    ): Response<BicoccappWizardCoursesResponse>

    /**
     * Retrieves the list of lessons for a specific degree program.
     *
     * Returns all courses (insegnamenti) offered in the selected degree
     * program. This is the final step in the wizard flow, where students
     * can select individual courses to add to their calendar.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `wizard/lessons`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappWizardLessonsResponse] object contains:
     * - **Lesson list:** All courses in the program with:
     *   - Activity code (unique lesson identifier)
     *   - Course name
     *   - Credits (CFU)
     *   - Year of study (1st, 2nd, 3rd year)
     *   - Semester
     *   - Professor name
     *   - Whether it's mandatory or elective
     *   - Partitions/sections (if applicable)
     *
     * ## Lesson Organization
     * Courses are typically organized by:
     * - **Year:** Which year of the program they belong to
     * - **Semester:** First or second semester
     * - **Type:** Mandatory (obbligatorio) or elective (a scelta)
     *
     * @param categoryCode The category code from step 1.
     *
     * @param degreeCode The degree type code from step 2.
     *
     * @param courseCode The degree program code from step 3.
     *                   Identifies which Corso di Studio to fetch lessons for.
     *
     * @return A [Response] containing [BicoccappWizardLessonsResponse] with course listings.
     *
     * @see getDegreePrograms For obtaining course codes
     * @see BicoccappCalendarApi.addCourseToCalendar For adding lessons to calendar
     */
    @GET("wizard/lessons")
    suspend fun getCourseLessons(
        @Query("category_code") categoryCode: String? = null,
        @Query("degree_code") degreeCode: String? = null,
        @Query("course_code") courseCode: String? = null
    ): Response<BicoccappWizardLessonsResponse>

    /**
     * Retrieves courses associated with the authenticated user's degree program.
     *
     * Returns the courses from the student's enrolled degree program,
     * providing a shortcut to the full wizard flow for students who want
     * to add courses from their own curriculum.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `wizard/user_courses`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappWizardCoursesResponse] object contains:
     * - **Course list:** Courses from the user's program with:
     *   - Activity code
     *   - Course name
     *   - Credits (CFU)
     *   - Year and semester
     *   - Whether already added to calendar
     *   - Course status (upcoming, in progress, completed)
     * - **Program info:** User's enrolled degree program details
     *
     * ## Use Cases
     * This endpoint is useful for:
     * - Quick calendar setup for new students
     * - Adding courses without navigating the full wizard
     * - Displaying "recommended" courses on the home screen
     *
     * ## Comparison to Wizard Flow
     * Instead of navigating Category → Degree → Program → Lessons,
     * this endpoint directly returns lessons for the user's program.
     *
     * @param matricId Optional enrollment number.
     *                 When omitted, uses the authenticated user's enrollment.
     *                 Useful for administrative access to other students' data.
     *
     * @return A [Response] containing [BicoccappWizardCoursesResponse] with the user's courses.
     *
     * @see getCourseLessons For browsing courses from any program
     */
    @GET("wizard/user_courses")
    suspend fun getUserCourses(
        @Query("matricId") matricId: Int? = null
    ): Response<BicoccappWizardCoursesResponse>
}
