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

/**
 * Response containing the public configuration of the Moodle site.
 * This information is available without authentication and is used for login configuration.
 *
 * @property siteUrl The base URL of the site.
 * @property securedSiteUrl The HTTPS URL of the site (if HTTPS login is enabled).
 * @property siteName The name of the Moodle site.
 * @property guestLoginEnabled Whether guest login is enabled (1 = enabled, 0 = disabled).
 * @property rememberUsername Username remembering setting (0 = No, 1 = Yes, 2 = Optional).
 * @property authenticationLoginViaEmail Whether login via email is enabled.
 * @property registrationAuthMethod Authentication method for user registration.
 * @property forgottenPasswordUrl URL for the forgotten password page.
 * @property authenticationInstructions Instructions displayed on the login page.
 * @property authenticationNoneEnabled Whether authentication bypass is enabled.
 * @property webServicesEnabled Whether web services are enabled.
 * @property mobileWebServiceEnabled Whether the mobile web service is enabled.
 * @property maintenanceEnabled Whether site maintenance mode is enabled.
 * @property maintenanceMessage Message displayed during maintenance.
 * @property logoUrl URL to the site logo.
 * @property compactLogoUrl URL to the compact version of the site logo.
 * @property typeOfLogin The type of login required (app, browser, or embedded).
 * @property launchUrl Single sign-on launch URL.
 * @property mobileCascadingStyleSheetUrl URL for custom mobile CSS theme.
 * @property mobileDisabledFeatures Comma-separated list of disabled features in the mobile app.
 * @property identityProviders List of external identity providers for authentication.
 * @property country Default country code for the site.
 * @property ageDigitalConsentVerification Whether age verification is required.
 * @property supportName Name of the support contact.
 * @property supportEmail Email of the support contact.
 * @property supportAvailability Who can access support.
 * @property supportPage URL to the support page.
 * @property autoDetectLanguage Whether to auto-detect language from browser settings.
 * @property defaultLanguage Default language code for the site.
 * @property languageMenuEnabled Whether the language selection menu is displayed.
 * @property availableLanguages Available languages in the language menu.
 * @property locale The sitewide locale setting.
 * @property mobileMinimumVersion Minimum app version required to access the site.
 * @property mobileIosApplicationId The iOS app store identifier.
 * @property mobileAndroidApplicationId The Android app package identifier.
 * @property mobileSetupLink URL to the app download page.
 * @property mobileQuickResponseCodeType Type of QR code functionality enabled.
 * @property warnings List of warnings returned by the web service.
 * @property showLoginForm Whether to display the default login form.
 */
@Serializable
data class ElearningGetPublicConfigResponse(
    @SerialName("wwwroot")
    val siteUrl: String,
    @SerialName("httpswwwroot")
    val securedSiteUrl: String,
    @SerialName("sitename")
    val siteName: String,
    @SerialName("guestlogin")
    val guestLoginEnabled: Int,
    @SerialName("rememberusername")
    val rememberUsername: Int,
    @SerialName("authloginviaemail")
    val authenticationLoginViaEmail: Int,
    @SerialName("registerauth")
    val registrationAuthMethod: String,
    @SerialName("forgottenpasswordurl")
    val forgottenPasswordUrl: String,
    @SerialName("authinstructions")
    val authenticationInstructions: String,
    @SerialName("authnoneenabled")
    val authenticationNoneEnabled: Int,
    @SerialName("enablewebservices")
    val webServicesEnabled: Int,
    @SerialName("enablemobilewebservice")
    val mobileWebServiceEnabled: Int,
    @SerialName("maintenanceenabled")
    val maintenanceEnabled: Int,
    @SerialName("maintenancemessage")
    val maintenanceMessage: String,
    @SerialName("logourl")
    val logoUrl: String? = null,
    @SerialName("compactlogourl")
    val compactLogoUrl: String? = null,
    @SerialName("typeoflogin")
    val typeOfLogin: TypeOfLogin,
    @SerialName("launchurl")
    val launchUrl: String? = null,
    @SerialName("mobilecssurl")
    val mobileCascadingStyleSheetUrl: String? = null,
    @SerialName("tool_mobile_disabledfeatures")
    val mobileDisabledFeatures: String? = null,
    @SerialName("identityproviders")
    val identityProviders: List<IdentityProvider>? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("agedigitalconsentverification")
    val ageDigitalConsentVerification: Boolean? = null,
    @SerialName("supportname")
    val supportName: String? = null,
    @SerialName("supportemail")
    val supportEmail: String? = null,
    @SerialName("supportavailability")
    val supportAvailability: SupportAvailability? = null,
    @SerialName("supportpage")
    val supportPage: String? = null,
    @SerialName("autolang")
    val autoDetectLanguage: Int? = null,
    @SerialName("lang")
    val defaultLanguage: String? = null,
    @SerialName("langmenu")
    val languageMenuEnabled: Int? = null,
    @SerialName("langlist")
    val availableLanguages: String? = null,
    @SerialName("locale")
    val locale: String? = null,
    @SerialName("tool_mobile_minimumversion")
    val mobileMinimumVersion: String? = null,
    @SerialName("tool_mobile_iosappid")
    val mobileIosApplicationId: String? = null,
    @SerialName("tool_mobile_androidappid")
    val mobileAndroidApplicationId: String? = null,
    @SerialName("tool_mobile_setuplink")
    val mobileSetupLink: String? = null,
    @SerialName("tool_mobile_qrcodetype")
    val mobileQuickResponseCodeType: QuickResponseCodeType? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning>? = null,
    @SerialName("showloginform")
    val showLoginForm: Int? = null,
) : ElearningResponse {
    /**
     * Defines the default home page type for users.
     *
     * @property value The numeric value representing the home page type.
     */
    @Serializable
    enum class UserHomePage(val value: Int) {
        /** Site home page. */
        @SerialName("0")
        SITE(0),

        /** User dashboard. */
        @SerialName("1")
        DASHBOARD(1),

        /** My courses page. */
        @SerialName("3")
        MY_COURSES(3),

        /** A custom URL. */
        @SerialName("4")
        CUSTOM_URL(4),
    }

    /**
     * Defines who can access the support functionality.
     *
     * @property value The numeric value representing the availability level.
     */
    @Serializable
    enum class SupportAvailability(val value: Int) {
        /** Support is disabled. */
        @SerialName("0")
        DISABLED(0),

        /** Support is available only to authenticated users. */
        @SerialName("1")
        AUTHENTICATED_USERS(1),

        /** Support is available to anyone. */
        @SerialName("2")
        ANYONE(2),
    }

    /**
     * Defines the type of QR code functionality available.
     *
     * @property value The numeric value representing the QR code type.
     */
    @Serializable
    enum class QuickResponseCodeType(val value: Int) {
        /** QR code functionality is disabled. */
        @SerialName("0")
        DISABLED(0),

        /** QR code contains a URL. */
        @SerialName("1")
        URL(1),

        /** QR code is used for login. */
        @SerialName("2")
        LOGIN(2),
    }

    /**
     * Defines the type of login required for authentication.
     *
     * @property value The numeric value representing the login type.
     */
    @Serializable
    enum class TypeOfLogin(val value: Int) {
        /** Login is handled within the app. */
        @SerialName("1")
        APP(1),

        /** Single sign-on in an external browser window is required. */
        @SerialName("2")
        BROWSER(2),

        /** Single sign-on in an embedded browser is required. */
        @SerialName("3")
        EMBEDDED(3),
    }

    /**
     * Represents an external identity provider for authentication.
     *
     * @property name The display name of the identity provider.
     * @property iconUrl URL to the provider's icon.
     * @property url The authentication URL for the provider.
     */
    @Serializable
    data class IdentityProvider(
        @SerialName("name")
        val name: String,
        @SerialName("iconurl")
        val iconUrl: String,
        @SerialName("url")
        val url: String,
    )
}
