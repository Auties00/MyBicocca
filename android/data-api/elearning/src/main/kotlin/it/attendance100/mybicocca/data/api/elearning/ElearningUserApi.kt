package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.elearning.*
import kotlinx.serialization.json.Json

/**
 * API for user-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningUserApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets users by their IDs.
     */
    suspend fun getUsersById(wsToken: String, userIds: List<Int>): ElearningGetUsersByFieldResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUsersByFieldRequest.byId(userIds))
    }

    /**
     * Gets a user by their ID.
     */
    suspend fun getUserById(wsToken: String, userId: Int): ElearningGetUsersByFieldResponse {
        return getUsersById(wsToken, listOf(userId))
    }

    /**
     * Gets users by their usernames.
     */
    suspend fun getUsersByUsername(wsToken: String, usernames: List<String>): ElearningGetUsersByFieldResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUsersByFieldRequest.byUsername(usernames))
    }

    /**
     * Gets users by their email addresses.
     */
    suspend fun getUsersByEmail(wsToken: String, emails: List<String>): ElearningGetUsersByFieldResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUsersByFieldRequest.byEmail(emails))
    }

    /**
     * Gets user profiles in the context of a course.
     */
    suspend fun getCourseUserProfiles(
        wsToken: String,
        userList: List<ElearningUserCourseRequest>
    ): ElearningGetCourseUserProfilesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetCourseUserProfilesRequest(userList))
    }

    /**
     * Gets a user profile in the context of a specific course.
     */
    suspend fun getCourseUserProfile(
        wsToken: String,
        userId: Int,
        courseId: Int
    ): ElearningGetCourseUserProfilesResponse {
        return getCourseUserProfiles(wsToken, listOf(ElearningUserCourseRequest(userId, courseId)))
    }

    /**
     * Gets user preferences.
     */
    suspend fun getUserPreferences(
        wsToken: String,
        name: String? = null,
        userId: Int? = null
    ): ElearningGetUserPreferencesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUserPreferencesRequest(name, userId))
    }

    /**
     * Updates user preferences.
     */
    suspend fun updateUserPreferences(
        wsToken: String,
        preferences: List<ElearningUserPreference>,
        userId: Int? = null
    ): ElearningUpdateUserPreferencesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningUpdateUserPreferencesRequest(userId, preferences))
    }

    /**
     * Logs a view of a user profile.
     */
    suspend fun viewUserProfile(
        wsToken: String,
        userId: Int,
        courseId: Int? = null
    ): ElearningViewUserProfileResponse {
        return executeAuthenticatedRequest(wsToken, ElearningViewUserProfileRequest(userId, courseId))
    }

    /**
     * Registers a device for push notifications.
     */
    suspend fun addUserDevice(
        wsToken: String,
        appId: String,
        name: String,
        model: String,
        platform: String,
        version: String,
        pushId: String,
        uuid: String
    ): ElearningAddUserDeviceResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningAddUserDeviceRequest(appId, name, model, platform, version, pushId, uuid)
        )
    }

    /**
     * Removes a registered device.
     */
    suspend fun removeUserDevice(
        wsToken: String,
        uuid: String,
        appId: String? = null
    ): ElearningRemoveUserDeviceResponse {
        return executeAuthenticatedRequest(wsToken, ElearningRemoveUserDeviceRequest(uuid, appId))
    }

    /**
     * Gets information about user's private files.
     */
    suspend fun getPrivateFilesInfo(
        wsToken: String,
        userId: Int? = null
    ): ElearningGetPrivateFilesInfoResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetPrivateFilesInfoRequest(userId))
    }
}
