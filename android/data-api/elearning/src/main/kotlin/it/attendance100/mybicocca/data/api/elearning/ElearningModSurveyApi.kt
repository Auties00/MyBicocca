package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModSurveyGetQuestions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModSurveyGetQuestionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModSurveyGetSurveysByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModSurveySubmitAnswersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModSurveyViewSurveyRequest

interface ModSurveyApi {
    /**
     * POST mod_survey_get_questions
     * Get the complete list of questions for the survey, including subquestions.
     * Get the complete list of questions for the survey, including subquestions.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModSurveyGetQuestionsRequest 
     * @return [Call]<[ElearningModSurveyGetQuestions200Response]>
     */
    @POST("mod_survey_get_questions")
    fun modSurveyGetQuestions(@Body elearningModSurveyGetQuestionsRequest: ElearningModSurveyGetQuestionsRequest): Call<ElearningModSurveyGetQuestions200Response>

    /**
     * POST mod_survey_get_surveys_by_courses
     * Returns a list of survey instances in a provided set of courses,                             if no courses are provided then all the survey instances the user has access to will be returned.
     * Returns a list of survey instances in a provided set of courses,                             if no courses are provided then all the survey instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModSurveyGetSurveysByCourses200Response]>
     */
    @POST("mod_survey_get_surveys_by_courses")
    fun modSurveyGetSurveysByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModSurveyGetSurveysByCourses200Response>

    /**
     * POST mod_survey_submit_answers
     * Submit the answers for a given survey.
     * Submit the answers for a given survey.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModSurveySubmitAnswersRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_survey_submit_answers")
    fun modSurveySubmitAnswers(@Body elearningModSurveySubmitAnswersRequest: ElearningModSurveySubmitAnswersRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_survey_view_survey
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModSurveyViewSurveyRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_survey_view_survey")
    fun modSurveyViewSurvey(@Body elearningModSurveyViewSurveyRequest: ElearningModSurveyViewSurveyRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
