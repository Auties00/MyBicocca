package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonFinishAttempt200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonFinishAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetAttemptsOverview200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetAttemptsOverviewRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetContentPagesViewed200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetContentPagesViewedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetLesson200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetLessonAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetLessonAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetLessonRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetLessonsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetPageData200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetPageDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetPages200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetPagesPossibleJumps200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetPagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetQuestionsAttempts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetQuestionsAttemptsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserAttempt200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserAttemptGrade200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserAttemptGradeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserGrade200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserGradeRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonGetUserTimers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonLaunchAttempt200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonLaunchAttemptRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonProcessPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModLessonProcessPageRequest

interface ModLessonApi {
    /**
     * POST mod_lesson_finish_attempt
     * Finishes the current attempt.
     * Finishes the current attempt.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonFinishAttemptRequest 
     * @return [Call]<[ElearningModLessonFinishAttempt200Response]>
     */
    @POST("mod_lesson_finish_attempt")
    fun modLessonFinishAttempt(@Body elearningModLessonFinishAttemptRequest: ElearningModLessonFinishAttemptRequest): Call<ElearningModLessonFinishAttempt200Response>

    /**
     * POST mod_lesson_get_attempts_overview
     * Get a list of all the attempts made by users in a lesson.
     * Get a list of all the attempts made by users in a lesson.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetAttemptsOverviewRequest 
     * @return [Call]<[ElearningModLessonGetAttemptsOverview200Response]>
     */
    @POST("mod_lesson_get_attempts_overview")
    fun modLessonGetAttemptsOverview(@Body elearningModLessonGetAttemptsOverviewRequest: ElearningModLessonGetAttemptsOverviewRequest): Call<ElearningModLessonGetAttemptsOverview200Response>

    /**
     * POST mod_lesson_get_content_pages_viewed
     * Return the list of content pages viewed by a user during a lesson attempt.
     * Return the list of content pages viewed by a user during a lesson attempt.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetContentPagesViewedRequest 
     * @return [Call]<[ElearningModLessonGetContentPagesViewed200Response]>
     */
    @POST("mod_lesson_get_content_pages_viewed")
    fun modLessonGetContentPagesViewed(@Body elearningModLessonGetContentPagesViewedRequest: ElearningModLessonGetContentPagesViewedRequest): Call<ElearningModLessonGetContentPagesViewed200Response>

    /**
     * POST mod_lesson_get_lesson
     * Return information of a given lesson.
     * Return information of a given lesson.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetLessonRequest 
     * @return [Call]<[ElearningModLessonGetLesson200Response]>
     */
    @POST("mod_lesson_get_lesson")
    fun modLessonGetLesson(@Body elearningModLessonGetLessonRequest: ElearningModLessonGetLessonRequest): Call<ElearningModLessonGetLesson200Response>

    /**
     * POST mod_lesson_get_lesson_access_information
     * Return access information for a given lesson.
     * Return access information for a given lesson.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetLessonAccessInformationRequest 
     * @return [Call]<[ElearningModLessonGetLessonAccessInformation200Response]>
     */
    @POST("mod_lesson_get_lesson_access_information")
    fun modLessonGetLessonAccessInformation(@Body elearningModLessonGetLessonAccessInformationRequest: ElearningModLessonGetLessonAccessInformationRequest): Call<ElearningModLessonGetLessonAccessInformation200Response>

    /**
     * POST mod_lesson_get_lessons_by_courses
     * Returns a list of lessons in a provided list of courses,                             if no list is provided all lessons that the user can view will be returned.
     * Returns a list of lessons in a provided list of courses,                             if no list is provided all lessons that the user can view will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModLessonGetLessonsByCourses200Response]>
     */
    @POST("mod_lesson_get_lessons_by_courses")
    fun modLessonGetLessonsByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModLessonGetLessonsByCourses200Response>

    /**
     * POST mod_lesson_get_page_data
     * Return information of a given page, including its contents.
     * Return information of a given page, including its contents.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetPageDataRequest 
     * @return [Call]<[ElearningModLessonGetPageData200Response]>
     */
    @POST("mod_lesson_get_page_data")
    fun modLessonGetPageData(@Body elearningModLessonGetPageDataRequest: ElearningModLessonGetPageDataRequest): Call<ElearningModLessonGetPageData200Response>

    /**
     * POST mod_lesson_get_pages
     * Return the list of pages in a lesson (based on the user permissions).
     * Return the list of pages in a lesson (based on the user permissions).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetPagesRequest 
     * @return [Call]<[ElearningModLessonGetPages200Response]>
     */
    @POST("mod_lesson_get_pages")
    fun modLessonGetPages(@Body elearningModLessonGetPagesRequest: ElearningModLessonGetPagesRequest): Call<ElearningModLessonGetPages200Response>

    /**
     * POST mod_lesson_get_pages_possible_jumps
     * Return all the possible jumps for the pages in a given lesson.
     * Return all the possible jumps for the pages in a given lesson.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetLessonAccessInformationRequest 
     * @return [Call]<[ElearningModLessonGetPagesPossibleJumps200Response]>
     */
    @POST("mod_lesson_get_pages_possible_jumps")
    fun modLessonGetPagesPossibleJumps(@Body elearningModLessonGetLessonAccessInformationRequest: ElearningModLessonGetLessonAccessInformationRequest): Call<ElearningModLessonGetPagesPossibleJumps200Response>

    /**
     * POST mod_lesson_get_questions_attempts
     * Return the list of questions attempts in a given lesson.
     * Return the list of questions attempts in a given lesson.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetQuestionsAttemptsRequest 
     * @return [Call]<[ElearningModLessonGetQuestionsAttempts200Response]>
     */
    @POST("mod_lesson_get_questions_attempts")
    fun modLessonGetQuestionsAttempts(@Body elearningModLessonGetQuestionsAttemptsRequest: ElearningModLessonGetQuestionsAttemptsRequest): Call<ElearningModLessonGetQuestionsAttempts200Response>

    /**
     * POST mod_lesson_get_user_attempt
     * Return information about the given user attempt (including answers).
     * Return information about the given user attempt (including answers).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetUserAttemptRequest 
     * @return [Call]<[ElearningModLessonGetUserAttempt200Response]>
     */
    @POST("mod_lesson_get_user_attempt")
    fun modLessonGetUserAttempt(@Body elearningModLessonGetUserAttemptRequest: ElearningModLessonGetUserAttemptRequest): Call<ElearningModLessonGetUserAttempt200Response>

    /**
     * POST mod_lesson_get_user_attempt_grade
     * Return grade information in the attempt for a given user.
     * Return grade information in the attempt for a given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetUserAttemptGradeRequest 
     * @return [Call]<[ElearningModLessonGetUserAttemptGrade200Response]>
     */
    @POST("mod_lesson_get_user_attempt_grade")
    fun modLessonGetUserAttemptGrade(@Body elearningModLessonGetUserAttemptGradeRequest: ElearningModLessonGetUserAttemptGradeRequest): Call<ElearningModLessonGetUserAttemptGrade200Response>

    /**
     * POST mod_lesson_get_user_grade
     * Return the final grade in the lesson for the given user.
     * Return the final grade in the lesson for the given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetUserGradeRequest 
     * @return [Call]<[ElearningModLessonGetUserGrade200Response]>
     */
    @POST("mod_lesson_get_user_grade")
    fun modLessonGetUserGrade(@Body elearningModLessonGetUserGradeRequest: ElearningModLessonGetUserGradeRequest): Call<ElearningModLessonGetUserGrade200Response>

    /**
     * POST mod_lesson_get_user_timers
     * Return the timers in the current lesson for the given user.
     * Return the timers in the current lesson for the given user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetUserGradeRequest 
     * @return [Call]<[ElearningModLessonGetUserTimers200Response]>
     */
    @POST("mod_lesson_get_user_timers")
    fun modLessonGetUserTimers(@Body elearningModLessonGetUserGradeRequest: ElearningModLessonGetUserGradeRequest): Call<ElearningModLessonGetUserTimers200Response>

    /**
     * POST mod_lesson_launch_attempt
     * Starts a new attempt or continues an existing one.
     * Starts a new attempt or continues an existing one.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonLaunchAttemptRequest 
     * @return [Call]<[ElearningModLessonLaunchAttempt200Response]>
     */
    @POST("mod_lesson_launch_attempt")
    fun modLessonLaunchAttempt(@Body elearningModLessonLaunchAttemptRequest: ElearningModLessonLaunchAttemptRequest): Call<ElearningModLessonLaunchAttempt200Response>

    /**
     * POST mod_lesson_process_page
     * Processes page responses.
     * Processes page responses.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonProcessPageRequest 
     * @return [Call]<[ElearningModLessonProcessPage200Response]>
     */
    @POST("mod_lesson_process_page")
    fun modLessonProcessPage(@Body elearningModLessonProcessPageRequest: ElearningModLessonProcessPageRequest): Call<ElearningModLessonProcessPage200Response>

    /**
     * POST mod_lesson_view_lesson
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModLessonGetLessonRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_lesson_view_lesson")
    fun modLessonViewLesson(@Body elearningModLessonGetLessonRequest: ElearningModLessonGetLessonRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
