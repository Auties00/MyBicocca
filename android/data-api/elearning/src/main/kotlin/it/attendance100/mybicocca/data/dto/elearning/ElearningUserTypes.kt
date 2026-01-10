package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve users by a specific field such as id, username, or email.
 *
 * @property field The field to search by (id, username, email, idnumber).
 * @property values The list of values to search for.
 */
@Serializable
class ElearningGetUsersByFieldRequest(
    private val field: String,
    private val values: List<String>
) : ElearningRequest<ElearningGetUsersByFieldResponse> {
    override val functionName = "core_user_get_users_by_field"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("field", field)
        values.forEachIndexed { index, value ->
            formData.append("values[$index]", value)
        }
    }

    companion object {
        /**
         * Creates a request to find users by their unique identifiers.
         *
         * @param userIds The list of user identifiers to search for.
         * @return A configured request instance.
         */
        fun byId(userIds: List<Int>) = ElearningGetUsersByFieldRequest(
            "id",
            userIds.map { it.toString() }
        )

        /**
         * Creates a request to find users by their usernames.
         *
         * @param usernames The list of usernames to search for.
         * @return A configured request instance.
         */
        fun byUsername(usernames: List<String>) = ElearningGetUsersByFieldRequest(
            "username",
            usernames
        )

        /**
         * Creates a request to find users by their email addresses.
         *
         * @param emails The list of email addresses to search for.
         * @return A configured request instance.
         */
        fun byEmail(emails: List<String>) = ElearningGetUsersByFieldRequest(
            "email",
            emails
        )

        /**
         * Creates a request to find users by their identification numbers.
         *
         * @param identificationNumbers The list of identification numbers to search for.
         * @return A configured request instance.
         */
        fun byIdNumber(identificationNumbers: List<String>) = ElearningGetUsersByFieldRequest(
            "idnumber",
            identificationNumbers
        )
    }
}

/**
 * Response containing a list of user profiles matching the search criteria.
 *
 * @property items The list of user profiles found.
 */
@Serializable
data class ElearningGetUsersByFieldResponse(
    override val items: List<ElearningUserProfile>
) : ElearningListResponse<ElearningUserProfile> {
    /**
     * Alias for [items] providing a more descriptive name.
     */
    val users: List<ElearningUserProfile> get() = items
}

/**
 * Request parameters for retrieving user profiles within specific courses.
 *
 * @property userId The unique identifier of the user.
 * @property courseId The unique identifier of the course.
 */
@Serializable
data class ElearningUserCourseRequest(
    @SerialName("userid")
    val userId: Int,
    @SerialName("courseid")
    val courseId: Int
)

/**
 * Request to retrieve user profiles for specific user-course combinations.
 *
 * @property userList The list of user-course pairs to retrieve profiles for.
 */
@Serializable
class ElearningGetCourseUserProfilesRequest(
    private val userList: List<ElearningUserCourseRequest>
) : ElearningRequest<ElearningGetCourseUserProfilesResponse> {
    override val functionName = "core_user_get_course_user_profiles"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userList.forEachIndexed { index, request ->
            formData.append("userlist[$index][userid]", request.userId.toString())
            formData.append("userlist[$index][courseid]", request.courseId.toString())
        }
    }
}

/**
 * Response containing user profiles for the requested user-course combinations.
 *
 * @property items The list of user profiles found.
 */
@Serializable
data class ElearningGetCourseUserProfilesResponse(
    override val items: List<ElearningUserProfile>
) : ElearningListResponse<ElearningUserProfile> {
    /**
     * Alias for [items] providing a more descriptive name.
     */
    val users: List<ElearningUserProfile> get() = items
}

/**
 * Request to retrieve user preferences.
 *
 * @property name Optional name of a specific preference to retrieve. If null, all preferences are returned.
 * @property userId Optional user identifier. If null, the current user's preferences are retrieved.
 */
@Serializable
class ElearningGetUserPreferencesRequest(
    private val name: String? = null,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetUserPreferencesResponse> {
    override val functionName = "core_user_get_user_preferences"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        name?.let { formData.append("name", it) }
        userId?.let { formData.append("userid", it.toString()) }
    }
}

/**
 * Response containing user preferences.
 *
 * @property preferences The list of user preferences.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetUserPreferencesResponse(
    @SerialName("preferences")
    val preferences: List<ElearningUserPreference> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to update user preferences.
 *
 * @property userId Optional user identifier. If null, the current user's preferences are updated.
 * @property preferences The list of preferences to update.
 */
@Serializable
class ElearningUpdateUserPreferencesRequest(
    private val userId: Int? = null,
    private val preferences: List<ElearningUserPreference>
) : ElearningRequest<ElearningUpdateUserPreferencesResponse> {
    override val functionName = "core_user_update_user_preferences"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
        preferences.forEachIndexed { index, preference ->
            formData.append("preferences[$index][type]", preference.name)
            preference.value?.let { formData.append("preferences[$index][value]", it) }
        }
    }
}

/**
 * Response after updating user preferences.
 *
 * @property saved Whether the preferences were successfully saved.
 * @property warnings Any warnings generated during the update.
 */
@Serializable
data class ElearningUpdateUserPreferencesResponse(
    @SerialName("saved")
    val saved: Boolean? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to trigger a profile view event for analytics tracking.
 *
 * @property userId The unique identifier of the user whose profile is being viewed.
 * @property courseId Optional course identifier for course-specific profile views.
 */
@Serializable
class ElearningViewUserProfileRequest(
    private val userId: Int,
    private val courseId: Int? = null
) : ElearningRequest<ElearningViewUserProfileResponse> {
    override val functionName = "core_user_view_user_profile"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        courseId?.let { formData.append("courseid", it.toString()) }
    }
}

/**
 * Response after triggering a profile view event.
 *
 * @property status Whether the view event was successfully recorded.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningViewUserProfileResponse(
    @SerialName("status")
    val status: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to register a user device for push notifications.
 *
 * @property applicationId The unique identifier of the application.
 * @property name The name of the device.
 * @property model The model of the device.
 * @property platform The operating system platform (e.g., "android", "ios").
 * @property version The version of the application.
 * @property pushIdentifier The push notification token.
 * @property universallyUniqueIdentifier The unique identifier for the device.
 */
@Serializable
class ElearningAddUserDeviceRequest(
    private val applicationId: String,
    private val name: String,
    private val model: String,
    private val platform: String,
    private val version: String,
    private val pushIdentifier: String,
    private val universallyUniqueIdentifier: String
) : ElearningRequest<ElearningAddUserDeviceResponse> {
    override val functionName = "core_user_add_user_device"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("appid", applicationId)
        formData.append("name", name)
        formData.append("model", model)
        formData.append("platform", platform)
        formData.append("version", version)
        formData.append("pushid", pushIdentifier)
        formData.append("uuid", universallyUniqueIdentifier)
    }
}

/**
 * Response after registering a user device.
 *
 * @property items The list of device registration results.
 */
@Serializable
data class ElearningAddUserDeviceResponse(
    override val items: List<ElearningDeviceResult>
) : ElearningListResponse<ElearningDeviceResult>

/**
 * Result of a device registration operation.
 *
 * @property id The unique identifier assigned to the device registration.
 * @property pushIdentifier The push notification token that was registered.
 * @property warnings Any warnings generated during registration.
 */
@Serializable
data class ElearningDeviceResult(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("pushid")
    val pushIdentifier: String? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
)

/**
 * Request to remove a user device from push notification registration.
 *
 * @property universallyUniqueIdentifier The unique identifier of the device to remove.
 * @property applicationId Optional application identifier to limit removal to a specific app.
 */
@Serializable
class ElearningRemoveUserDeviceRequest(
    private val universallyUniqueIdentifier: String,
    private val applicationId: String? = null
) : ElearningRequest<ElearningRemoveUserDeviceResponse> {
    override val functionName = "core_user_remove_user_device"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("uuid", universallyUniqueIdentifier)
        applicationId?.let { formData.append("appid", it) }
    }
}

/**
 * Response after removing a user device.
 *
 * @property removed Whether the device was successfully removed.
 * @property warnings Any warnings generated during removal.
 */
@Serializable
data class ElearningRemoveUserDeviceResponse(
    @SerialName("removed")
    val removed: Boolean,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to retrieve information about a user's private files storage.
 *
 * @property userId Optional user identifier. If null, information for the current user is retrieved.
 */
@Serializable
class ElearningGetPrivateFilesInfoRequest(
    private val userId: Int? = null
) : ElearningRequest<ElearningGetPrivateFilesInfoResponse> {
    override val functionName = "core_user_get_private_files_info"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
    }
}

/**
 * Response containing information about a user's private files storage.
 *
 * @property fileCount The total number of files in private storage.
 * @property folderCount The total number of folders in private storage.
 * @property fileSize The total size of all files in bytes.
 * @property fileSizeWithoutReferences The total size excluding referenced files in bytes.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetPrivateFilesInfoResponse(
    @SerialName("filecount")
    val fileCount: Int,
    @SerialName("foldercount")
    val folderCount: Int,
    @SerialName("filesize")
    val fileSize: Long,
    @SerialName("filesizewithoutreferences")
    val fileSizeWithoutReferences: Long,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents a complete user profile with all available fields.
 *
 * @property id The unique identifier of the user.
 * @property username The username used for login.
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property fullName The user's full display name.
 * @property email The user's email address.
 * @property department The department the user belongs to.
 * @property institution The institution the user is associated with.
 * @property identificationNumber An external identification number for the user.
 * @property firstAccessTimestamp Unix timestamp of the user's first access to the site.
 * @property lastAccessTimestamp Unix timestamp of the user's last access to the site.
 * @property lastCourseAccessTimestamp Unix timestamp of the user's last access to any course.
 * @property description The user's profile description.
 * @property descriptionFormat The format of the description (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 * @property city The user's city.
 * @property country The user's country code.
 * @property profileImageUrlSmall URL to the small version of the user's profile picture.
 * @property profileImageUrl URL to the full version of the user's profile picture.
 * @property customFields List of custom profile fields for the user.
 * @property groups List of groups the user belongs to.
 * @property roles List of roles assigned to the user.
 * @property enrolledCourses List of courses the user is enrolled in.
 */
@Serializable
data class ElearningUserProfile(
    @SerialName("id")
    val id: Int,
    @SerialName("username")
    val username: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,
    @SerialName("lastname")
    val lastName: String? = null,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("department")
    val department: String? = null,
    @SerialName("institution")
    val institution: String? = null,
    @SerialName("idnumber")
    val identificationNumber: String? = null,
    @SerialName("firstaccess")
    val firstAccessTimestamp: Long? = null,
    @SerialName("lastaccess")
    val lastAccessTimestamp: Long? = null,
    @SerialName("lastcourseaccess")
    val lastCourseAccessTimestamp: Long? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("descriptionformat")
    val descriptionFormat: Int? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("profileimageurlsmall")
    val profileImageUrlSmall: String? = null,
    @SerialName("profileimageurl")
    val profileImageUrl: String? = null,
    @SerialName("customfields")
    val customFields: List<ElearningUserCustomField>? = null,
    @SerialName("groups")
    val groups: List<ElearningUserGroup>? = null,
    @SerialName("roles")
    val roles: List<ElearningUserRole>? = null,
    @SerialName("enrolledcourses")
    val enrolledCourses: List<ElearningUserEnrolledCourse>? = null
)

/**
 * Represents a custom profile field for a user.
 *
 * @property type The type of the custom field.
 * @property value The value of the custom field.
 * @property name The display name of the custom field.
 * @property shortName The short identifier name of the custom field.
 */
@Serializable
data class ElearningUserCustomField(
    @SerialName("type")
    val type: String? = null,
    @SerialName("value")
    val value: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("shortname")
    val shortName: String? = null
)

/**
 * Represents a group that a user belongs to.
 *
 * @property id The unique identifier of the group.
 * @property name The name of the group.
 * @property description The description of the group.
 * @property descriptionFormat The format of the description (0 = Moodle, 1 = HTML, 2 = Plain, 4 = Markdown).
 */
@Serializable
data class ElearningUserGroup(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("descriptionformat")
    val descriptionFormat: Int? = null
)

/**
 * Represents a role assigned to a user.
 *
 * @property roleId The unique identifier of the role.
 * @property name The display name of the role.
 * @property shortName The short identifier name of the role.
 * @property sortOrder The sort order for displaying roles.
 */
@Serializable
data class ElearningUserRole(
    @SerialName("roleid")
    val roleId: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("sortorder")
    val sortOrder: Int? = null
)

/**
 * Represents a course that a user is enrolled in.
 *
 * @property id The unique identifier of the course.
 * @property fullName The full name of the course.
 * @property shortName The short identifier name of the course.
 */
@Serializable
data class ElearningUserEnrolledCourse(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null
)

/**
 * Represents a user preference setting.
 *
 * @property name The name of the preference.
 * @property value The value of the preference.
 */
@Serializable
data class ElearningUserPreference(
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: String? = null
)

/**
 * Request to block a user.
 *
 * @param userId The user ID performing the block
 * @param blockedUserId The user ID to block
 */
@Serializable
class ElearningBlockUserRequest(
    private val userId: Int,
    private val blockedUserId: Int
) : ElearningRequest<ElearningBlockUserResponse> {
    override val functionName = "core_message_block_user"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("blockeduserid", blockedUserId.toString())
    }
}

/**
 * Response containing the result of blocking a user.
 *
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningBlockUserResponse(
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to unblock a user.
 *
 * @param userId The user ID performing the unblock
 * @param unblockedUserId The user ID to unblock
 */
@Serializable
class ElearningUnblockUserRequest(
    private val userId: Int,
    private val unblockedUserId: Int
) : ElearningRequest<ElearningUnblockUserResponse> {
    override val functionName = "core_message_unblock_user"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("unblockeduserid", unblockedUserId.toString())
    }
}

/**
 * Response containing the result of unblocking a user.
 *
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningUnblockUserResponse(
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse