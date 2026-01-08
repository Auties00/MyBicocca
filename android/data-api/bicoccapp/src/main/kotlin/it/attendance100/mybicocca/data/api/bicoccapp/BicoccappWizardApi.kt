package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappWizardCategoriesResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappWizardDegreesResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappWizardLessonsResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

/**
 * Wizard API for hierarchical course selection: Category -> Degree Type -> Program -> Lessons.
 */
interface BicoccappWizardApi {
    /**
     * Retrieves all academic area categories (step 1).
     *
     * @return Categories with codes and names (e.g., "SCI" for Sciences).
     */
    @GET("wizard/categories")
    suspend fun getCategories(): BicoccappWizardCategoriesResponse

    /**
     * Retrieves degree types for a category (step 2).
     *
     * @param categoryCode Category code from step 1.
     * @return Degree types (L=Bachelor's, LM=Master's, etc.).
     */
    @GET("wizard/degrees")
    suspend fun getDegreeTypes(
        @Query("category_code") categoryCode: String
    ): BicoccappWizardDegreesResponse

    /**
     * Lists degree programs for a category and degree type (step 3).
     *
     * @param categoryCode Category code from step 1.
     * @param degreeCode Degree type code from step 2.
     * @return Available degree programs (Corsi di Studio).
     */
    @GET("wizard/courses")
    suspend fun getDegreePrograms(
        @Query("category_code") categoryCode: String,
        @Query("degree_code") degreeCode: String
    ): BicoccappWizardLessonsResponse

    /**
     * Retrieves lessons for a specific degree program (step 4).
     *
     * @param categoryCode Category code from step 1.
     * @param degreeCode Degree type code from step 2.
     * @param courseCode Degree program code from step 3.
     * @return Course list with credits, year, semester, and professor.
     */
    @GET("wizard/lessons")
    suspend fun getCourseLessons(
        @Query("category_code") categoryCode: String,
        @Query("degree_code") degreeCode: String,
        @Query("course_code") courseCode: String
    ): BicoccappWizardLessonsResponse

    /**
     * Retrieves courses from the user's enrolled degree program (shortcut to step 4).
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return User's courses with calendar subscription status.
     */
    @GET("wizard/user_courses")
    suspend fun getUserCourses(
        @Query("matricId") enrollmentId: String
    ): BicoccappWizardLessonsResponse
}
