package it.attendance100.mybicocca.data.remote.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.CheckUpdatesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.CheckUpdatesResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.Course
import it.attendance100.mybicocca.data.remote.dto.elearning.CourseCategory
import it.attendance100.mybicocca.data.remote.dto.elearning.CourseModuleResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.CourseSection
import it.attendance100.mybicocca.data.remote.dto.elearning.EnrolledCourse
import it.attendance100.mybicocca.data.remote.dto.elearning.EnrolledUser
import it.attendance100.mybicocca.data.remote.dto.elearning.EnrolmentMethod
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCategoriesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCourseContentsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCourseModuleByInstanceRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCourseModuleRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCoursesByFieldRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCoursesByFieldResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.GetCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetEnrolledUsersRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetEnrolmentMethodsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetGuestEnrolInfoRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetRecentCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetSelfEnrolInfoRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetUserAdministrationOptionsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetUserNavigationOptionsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetUsersCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GuestEnrolInfoResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.RecentCourse
import it.attendance100.mybicocca.data.remote.dto.elearning.SearchCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.SearchCoursesResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.SearchUsersRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.SearchedUser
import it.attendance100.mybicocca.data.remote.dto.elearning.SelfEnrolInfoResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.SelfEnrolUserRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.SelfEnrolUserResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.SetFavouriteCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.UserNavigationOptionsResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.ValidateGuestPasswordRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.ValidateGuestPasswordResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.ViewCourseRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Course & Enrolment API
 *
 * Handles courses, contents, categories, and user enrolments.
 *
 * ## Key Features
 *
 * - **Course Listings:** Get enrolled courses, recent courses, or search by criteria.
 * - **Contents:** Retrieve course sections, modules, and completion data.
 * - **Enrolment:** Self-enrol, get enrolled users, and check enrolment methods.
 * - **Categories:** Browse course categories.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get user's courses
 * val courses = courseApi.getUsersCourses(
 *     GetUsersCoursesRequest(userId = userId)
 * )
 *
 * // Get course content
 * val content = courseApi.getContents(
 *     GetCourseContentsRequest(courseId = courseId)
 * )
 * ```
 */
interface ElearningCourseApi {

    /**
     * Retrieve course details.
     *
     * @param request List of course IDs.
     * @return List of courses.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_courses")
    suspend fun getCourses(@Body request: GetCoursesRequest): Response<List<Course>>

    /**
     * Get course contents (sections and modules).
     *
     * @param request Course ID.
     * @return List of course sections.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_contents")
    suspend fun getContents(@Body request: GetCourseContentsRequest): Response<List<CourseSection>>

    /**
     * Get course categories.
     *
     * @param request Search criteria for categories.
     * @return List of categories.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_categories")
    suspend fun getCategories(@Body request: GetCategoriesRequest): Response<List<CourseCategory>>

    /**
     * Search courses by search string.
     *
     * @param request Search string and criteria.
     * @return Search results.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_search_courses")
    suspend fun searchCourses(@Body request: SearchCoursesRequest): Response<SearchCoursesResponse>

    /**
     * Get recent courses for the user.
     *
     * @param request User ID and limit.
     * @return List of recent courses.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_recent_courses")
    suspend fun getRecentCourses(@Body request: GetRecentCoursesRequest): Response<List<RecentCourse>>

    /**
     * Get courses matching a specific field value.
     *
     * @param request Field name and value.
     * @return Courses matching the field.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_courses_by_field")
    suspend fun getCoursesByField(@Body request: GetCoursesByFieldRequest): Response<GetCoursesByFieldResponse>

    /**
     * Check if there are updates for a user in a course.
     *
     * @param request Course ID and timestamp.
     * @return Update information.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_check_updates")
    suspend fun checkUpdates(@Body request: CheckUpdatesRequest): Response<CheckUpdatesResponse>

    /**
     * Log that a course was viewed.
     *
     * @param request Course ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_view_course")
    suspend fun viewCourse(@Body request: ViewCourseRequest): Response<Any>

    /**
     * Retrieve navigation options for a course.
     *
     * @param request Course IDs.
     * @return Navigation options.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_user_navigation_options")
    suspend fun getUserNavigationOptions(@Body request: GetUserNavigationOptionsRequest): Response<UserNavigationOptionsResponse>

    /**
     * Retrieve administration options for a course.
     *
     * @param request Course IDs.
     * @return Administration options.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_user_administration_options")
    suspend fun getUserAdministrationOptions(@Body request: GetUserAdministrationOptionsRequest): Response<UserNavigationOptionsResponse>

    /**
     * Get course module information.
     *
     * @param request Module ID.
     * @return Module details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_course_module")
    suspend fun getCourseModule(@Body request: GetCourseModuleRequest): Response<CourseModuleResponse>

    /**
     * Get course module by instance ID.
     *
     * @param request Module name and instance ID.
     * @return Module details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_get_course_module_by_instance")
    suspend fun getCourseModuleByInstance(@Body request: GetCourseModuleByInstanceRequest): Response<CourseModuleResponse>

    /**
     * Set a course as favourite (starred).
     *
     * @param request Course IDs and favourite status.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_course_set_favourite_courses")
    suspend fun setFavouriteCourses(@Body request: SetFavouriteCoursesRequest): Response<Any>

    /**
     * Get the list of courses where a user is enrolled.
     *
     * @param request User ID.
     * @return List of enrolled courses.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_enrol_get_users_courses")
    suspend fun getUsersCourses(@Body request: GetUsersCoursesRequest): Response<List<EnrolledCourse>>

    /**
     * Get enrolled users in a course.
     *
     * @param request Course ID and options.
     * @return List of enrolled users.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_enrol_get_enrolled_users")
    suspend fun getEnrolledUsers(@Body request: GetEnrolledUsersRequest): Response<List<EnrolledUser>>

    /**
     * Get available enrolment methods for a course.
     *
     * @param request Course ID.
     * @return List of enrolment methods.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_enrol_get_course_enrolment_methods")
    suspend fun getCourseEnrolmentMethods(@Body request: GetEnrolmentMethodsRequest): Response<List<EnrolmentMethod>>

    /**
     * Search users in a course.
     *
     * @param request Course ID and search string.
     * @return List of matching users.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_enrol_search_users")
    suspend fun searchUsers(@Body request: SearchUsersRequest): Response<List<SearchedUser>>

    /**
     * Get information about a self-enrolment instance.
     *
     * @param request Instance ID.
     * @return Instance details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=enrol_self_get_instance_info")
    suspend fun getSelfEnrolInstanceInfo(@Body request: GetSelfEnrolInfoRequest): Response<SelfEnrolInfoResponse>

    /**
     * Self-enrol a user in a course.
     *
     * @param request Course ID and password.
     * @return Enrolment result.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=enrol_self_enrol_user")
    suspend fun selfEnrolUser(@Body request: SelfEnrolUserRequest): Response<SelfEnrolUserResponse>

    /**
     * Get information about a guest enrolment instance.
     *
     * @param request Instance ID.
     * @return Instance details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=enrol_guest_get_instance_info")
    suspend fun getGuestEnrolInstanceInfo(@Body request: GetGuestEnrolInfoRequest): Response<GuestEnrolInfoResponse>

    /**
     * Validate a guest access password.
     *
     * @param request Instance ID and password.
     * @return Validation result.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=enrol_guest_validate_password")
    suspend fun validateGuestPassword(@Body request: ValidateGuestPasswordRequest): Response<ValidateGuestPasswordResponse>
}
