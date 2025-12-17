package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.dto.elearning.*
import retrofit2.*
import retrofit2.http.*

/**
 * # Elearning User API
 *
 * Handles user profiles, preferences, devices, and files.
 *
 * ## Key Features
 *
 * - **Profiles:** Search users, view profiles, and list course participants.
 * - **Preferences:** Get and update user preferences.
 * - **Files:** Manage private files.
 * - **Devices:** Add/remove user devices for push notifications.
 * - **Policy:** Agree to site policies.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get user profile
 * val users = userApi.getUsersByField(
 *     GetUsersByFieldRequest(field = UserSearchFieldEnum.ID, values = listOf("123"))
 * )
 * ```
 */
interface ElearningUserApi {

    /**
     * Retrieve users' information for a specified unique field (id, username, email, etc.).
     *
     * @param request Search criteria.
     * @return A list of user profiles.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_get_users_by_field")
    suspend fun getUsersByField(@Body request: GetUsersByFieldRequest): Response<List<UserProfile>>

    /**
     * Retrieve user profiles for a specific course.
     *
     * @param request List of user IDs and course ID.
     * @return List of user profiles.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_get_course_user_profiles")
    suspend fun getCourseUserProfiles(@Body request: GetCourseUserProfilesRequest): Response<List<CourseUserProfile>>

    /**
     * Log that a user profile has been viewed.
     *
     * @param request User ID and course ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_view_user_profile")
    suspend fun viewUserProfile(@Body request: ViewUserProfileRequest): Response<Any>

    /**
     * Log that the user list has been viewed.
     *
     * @param request Course ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_view_user_list")
    suspend fun viewUserList(@Body request: ViewUserListRequest): Response<Any>

    /**
     * Update the user's profile picture.
     *
     * @param request Draft item ID containing the image.
     * @return URL of the new profile picture.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_update_picture")
    suspend fun updatePicture(@Body request: UpdatePictureRequest): Response<UpdatePictureResponse>

    /**
     * Return user preferences.
     *
     * @param request Filter for preferences (name, user ID).
     * @return List of preferences.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_get_user_preferences")
    suspend fun getUserPreferences(@Body request: GetUserPreferencesRequest): Response<UserPreferencesResponse>

    /**
     * Set user preferences.
     *
     * @param request List of preferences to set.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_update_user_preferences")
    suspend fun updateUserPreferences(@Body request: UpdateUserPreferencesRequest): Response<Any>

    /**
     * Returns information about the user's private files.
     *
     * @param request User ID (optional).
     * @return File info (count, size, limit).
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_get_private_files_info")
    suspend fun getPrivateFilesInfo(@Body request: GetPrivateFilesInfoRequest): Response<PrivateFilesInfoResponse>

    /**
     * Copy files from a draft area to the user's private files area.
     *
     * @param request Draft item ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_add_user_private_files")
    suspend fun addUserPrivateFiles(@Body request: AddUserPrivateFilesRequest): Response<AddUserPrivateFilesResponse>

    /**
     * Store mobile device information for push notifications.
     *
     * @param request Device details (app ID, token, model, etc.).
     * @return List of registered devices.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_add_user_device")
    suspend fun addUserDevice(@Body request: AddUserDeviceRequest): Response<List<UserDevice>>

    /**
     * Remove a user device from the push notification service.
     *
     * @param request UUID and App ID.
     * @return Removal status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_remove_user_device")
    suspend fun removeUserDevice(@Body request: RemoveUserDeviceRequest): Response<RemoveUserDeviceResponse>

    /**
     * Agree to the site policy.
     *
     * @param request Empty or minimal request.
     * @return Agreement status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_user_agree_site_policy")
    suspend fun agreeSitePolicy(@Body request: AgreeSitePolicyRequest): Response<AgreeSitePolicyResponse>
}
