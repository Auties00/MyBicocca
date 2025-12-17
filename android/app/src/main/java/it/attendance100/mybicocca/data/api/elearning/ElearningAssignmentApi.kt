package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.dto.elearning.*
import retrofit2.*
import retrofit2.http.*

/**
 * # Elearning Assignment API
 *
 * Handles assignments, submissions, and grading.
 *
 * ## Key Features
 *
 * - **Assignments:** List assignments, view details.
 * - **Submissions:** Get submission status, list submissions, save/start/remove submissions.
 * - **Grading:** Get grades, submit for grading.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get assignments
 * val assignments = assignmentApi.getAssignments(
 *     GetAssignmentsRequest(courseIds = listOf(courseId))
 * )
 *
 * // Get submission status
 * val status = assignmentApi.getSubmissionStatus(
 *     GetSubmissionStatusRequest(assignId = assignId)
 * )
 * ```
 */
interface ElearningAssignmentApi {

  /**
   * Returns the courses and assignments for the users.
   *
   * @param request Course IDs and capabilities.
   * @return List of assignments by course.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_get_assignments")
  suspend fun getAssignments(@Body request: GetAssignmentsRequest): Response<GetAssignmentsResponse>


  /**
   * Returns information about an assignment submission status for a given user.
   *
   * @param request Assignment ID and user ID.
   * @return Submission status details.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_get_submission_status")
  suspend fun getSubmissionStatus(@Body request: GetSubmissionStatusRequest): Response<SubmissionStatus>


  /**
   * Retrieve information about assignment submissions for a given instance.
   *
   * @param request Assignment IDs and status.
   * @return List of submissions.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_get_submissions")
  suspend fun getSubmissions(@Body request: GetSubmissionsRequest): Response<GetSubmissionsResponse>


  /**
   * Save a submission for an assignment.
   *
   * @param request Assignment ID and plugin data.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_save_submission")
  suspend fun saveSubmission(@Body request: SaveSubmissionRequest): Response<List<Warning>>


  /**
   * Submit an assignment for grading.
   *
   * @param request Assignment ID and accept statement.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_submit_for_grading")
  suspend fun submitForGrading(@Body request: SubmitForGradingRequest): Response<StatusWithWarningsResponse>


  /**
   * Submit grading form data.
   *
   * @param request Assignment ID, user ID, and grading data.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_submit_grading_form")
  suspend fun submitGradingForm(@Body request: SubmitGradingFormRequest): Response<StatusWithWarningsResponse>


  /**
   * Update the module completion status.
   *
   * @param request Assignment ID.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_view_assign")
  suspend fun viewAssign(@Body request: ViewAssignRequest): Response<StatusWithWarningsResponse>


  /**
   * Log that the submission status has been viewed.
   *
   * @param request Assignment ID.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_view_submission_status")
  suspend fun viewSubmissionStatus(@Body request: ViewSubmissionStatusRequest): Response<StatusWithWarningsResponse>


  /**
   * List participants for an assignment.
   *
   * @param request Assignment ID and filter.
   * @return List of participants.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_list_participants")
  suspend fun listParticipants(@Body request: ListParticipantsRequest): Response<List<Participant>>


  /**
   * Returns grades from the assignment.
   *
   * @param request Assignment IDs.
   * @return List of grades.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_get_grades")
  suspend fun getGrades(@Body request: GetGradesRequest): Response<GetGradesResponse>


  /**
   * Start a submission for an assignment.
   *
   * @param request Assignment ID.
   * @return Submission details.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_start_submission")
  suspend fun startSubmission(@Body request: StartSubmissionRequest): Response<StartSubmissionResponse>


  /**
   * Remove a submission.
   *
   * @param request Assignment ID and user ID.
   * @return Unit.
   */
  @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_assign_remove_submission")
  suspend fun removeSubmission(@Body request: RemoveSubmissionRequest): Response<StatusWithWarningsResponse>
}
