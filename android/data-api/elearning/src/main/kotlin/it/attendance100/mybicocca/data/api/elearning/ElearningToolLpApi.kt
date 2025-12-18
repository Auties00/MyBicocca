package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCompetencyCompetencyViewedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCompetencyCompletePlanRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetenciesManagePage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetenciesManagePageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetencyFrameworksManagePage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetencyFrameworksManagePageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetencySummary200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCompetencySummaryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCourseCompetenciesPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForCourseCompetenciesPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForPlanPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForPlansPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForPlansPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForRelatedCompetenciesSection200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForRelatedCompetenciesSectionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForTemplateCompetenciesPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForTemplateCompetenciesPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForTemplatesManagePage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummary200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummaryInCourse200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummaryInCourseRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummaryInPlan200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummaryInPlanRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserCompetencySummaryRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserEvidenceListPage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserEvidenceListPageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserEvidencePage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpDataForUserEvidencePageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpSearchCohorts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpSearchCohortsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpSearchUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolLpSearchUsersRequest

interface ToolLpApi {
    /**
     * POST tool_lp_data_for_competencies_manage_page
     * Load the data for the competencies manage page template
     * Load the data for the competencies manage page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForCompetenciesManagePageRequest 
     * @return [Call]<[ElearningToolLpDataForCompetenciesManagePage200Response]>
     */
    @POST("tool_lp_data_for_competencies_manage_page")
    fun toolLpDataForCompetenciesManagePage(@Body elearningToolLpDataForCompetenciesManagePageRequest: ElearningToolLpDataForCompetenciesManagePageRequest): Call<ElearningToolLpDataForCompetenciesManagePage200Response>

    /**
     * POST tool_lp_data_for_competency_frameworks_manage_page
     * Load the data for the competency frameworks manage page template
     * Load the data for the competency frameworks manage page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForCompetencyFrameworksManagePageRequest 
     * @return [Call]<[ElearningToolLpDataForCompetencyFrameworksManagePage200Response]>
     */
    @POST("tool_lp_data_for_competency_frameworks_manage_page")
    fun toolLpDataForCompetencyFrameworksManagePage(@Body elearningToolLpDataForCompetencyFrameworksManagePageRequest: ElearningToolLpDataForCompetencyFrameworksManagePageRequest): Call<ElearningToolLpDataForCompetencyFrameworksManagePage200Response>

    /**
     * POST tool_lp_data_for_competency_summary
     * Load competency data for summary template.
     * Load competency data for summary template.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForCompetencySummaryRequest 
     * @return [Call]<[ElearningToolLpDataForCompetencySummary200Response]>
     */
    @POST("tool_lp_data_for_competency_summary")
    fun toolLpDataForCompetencySummary(@Body elearningToolLpDataForCompetencySummaryRequest: ElearningToolLpDataForCompetencySummaryRequest): Call<ElearningToolLpDataForCompetencySummary200Response>

    /**
     * POST tool_lp_data_for_course_competencies_page
     * Load the data for the course competencies page template.
     * Load the data for the course competencies page template.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForCourseCompetenciesPageRequest 
     * @return [Call]<[ElearningToolLpDataForCourseCompetenciesPage200Response]>
     */
    @POST("tool_lp_data_for_course_competencies_page")
    fun toolLpDataForCourseCompetenciesPage(@Body elearningToolLpDataForCourseCompetenciesPageRequest: ElearningToolLpDataForCourseCompetenciesPageRequest): Call<ElearningToolLpDataForCourseCompetenciesPage200Response>

    /**
     * POST tool_lp_data_for_plan_page
     * Load the data for the plan page template.
     * Load the data for the plan page template.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreCompetencyCompletePlanRequest 
     * @return [Call]<[ElearningToolLpDataForPlanPage200Response]>
     */
    @POST("tool_lp_data_for_plan_page")
    fun toolLpDataForPlanPage(@Body elearningCoreCompetencyCompletePlanRequest: ElearningCoreCompetencyCompletePlanRequest): Call<ElearningToolLpDataForPlanPage200Response>

    /**
     * POST tool_lp_data_for_plans_page
     * Load the data for the plans page template
     * Load the data for the plans page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForPlansPageRequest 
     * @return [Call]<[ElearningToolLpDataForPlansPage200Response]>
     */
    @POST("tool_lp_data_for_plans_page")
    fun toolLpDataForPlansPage(@Body elearningToolLpDataForPlansPageRequest: ElearningToolLpDataForPlansPageRequest): Call<ElearningToolLpDataForPlansPage200Response>

    /**
     * POST tool_lp_data_for_related_competencies_section
     * Load the data for the related competencies template.
     * Load the data for the related competencies template.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForRelatedCompetenciesSectionRequest 
     * @return [Call]<[ElearningToolLpDataForRelatedCompetenciesSection200Response]>
     */
    @POST("tool_lp_data_for_related_competencies_section")
    fun toolLpDataForRelatedCompetenciesSection(@Body elearningToolLpDataForRelatedCompetenciesSectionRequest: ElearningToolLpDataForRelatedCompetenciesSectionRequest): Call<ElearningToolLpDataForRelatedCompetenciesSection200Response>

    /**
     * POST tool_lp_data_for_template_competencies_page
     * Load the data for the template competencies page template.
     * Load the data for the template competencies page template.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForTemplateCompetenciesPageRequest 
     * @return [Call]<[ElearningToolLpDataForTemplateCompetenciesPage200Response]>
     */
    @POST("tool_lp_data_for_template_competencies_page")
    fun toolLpDataForTemplateCompetenciesPage(@Body elearningToolLpDataForTemplateCompetenciesPageRequest: ElearningToolLpDataForTemplateCompetenciesPageRequest): Call<ElearningToolLpDataForTemplateCompetenciesPage200Response>

    /**
     * POST tool_lp_data_for_templates_manage_page
     * Load the data for the learning plan templates manage page template
     * Load the data for the learning plan templates manage page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForCompetencyFrameworksManagePageRequest 
     * @return [Call]<[ElearningToolLpDataForTemplatesManagePage200Response]>
     */
    @POST("tool_lp_data_for_templates_manage_page")
    fun toolLpDataForTemplatesManagePage(@Body elearningToolLpDataForCompetencyFrameworksManagePageRequest: ElearningToolLpDataForCompetencyFrameworksManagePageRequest): Call<ElearningToolLpDataForTemplatesManagePage200Response>

    /**
     * POST tool_lp_data_for_user_competency_summary
     * Load a summary of a user competency.
     * Load a summary of a user competency.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForUserCompetencySummaryRequest 
     * @return [Call]<[ElearningToolLpDataForUserCompetencySummary200Response]>
     */
    @POST("tool_lp_data_for_user_competency_summary")
    fun toolLpDataForUserCompetencySummary(@Body elearningToolLpDataForUserCompetencySummaryRequest: ElearningToolLpDataForUserCompetencySummaryRequest): Call<ElearningToolLpDataForUserCompetencySummary200Response>

    /**
     * POST tool_lp_data_for_user_competency_summary_in_course
     * Load a summary of a user competency.
     * Load a summary of a user competency.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForUserCompetencySummaryInCourseRequest 
     * @return [Call]<[ElearningToolLpDataForUserCompetencySummaryInCourse200Response]>
     */
    @POST("tool_lp_data_for_user_competency_summary_in_course")
    fun toolLpDataForUserCompetencySummaryInCourse(@Body elearningToolLpDataForUserCompetencySummaryInCourseRequest: ElearningToolLpDataForUserCompetencySummaryInCourseRequest): Call<ElearningToolLpDataForUserCompetencySummaryInCourse200Response>

    /**
     * POST tool_lp_data_for_user_competency_summary_in_plan
     * Load a summary of a user competency.
     * Load a summary of a user competency.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForUserCompetencySummaryInPlanRequest 
     * @return [Call]<[ElearningToolLpDataForUserCompetencySummaryInPlan200Response]>
     */
    @POST("tool_lp_data_for_user_competency_summary_in_plan")
    fun toolLpDataForUserCompetencySummaryInPlan(@Body elearningToolLpDataForUserCompetencySummaryInPlanRequest: ElearningToolLpDataForUserCompetencySummaryInPlanRequest): Call<ElearningToolLpDataForUserCompetencySummaryInPlan200Response>

    /**
     * POST tool_lp_data_for_user_evidence_list_page
     * Load the data for the user evidence list page template
     * Load the data for the user evidence list page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForUserEvidenceListPageRequest 
     * @return [Call]<[ElearningToolLpDataForUserEvidenceListPage200Response]>
     */
    @POST("tool_lp_data_for_user_evidence_list_page")
    fun toolLpDataForUserEvidenceListPage(@Body elearningToolLpDataForUserEvidenceListPageRequest: ElearningToolLpDataForUserEvidenceListPageRequest): Call<ElearningToolLpDataForUserEvidenceListPage200Response>

    /**
     * POST tool_lp_data_for_user_evidence_page
     * Load the data for the user evidence page template
     * Load the data for the user evidence page template
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpDataForUserEvidencePageRequest 
     * @return [Call]<[ElearningToolLpDataForUserEvidencePage200Response]>
     */
    @POST("tool_lp_data_for_user_evidence_page")
    fun toolLpDataForUserEvidencePage(@Body elearningToolLpDataForUserEvidencePageRequest: ElearningToolLpDataForUserEvidencePageRequest): Call<ElearningToolLpDataForUserEvidencePage200Response>

    /**
     * POST tool_lp_list_courses_using_competency
     * List the courses using a competency
     * List the courses using a competency
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreCompetencyCompetencyViewedRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_lp_list_courses_using_competency")
    fun toolLpListCoursesUsingCompetency(@Body elearningCoreCompetencyCompetencyViewedRequest: ElearningCoreCompetencyCompetencyViewedRequest): Call<kotlin.Any>

    /**
     * POST tool_lp_search_cohorts
     * Search for cohorts. This method is deprecated, please call &#39;core_cohort_search_cohorts&#39; instead
     * Search for cohorts. This method is deprecated, please call &#39;core_cohort_search_cohorts&#39; instead
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpSearchCohortsRequest 
     * @return [Call]<[ElearningToolLpSearchCohorts200Response]>
     */
    @POST("tool_lp_search_cohorts")
    fun toolLpSearchCohorts(@Body elearningToolLpSearchCohortsRequest: ElearningToolLpSearchCohortsRequest): Call<ElearningToolLpSearchCohorts200Response>

    /**
     * POST tool_lp_search_users
     * Search for users.
     * Search for users.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolLpSearchUsersRequest 
     * @return [Call]<[ElearningToolLpSearchUsers200Response]>
     */
    @POST("tool_lp_search_users")
    fun toolLpSearchUsers(@Body elearningToolLpSearchUsersRequest: ElearningToolLpSearchUsersRequest): Call<ElearningToolLpSearchUsers200Response>

}
