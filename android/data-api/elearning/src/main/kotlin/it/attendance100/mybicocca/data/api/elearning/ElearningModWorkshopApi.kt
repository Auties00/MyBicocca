package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopAddSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopAddSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopDeleteSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopDeleteSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopEvaluateAssessment200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopEvaluateAssessmentRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopEvaluateSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopEvaluateSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetAssessment200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetAssessmentFormDefinition200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetAssessmentFormDefinitionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetAssessmentRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetGrades200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetGradesReport200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetGradesReportRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetGradesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetReviewerAssessments200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetReviewerAssessmentsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetSubmissions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetSubmissionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetUserPlan200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetUserPlanRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetWorkshopAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetWorkshopAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopGetWorkshopsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopUpdateAssessment200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopUpdateAssessmentRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopUpdateSubmission200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopUpdateSubmissionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModWorkshopViewWorkshopRequest

interface ModWorkshopApi {
    /**
     * POST mod_workshop_add_submission
     * Add a new submission to a given workshop.
     * Add a new submission to a given workshop.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopAddSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopAddSubmission200Response]>
     */
    @POST("mod_workshop_add_submission")
    fun modWorkshopAddSubmission(@Body elearningModWorkshopAddSubmissionRequest: ElearningModWorkshopAddSubmissionRequest): Call<ElearningModWorkshopAddSubmission200Response>

    /**
     * POST mod_workshop_delete_submission
     * Deletes the given submission.
     * Deletes the given submission.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopDeleteSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopDeleteSubmission200Response]>
     */
    @POST("mod_workshop_delete_submission")
    fun modWorkshopDeleteSubmission(@Body elearningModWorkshopDeleteSubmissionRequest: ElearningModWorkshopDeleteSubmissionRequest): Call<ElearningModWorkshopDeleteSubmission200Response>

    /**
     * POST mod_workshop_evaluate_assessment
     * Evaluates an assessment (used by teachers for provide feedback to the reviewer).
     * Evaluates an assessment (used by teachers for provide feedback to the reviewer).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopEvaluateAssessmentRequest 
     * @return [Call]<[ElearningModWorkshopEvaluateAssessment200Response]>
     */
    @POST("mod_workshop_evaluate_assessment")
    fun modWorkshopEvaluateAssessment(@Body elearningModWorkshopEvaluateAssessmentRequest: ElearningModWorkshopEvaluateAssessmentRequest): Call<ElearningModWorkshopEvaluateAssessment200Response>

    /**
     * POST mod_workshop_evaluate_submission
     * Evaluates a submission (used by teachers for provide feedback or override the submission grade).
     * Evaluates a submission (used by teachers for provide feedback or override the submission grade).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopEvaluateSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopEvaluateSubmission200Response]>
     */
    @POST("mod_workshop_evaluate_submission")
    fun modWorkshopEvaluateSubmission(@Body elearningModWorkshopEvaluateSubmissionRequest: ElearningModWorkshopEvaluateSubmissionRequest): Call<ElearningModWorkshopEvaluateSubmission200Response>

    /**
     * POST mod_workshop_get_assessment
     * Retrieves the given assessment.
     * Retrieves the given assessment.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetAssessmentRequest 
     * @return [Call]<[ElearningModWorkshopGetAssessment200Response]>
     */
    @POST("mod_workshop_get_assessment")
    fun modWorkshopGetAssessment(@Body elearningModWorkshopGetAssessmentRequest: ElearningModWorkshopGetAssessmentRequest): Call<ElearningModWorkshopGetAssessment200Response>

    /**
     * POST mod_workshop_get_assessment_form_definition
     * Retrieves the assessment form definition.
     * Retrieves the assessment form definition.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetAssessmentFormDefinitionRequest 
     * @return [Call]<[ElearningModWorkshopGetAssessmentFormDefinition200Response]>
     */
    @POST("mod_workshop_get_assessment_form_definition")
    fun modWorkshopGetAssessmentFormDefinition(@Body elearningModWorkshopGetAssessmentFormDefinitionRequest: ElearningModWorkshopGetAssessmentFormDefinitionRequest): Call<ElearningModWorkshopGetAssessmentFormDefinition200Response>

    /**
     * POST mod_workshop_get_grades
     * Returns the assessment and submission grade for the given user.
     * Returns the assessment and submission grade for the given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetGradesRequest 
     * @return [Call]<[ElearningModWorkshopGetGrades200Response]>
     */
    @POST("mod_workshop_get_grades")
    fun modWorkshopGetGrades(@Body elearningModWorkshopGetGradesRequest: ElearningModWorkshopGetGradesRequest): Call<ElearningModWorkshopGetGrades200Response>

    /**
     * POST mod_workshop_get_grades_report
     * Retrieves the assessment grades report.
     * Retrieves the assessment grades report.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetGradesReportRequest 
     * @return [Call]<[ElearningModWorkshopGetGradesReport200Response]>
     */
    @POST("mod_workshop_get_grades_report")
    fun modWorkshopGetGradesReport(@Body elearningModWorkshopGetGradesReportRequest: ElearningModWorkshopGetGradesReportRequest): Call<ElearningModWorkshopGetGradesReport200Response>

    /**
     * POST mod_workshop_get_reviewer_assessments
     * Retrieves all the assessments reviewed by the given user.
     * Retrieves all the assessments reviewed by the given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetReviewerAssessmentsRequest 
     * @return [Call]<[ElearningModWorkshopGetReviewerAssessments200Response]>
     */
    @POST("mod_workshop_get_reviewer_assessments")
    fun modWorkshopGetReviewerAssessments(@Body elearningModWorkshopGetReviewerAssessmentsRequest: ElearningModWorkshopGetReviewerAssessmentsRequest): Call<ElearningModWorkshopGetReviewerAssessments200Response>

    /**
     * POST mod_workshop_get_submission
     * Retrieves the given submission.
     * Retrieves the given submission.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopGetSubmission200Response]>
     */
    @POST("mod_workshop_get_submission")
    fun modWorkshopGetSubmission(@Body elearningModWorkshopGetSubmissionRequest: ElearningModWorkshopGetSubmissionRequest): Call<ElearningModWorkshopGetSubmission200Response>

    /**
     * POST mod_workshop_get_submission_assessments
     * Retrieves all the assessments of the given submission.
     * Retrieves all the assessments of the given submission.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopGetReviewerAssessments200Response]>
     */
    @POST("mod_workshop_get_submission_assessments")
    fun modWorkshopGetSubmissionAssessments(@Body elearningModWorkshopGetSubmissionRequest: ElearningModWorkshopGetSubmissionRequest): Call<ElearningModWorkshopGetReviewerAssessments200Response>

    /**
     * POST mod_workshop_get_submissions
     * Retrieves all the workshop submissions or the one done by the given user (except example submissions).
     * Retrieves all the workshop submissions or the one done by the given user (except example submissions).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetSubmissionsRequest 
     * @return [Call]<[ElearningModWorkshopGetSubmissions200Response]>
     */
    @POST("mod_workshop_get_submissions")
    fun modWorkshopGetSubmissions(@Body elearningModWorkshopGetSubmissionsRequest: ElearningModWorkshopGetSubmissionsRequest): Call<ElearningModWorkshopGetSubmissions200Response>

    /**
     * POST mod_workshop_get_user_plan
     * Return the planner information for the given user.
     * Return the planner information for the given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetUserPlanRequest 
     * @return [Call]<[ElearningModWorkshopGetUserPlan200Response]>
     */
    @POST("mod_workshop_get_user_plan")
    fun modWorkshopGetUserPlan(@Body elearningModWorkshopGetUserPlanRequest: ElearningModWorkshopGetUserPlanRequest): Call<ElearningModWorkshopGetUserPlan200Response>

    /**
     * POST mod_workshop_get_workshop_access_information
     * Return access information for a given workshop.
     * Return access information for a given workshop.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetWorkshopAccessInformationRequest 
     * @return [Call]<[ElearningModWorkshopGetWorkshopAccessInformation200Response]>
     */
    @POST("mod_workshop_get_workshop_access_information")
    fun modWorkshopGetWorkshopAccessInformation(@Body elearningModWorkshopGetWorkshopAccessInformationRequest: ElearningModWorkshopGetWorkshopAccessInformationRequest): Call<ElearningModWorkshopGetWorkshopAccessInformation200Response>

    /**
     * POST mod_workshop_get_workshops_by_courses
     * Returns a list of workshops in a provided list of courses, if no list is provided all workshops that                             the user can view will be returned.
     * Returns a list of workshops in a provided list of courses, if no list is provided all workshops that                             the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModWorkshopGetWorkshopsByCourses200Response]>
     */
    @POST("mod_workshop_get_workshops_by_courses")
    fun modWorkshopGetWorkshopsByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModWorkshopGetWorkshopsByCourses200Response>

    /**
     * POST mod_workshop_update_assessment
     * Add information to an allocated assessment.
     * Add information to an allocated assessment.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopUpdateAssessmentRequest 
     * @return [Call]<[ElearningModWorkshopUpdateAssessment200Response]>
     */
    @POST("mod_workshop_update_assessment")
    fun modWorkshopUpdateAssessment(@Body elearningModWorkshopUpdateAssessmentRequest: ElearningModWorkshopUpdateAssessmentRequest): Call<ElearningModWorkshopUpdateAssessment200Response>

    /**
     * POST mod_workshop_update_submission
     * Update the given submission.
     * Update the given submission.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopUpdateSubmissionRequest 
     * @return [Call]<[ElearningModWorkshopUpdateSubmission200Response]>
     */
    @POST("mod_workshop_update_submission")
    fun modWorkshopUpdateSubmission(@Body elearningModWorkshopUpdateSubmissionRequest: ElearningModWorkshopUpdateSubmissionRequest): Call<ElearningModWorkshopUpdateSubmission200Response>

    /**
     * POST mod_workshop_view_submission
     * Trigger the submission viewed event.
     * Trigger the submission viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopGetSubmissionRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_workshop_view_submission")
    fun modWorkshopViewSubmission(@Body elearningModWorkshopGetSubmissionRequest: ElearningModWorkshopGetSubmissionRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_workshop_view_workshop
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModWorkshopViewWorkshopRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_workshop_view_workshop")
    fun modWorkshopViewWorkshop(@Body elearningModWorkshopViewWorkshopRequest: ElearningModWorkshopViewWorkshopRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
