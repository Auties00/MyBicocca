package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve site information from the Moodle web service.
 */
@Serializable
class ElearningGetSiteInfoRequest : ElearningRequest<ElearningGetSiteInfoResponse> {
    override val functionName: String
        get() = "core_webservice_get_site_info"
}

/**
 * Response containing detailed information about the Moodle site and the authenticated user.
 *
 * @property advancedFeatures List of advanced features available on the site.
 * @property downloadFilesEnabled Whether file downloading is enabled (1 = enabled, 0 = disabled).
 * @property firstName The first name of the authenticated user.
 * @property fullName The full name of the authenticated user.
 * @property functions List of web service functions available to the user.
 * @property language The current language code for the site.
 * @property lastName The last name of the authenticated user.
 * @property limitConcurrentLogins Maximum number of concurrent logins allowed.
 * @property mobileCascadingStyleSheetUrl URL for custom mobile CSS styles.
 * @property policyAgreed Whether the user has agreed to the site policy (1 = agreed, 0 = not agreed).
 * @property release The Moodle release version string.
 * @property siteCalendarType The calendar type used by the site.
 * @property siteId The unique identifier of the site.
 * @property siteName The name of the Moodle site.
 * @property siteUrl The base URL of the Moodle site.
 * @property theme The theme currently in use on the site.
 * @property uploadFilesEnabled Whether file uploading is enabled (1 = enabled, 0 = disabled).
 * @property userCalendarType The calendar type preferred by the user.
 * @property userCanManageOwnFiles Whether the user can manage their own files.
 * @property userHomePage The default home page type for the user.
 * @property userId The unique identifier of the authenticated user.
 * @property userIsSiteAdmin Whether the user has site administrator privileges.
 * @property userMaxUploadFileSize Maximum file size the user can upload in bytes.
 * @property username The username of the authenticated user.
 * @property userPictureUrl URL to the user's profile picture.
 * @property userPrivateAccessKey The user's private access key for file access.
 * @property userQuota The user's storage quota in bytes.
 * @property version The Moodle version number.
 */
@Serializable
data class ElearningGetSiteInfoResponse(
    @SerialName("advancedfeatures")
    val advancedFeatures: List<AdvancedFeature>,
    @SerialName("downloadfiles")
    val downloadFilesEnabled: Int,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("fullname")
    val fullName: String,
    @SerialName("functions")
    val functions: List<Function>,
    @SerialName("lang")
    val language: String,
    @SerialName("lastname")
    val lastName: String,
    @SerialName("limitconcurrentlogins")
    val limitConcurrentLogins: Int,
    @SerialName("mobilecssurl")
    val mobileCascadingStyleSheetUrl: String,
    @SerialName("policyagreed")
    val policyAgreed: Int,
    @SerialName("release")
    val release: String,
    @SerialName("sitecalendartype")
    val siteCalendarType: String,
    @SerialName("siteid")
    val siteId: Int,
    @SerialName("sitename")
    val siteName: String,
    @SerialName("siteurl")
    val siteUrl: String,
    @SerialName("theme")
    val theme: String,
    @SerialName("uploadfiles")
    val uploadFilesEnabled: Int,
    @SerialName("usercalendartype")
    val userCalendarType: String,
    @SerialName("usercanmanageownfiles")
    val userCanManageOwnFiles: Boolean,
    @SerialName("userhomepage")
    val userHomePage: Int,
    @SerialName("userid")
    val userId: Int,
    @SerialName("userissiteadmin")
    val userIsSiteAdmin: Boolean,
    @SerialName("usermaxuploadfilesize")
    val userMaxUploadFileSize: Int,
    @SerialName("username")
    val username: String,
    @SerialName("userpictureurl")
    val userPictureUrl: String,
    @SerialName("userprivateaccesskey")
    val userPrivateAccessKey: String,
    @SerialName("userquota")
    val userQuota: Int,
    @SerialName("version")
    val version: String
) : ElearningResponse {

    /**
     * Represents an advanced feature available on the Moodle site.
     *
     * @property name The name of the advanced feature.
     * @property value The value indicating the feature status (typically 1 = enabled, 0 = disabled).
     */
    @Serializable
    data class AdvancedFeature(
        @SerialName("name")
        val name: String,
        @SerialName("value")
        val value: Int
    )

    /**
     * Represents a web service function available to the authenticated user.
     *
     * @property name The name of the web service function.
     * @property version The version of the function.
     */
    @Serializable
    data class Function(
        @SerialName("name")
        val name: String,
        @SerialName("version")
        val version: String
    )
}
