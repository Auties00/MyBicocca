package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignCopyPreviousAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetAssignments200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetAssignmentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetGrades200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetGradesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetParticipant200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetParticipantRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetSubmissionStatus200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetSubmissionStatusRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetSubmissions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetSubmissionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetUserFlags200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetUserFlagsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignGetUserMappings200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignListParticipantsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignLockSubmissionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignRevealIdentitiesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignRevertSubmissionsToDraftRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSaveGradeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSaveGradesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSaveSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSaveUserExtensionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSetUserFlagsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignStartSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignStartSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSubmitForGradingRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignSubmitGradingFormRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModAssignViewAssignRequest

interface ModAssignApi {
    /**
     * POST mod_assign_copy_previous_attempt
     * Copy a students previous attempt to a new attempt.
     * Copy a students previous attempt to a new attempt.
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignCopyPreviousAttemptRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_copy_previous_attempt")
    fun modAssignCopyPreviousAttempt(@Body elearningModAssignCopyPreviousAttemptRequest: ElearningModAssignCopyPreviousAttemptRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_get_assignments
     * Returns the courses and assignments for the users capability
     * Returns the courses and assignments for the users capability
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetAssignmentsRequest 
     * @return [Call]<[ElearningModAssignGetAssignments200Response]>
     */
    @POST("mod_assign_get_assignments")
    fun modAssignGetAssignments(@Body elearningModAssignGetAssignmentsRequest: ElearningModAssignGetAssignmentsRequest): Call<ElearningModAssignGetAssignments200Response>

    /**
     * POST mod_assign_get_grades
     * Returns grades from the assignment
     * Returns grades from the assignment
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetGradesRequest 
     * @return [Call]<[ElearningModAssignGetGrades200Response]>
     */
    @POST("mod_assign_get_grades")
    fun modAssignGetGrades(@Body elearningModAssignGetGradesRequest: ElearningModAssignGetGradesRequest): Call<ElearningModAssignGetGrades200Response>

    /**
     * POST mod_assign_get_participant
     * Get a participant for an assignment, with some summary info about their submissions.
     * Get a participant for an assignment, with some summary info about their submissions.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetParticipantRequest 
     * @return [Call]<[ElearningModAssignGetParticipant200Response]>
     */
    @POST("mod_assign_get_participant")
    fun modAssignGetParticipant(@Body elearningModAssignGetParticipantRequest: ElearningModAssignGetParticipantRequest): Call<ElearningModAssignGetParticipant200Response>

    /**
     * POST mod_assign_get_submission_status
     * Returns information about an assignment submission status for a given user.
     * Returns information about an assignment submission status for a given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetSubmissionStatusRequest 
     * @return [Call]<[ElearningModAssignGetSubmissionStatus200Response]>
     */
    @POST("mod_assign_get_submission_status")
    fun modAssignGetSubmissionStatus(@Body elearningModAssignGetSubmissionStatusRequest: ElearningModAssignGetSubmissionStatusRequest): Call<ElearningModAssignGetSubmissionStatus200Response>

    /**
     * POST mod_assign_get_submissions
     * Returns the submissions for assignments
     * Returns the submissions for assignments
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetSubmissionsRequest 
     * @return [Call]<[ElearningModAssignGetSubmissions200Response]>
     */
    @POST("mod_assign_get_submissions")
    fun modAssignGetSubmissions(@Body elearningModAssignGetSubmissionsRequest: ElearningModAssignGetSubmissionsRequest): Call<ElearningModAssignGetSubmissions200Response>

    /**
     * POST mod_assign_get_user_flags
     * Returns the user flags for assignments
     * Returns the user flags for assignments
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetUserFlagsRequest 
     * @return [Call]<[ElearningModAssignGetUserFlags200Response]>
     */
    @POST("mod_assign_get_user_flags")
    fun modAssignGetUserFlags(@Body elearningModAssignGetUserFlagsRequest: ElearningModAssignGetUserFlagsRequest): Call<ElearningModAssignGetUserFlags200Response>

    /**
     * POST mod_assign_get_user_mappings
     * Returns the blind marking mappings for assignments
     * Returns the blind marking mappings for assignments
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignGetUserFlagsRequest 
     * @return [Call]<[ElearningModAssignGetUserMappings200Response]>
     */
    @POST("mod_assign_get_user_mappings")
    fun modAssignGetUserMappings(@Body elearningModAssignGetUserFlagsRequest: ElearningModAssignGetUserFlagsRequest): Call<ElearningModAssignGetUserMappings200Response>

    /**
     * POST mod_assign_list_participants
     * List the participants for a single assignment, with some summary info about their submissions.
     * List the participants for a single assignment, with some summary info about their submissions.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignListParticipantsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_list_participants")
    fun modAssignListParticipants(@Body elearningModAssignListParticipantsRequest: ElearningModAssignListParticipantsRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_lock_submissions
     * Prevent students from making changes to a list of submissions
     * Prevent students from making changes to a list of submissions
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignLockSubmissionsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_lock_submissions")
    fun modAssignLockSubmissions(@Body elearningModAssignLockSubmissionsRequest: ElearningModAssignLockSubmissionsRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_reveal_identities
     * Reveal the identities for a blind marking assignment
     * Reveal the identities for a blind marking assignment
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignRevealIdentitiesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_reveal_identities")
    fun modAssignRevealIdentities(@Body elearningModAssignRevealIdentitiesRequest: ElearningModAssignRevealIdentitiesRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_revert_submissions_to_draft
     * Reverts the list of submissions to draft status
     * Reverts the list of submissions to draft status
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignRevertSubmissionsToDraftRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_revert_submissions_to_draft")
    fun modAssignRevertSubmissionsToDraft(@Body elearningModAssignRevertSubmissionsToDraftRequest: ElearningModAssignRevertSubmissionsToDraftRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_save_grade
     * Save a grade update for a single student.
     * Save a grade update for a single student.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSaveGradeRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_save_grade")
    fun modAssignSaveGrade(@Body elearningModAssignSaveGradeRequest: ElearningModAssignSaveGradeRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_save_grades
     * Save multiple grade updates for an assignment.
     * Save multiple grade updates for an assignment.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSaveGradesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_save_grades")
    fun modAssignSaveGrades(@Body elearningModAssignSaveGradesRequest: ElearningModAssignSaveGradesRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_save_submission
     * Update the current students submission
     * Update the current students submission
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSaveSubmissionRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_save_submission")
    fun modAssignSaveSubmission(@Body elearningModAssignSaveSubmissionRequest: ElearningModAssignSaveSubmissionRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_save_user_extensions
     * Save a list of assignment extensions
     * Save a list of assignment extensions
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSaveUserExtensionsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_save_user_extensions")
    fun modAssignSaveUserExtensions(@Body elearningModAssignSaveUserExtensionsRequest: ElearningModAssignSaveUserExtensionsRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_set_user_flags
     * Creates or updates user flags
     * Creates or updates user flags
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSetUserFlagsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_set_user_flags")
    fun modAssignSetUserFlags(@Body elearningModAssignSetUserFlagsRequest: ElearningModAssignSetUserFlagsRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_start_submission
     * Start a submission for user if assignment has a time limit.
     * Start a submission for user if assignment has a time limit.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignStartSubmissionRequest 
     * @return [Call]<[ElearningModAssignStartSubmission200Response]>
     */
    @POST("mod_assign_start_submission")
    fun modAssignStartSubmission(@Body elearningModAssignStartSubmissionRequest: ElearningModAssignStartSubmissionRequest): Call<ElearningModAssignStartSubmission200Response>

    /**
     * POST mod_assign_submit_for_grading
     * Submit the current students assignment for grading
     * Submit the current students assignment for grading
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSubmitForGradingRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_submit_for_grading")
    fun modAssignSubmitForGrading(@Body elearningModAssignSubmitForGradingRequest: ElearningModAssignSubmitForGradingRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_submit_grading_form
     * Submit the grading form data via ajax
     * Submit the grading form data via ajax
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignSubmitGradingFormRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_submit_grading_form")
    fun modAssignSubmitGradingForm(@Body elearningModAssignSubmitGradingFormRequest: ElearningModAssignSubmitGradingFormRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_unlock_submissions
     * Allow students to make changes to a list of submissions
     * Allow students to make changes to a list of submissions
     * Responses:
     *  - 200: list of warnings
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignRevertSubmissionsToDraftRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_assign_unlock_submissions")
    fun modAssignUnlockSubmissions(@Body elearningModAssignRevertSubmissionsToDraftRequest: ElearningModAssignRevertSubmissionsToDraftRequest): Call<kotlin.Any>

    /**
     * POST mod_assign_view_assign
     * Update the module completion status.
     * Update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignViewAssignRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_assign_view_assign")
    fun modAssignViewAssign(@Body elearningModAssignViewAssignRequest: ElearningModAssignViewAssignRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_assign_view_grading_table
     * Trigger the grading_table_viewed event.
     * Trigger the grading_table_viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignViewAssignRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_assign_view_grading_table")
    fun modAssignViewGradingTable(@Body elearningModAssignViewAssignRequest: ElearningModAssignViewAssignRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_assign_view_submission_status
     * Trigger the submission status viewed event.
     * Trigger the submission status viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModAssignViewAssignRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_assign_view_submission_status")
    fun modAssignViewSubmissionStatus(@Body elearningModAssignViewAssignRequest: ElearningModAssignViewAssignRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
