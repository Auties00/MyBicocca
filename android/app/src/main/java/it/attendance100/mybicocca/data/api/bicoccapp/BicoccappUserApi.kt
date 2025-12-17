package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.*
import it.attendance100.mybicocca.data.dto.elearning.*
import retrofit2.*
import retrofit2.http.*

/**
 * # BicoccApp User API
 *
 * This interface provides endpoints for accessing student academic data,
 * including profile information, career progress, exams, fees, and registrations.
 *
 * ## Features
 *
 * - **Profile:** Personal and academic information
 * - **Career:** Degree progress, enrolled courses, and academic standing
 * - **Exams:** Completed exams with grades and statistics
 * - **Appeals:** Available and registered exam sessions
 * - **Fees:** Tuition payments and outstanding balances
 * - **Registrations:** Course enrollment history
 *
 * ## Authentication
 *
 * All endpoints require a valid authentication token. The server uses the
 * authenticated session to determine which student's data to return when
 * optional identifier parameters are omitted.
 *
 * ## Identifier Parameters
 *
 * Several endpoints accept optional identifier parameters:
 * - **matricId:** Student enrollment number (matricola)
 * - **personId:** Internal person identifier
 * - **studentId:** Student record identifier
 *
 * When omitted, the server returns data for the authenticated user.
 * These parameters are primarily used for administrative access.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Fetch authenticated user's profile
 * val profile = userApi.getProfile()
 *
 * // Fetch career data for a specific student
 * val career = userApi.getCareer(matricId = 123456)
 *
 * // Fetch exam history
 * val exams = userApi.getExams()
 * ```
 */
interface BicoccappUserApi {

    /**
     * Retrieves the authenticated user's profile information.
     *
     * Returns personal and academic profile data including name, contact
     * information, enrolled degree program, and academic identifiers.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_profile`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappUserProfile] object contains:
     * - **Personal data:** Name, date of birth, tax code (codice fiscale)
     * - **Contact info:** Email, phone number, address
     * - **Academic info:** Enrolled degree program, department, academic year
     * - **Identifiers:** Matricola, person ID, student ID
     * - **Careers:** List of all academic careers (current and past)
     *
     * @param fiscalCode Optional tax code (codice fiscale) to look up a specific user.
     *                   When omitted, returns the authenticated user's profile.
     *                   Format: 16 alphanumeric characters (e.g., "RSSMRA80A01F205X").
     *
     * @return A [Response] containing [BicoccappUserProfile] with the user's complete profile.
     */
    @GET("user_profile")
    suspend fun getProfile(
        @Query("fiscalCode") fiscalCode: String? = null
    ): Response<BicoccappUserProfile>

    /**
     * Retrieves detailed information about the student's academic career.
     *
     * Returns comprehensive data about the student's progress in their
     * degree program, including enrolled courses, credits earned, GPA,
     * and expected graduation timeline.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_career`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappUserCareerResponse] object contains:
     * - **Degree info:** Program name, department, curriculum
     * - **Progress:** Total credits earned, remaining credits, completion percentage
     * - **Academic standing:** GPA (media), weighted average, graduation forecast
     * - **Enrollment:** Academic year, enrollment date, expected graduation
     * - **Study plan:** List of courses in the curriculum with status
     *
     * ## Multiple Careers
     * Students may have multiple careers (e.g., bachelor's + master's).
     * Use `typeTitleCode` to filter by degree type.
     *
     * @param matricId Optional enrollment number to fetch a specific student's career.
     *                 When omitted, uses the authenticated user's default career.
     *
     * @param personId Optional internal person identifier.
     *                 Alternative to matricId for user identification.
     *
     * @param studentId Optional student record identifier.
     *                  Useful when a person has multiple student records.
     *
     * @param typeTitleCode Optional degree type filter code.
     *                      Use to select a specific career when the student
     *                      has multiple active enrollments.
     *
     * @return A [Response] containing [BicoccappUserCareerResponse] with academic career details.
     */
    @GET("user_career")
    suspend fun getCareer(
        @Query("matricId") matricId: Int? = null,
        @Query("personId") personId: Int? = null,
        @Query("studentId") studentId: Int? = null,
        @Query("typeTitleCode") typeTitleCode: String? = null
    ): Response<BicoccappUserCareerResponse>

    /**
     * Retrieves the student's completed exam history.
     *
     * Returns a list of all exams the student has taken, including
     * grades, dates, credits, and course information.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_exams`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappUserExamsResponse] object contains:
     * - **Exam list:** All completed exams with:
     *   - Course name and code
     *   - Grade (voto) on the Italian 18-30 scale
     *   - Date of examination
     *   - Credits (CFU) earned
     *   - Whether the exam counts toward the final grade
     * - **Statistics:** Average grade, total credits, exam count
     * - **Career reference:** Which degree program the exams belong to
     *
     * ## Grade Scale
     * Italian university grades range from 18 (minimum pass) to 30 (maximum),
     * with optional "lode" (30L) indicating honors.
     *
     * @param matricId Optional enrollment number to fetch a specific student's exams.
     *                 When omitted, returns exams for the authenticated user.
     *
     * @return A [Response] containing [BicoccappUserExamsResponse] with the complete exam history.
     *
     * @see getExamsSessions For upcoming exam sessions the student can register for
     */
    @GET("user_exams")
    suspend fun getExams(
        @Query("matricId") matricId: Int? = null
    ): Response<BicoccappUserExamsResponse>

    /**
     * Retrieves available and registered exam sessions.
     *
     * Returns information about exam sessions the student can register for
     * or has already registered for, including dates, locations, and
     * registration deadlines.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_appeals`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappExamsSessionsResponse] object contains:
     * - **Available sessions:** Exams open for registration with:
     *   - Course name and code
     *   - Exam date and time
     *   - Location (building, room)
     *   - Registration deadline
     *   - Available spots
     * - **Registered sessions:** Exams the student has signed up for
     * - **Past sessions:** Recently concluded exam sessions
     *
     * ## Terminology
     * In the Italian university system, "appello" refers to an exam session
     * or examination date. Students must register for an appello before
     * they can take the exam.
     *
     * @param matricId Optional enrollment number to fetch exam sessions
     *                 for a specific student.
     *
     * @param personId Optional internal person identifier.
     *                 Alternative to matricId for user identification.
     *
     * @return A [Response] containing [BicoccappExamsSessionsResponse] with exam session information.
     *
     * @see getExams For completed exam results
     */
    @GET("user_appeals")
    suspend fun getExamsSessions(
        @Query("matricId") matricId: Int? = null,
        @Query("personId") personId: Int? = null
    ): Response<BicoccappExamsSessionsResponse>

    /**
     * Cancels a student's registration for an exam session.
     *
     * Removes the student's enrollment from a previously registered exam
     * session (appello). This action is typically only allowed before the
     * registration deadline has passed.
     *
     * ## HTTP Details
     * - **Method:** DELETE
     * - **Path:** `user_appeals`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappModificationResponse] object contains:
     * - **Success status:** Whether the cancellation was processed
     * - **Message:** Confirmation or error description
     * - **Timestamp:** When the modification was recorded
     *
     * ## Required Parameters
     * All identifier parameters are needed to uniquely identify the exam
     * session registration to cancel. These values can be obtained from
     * the [getExamsSessions] response.
     *
     * @param cdsId The degree program identifier (Corso di Studi ID).
     *              Identifies which degree program the exam belongs to.
     *
     * @param activityId The teaching activity identifier.
     *                   Represents the course or module being examined.
     *
     * @param activityItemId The specific activity item identifier.
     *                       Used when a course has multiple exam components.
     *
     * @param activityAppealId The exam session identifier.
     *                         Identifies the specific date/time slot of the exam.
     *
     * @param studentId The student record identifier.
     *                  Identifies which student's registration to cancel.
     *
     * @return A [Response] containing [BicoccappModificationResponse] with the operation result.
     *
     * @see getExamsSessions To retrieve registered exam sessions and their identifiers
     * @see addExamSession To register for an exam session
     */
    @DELETE("user_appeals")
    suspend fun cancelExamSession(
        @Query("cdsId") cdsId: Int? = null,
        @Query("activityId") activityId: Int? = null,
        @Query("activityItemId") activityItemId: Int? = null,
        @Query("activityAppealId") activityAppealId: Int? = null,
        @Query("studentId") studentId: Int? = null
    ): Response<BicoccappModificationResponse>

    /**
     * Registers the authenticated student for an exam session.
     *
     * Enrolls the student in a specific exam session (appello). Registration
     * is required before taking any exam and must be done before the
     * session's registration deadline.
     *
     * ## HTTP Details
     * - **Method:** POST
     * - **Path:** `user_appeals`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappModificationResponse] object contains:
     * - **Success status:** Whether the registration was processed
     * - **Message:** Confirmation or error description
     * - **Timestamp:** When the modification was recorded
     *
     * ## Required Parameters
     * All identifier parameters are needed to uniquely identify the exam
     * session to register for. These values can be obtained from the
     * [getExamsSessions] response under available sessions.
     *
     * ## Prerequisites
     * - The exam session must be open for registration
     * - The registration deadline must not have passed
     * - The student must be eligible to take the exam (e.g., prerequisites met)
     * - Available spots must remain in the session
     *
     * @param cdsId The degree program identifier (Corso di Studi ID).
     *              Identifies which degree program the exam belongs to.
     *
     * @param activityId The teaching activity identifier.
     *                   Represents the course or module being examined.
     *
     * @param activityItemId The specific activity item identifier.
     *                       Used when a course has multiple exam components.
     *
     * @param activityAppealId The exam session identifier.
     *                         Identifies the specific date/time slot to register for.
     *
     * @return A [Response] containing [BicoccappModificationResponse] with the operation result.
     *
     * @see getExamsSessions To retrieve available exam sessions and their identifiers
     * @see cancelExamSession To cancel an existing registration
     */
    @POST("user_appeals")
    suspend fun addExamSession(
        @Query("cdsId") cdsId: Int? = null,
        @Query("activityId") activityId: Int? = null,
        @Query("activityItemId") activityItemId: Int? = null,
        @Query("activityAppealId") activityAppealId: Int? = null
    ): Response<BicoccappModificationResponse>

    /**
     * Retrieves the student's tuition fee payment history.
     *
     * Returns information about tuition fees, including paid installments,
     * pending payments, deadlines, and scholarship deductions.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_fees`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappTaxesResponse] object contains:
     * - **Fee summary:** Total amount due, amount paid, outstanding balance
     * - **Installments:** List of payment installments with:
     *   - Due date
     *   - Amount
     *   - Payment status (paid, pending, overdue)
     *   - Payment date (if paid)
     *   - MAV/PagoPA payment code
     * - **Scholarships:** Applied fee reductions (DSU, merit-based)
     * - **Academic year:** Which year the fees refer to
     *
     * ## Payment Status
     * Fee statuses include:
     * - **Paid:** Payment received and processed
     * - **Pending:** Payment due but not yet received
     * - **Overdue:** Payment deadline has passed
     * - **Exempt:** Student is exempt (scholarship, disability, etc.)
     *
     * @param personId Optional internal person identifier to fetch fees
     *                 for a specific user.
     *
     * @param matricId Optional enrollment number.
     *                 Alternative identifier for the student.
     *
     * @return A [Response] containing [BicoccappTaxesResponse] with payment information.
     */
    @GET("user_fees")
    suspend fun getTaxes(
        @Query("personId") personId: Int? = null,
        @Query("matricId") matricId: Int? = null
    ): Response<BicoccappTaxesResponse>

    /**
     * Retrieves the student's course registration history.
     *
     * Returns information about the student's enrollment status
     * across academic years, including registration dates, enrollment
     * type, and academic year details.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `user_registrations`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [BicoccappUserRegistrationsResponse] object contains:
     * - **Registration list:** All academic year registrations with:
     *   - Academic year (e.g., "2024/2025")
     *   - Registration date
     *   - Enrollment type (first enrollment, renewal, transfer)
     *   - Status (active, suspended, graduated)
     * - **Current registration:** Details of the active enrollment
     * - **Career info:** Associated degree program
     *
     * ## Enrollment Types
     * - **Prima immatricolazione:** First-time enrollment
     * - **Rinnovo:** Annual renewal
     * - **Trasferimento:** Transfer from another university
     * - **Passaggio:** Program change within the university
     *
     * @param matricId Optional enrollment number to fetch registrations
     *                 for a specific student.
     *
     * @return A [Response] containing [BicoccappUserRegistrationsResponse] with enrollment history.
     */
    @GET("user_registrations")
    suspend fun getRegistrations(
        @Query("matricId") matricId: Int? = null
    ): Response<BicoccappUserRegistrationsResponse>
}
