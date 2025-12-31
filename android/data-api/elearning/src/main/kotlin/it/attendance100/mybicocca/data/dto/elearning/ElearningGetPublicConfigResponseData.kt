package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum constants that define default user home page.
 */
@Serializable
enum class ElearningCoreSiteInfoUserHomepage(val value: Int) {
    @SerialName("0")
    HOMEPAGE_SITE(0), // Site home.
    @SerialName("1")
    HOMEPAGE_MY(1), // Dashboard.
    @SerialName("3")
    HOMEPAGE_MYCOURSES(3), // My courses.
    @SerialName("4")
    HOMEPAGE_URL(4), // A custom URL.
}

/**
 * Possible values for 'supportavailability' config.
 */
@Serializable
enum class ElearningCoreSiteConfigSupportAvailability(val value: Int) {
    @SerialName("0")
    Disabled(0),
    @SerialName("1")
    Authenticated(1),
    @SerialName("2")
    Anyone(2),
}

/**
 * QR Code type enumeration.
 */
@Serializable
enum class ElearningCoreSiteQRCodeType(val value: Int) {
    @SerialName("0")
    QR_CODE_DISABLED(0), // QR code disabled value
    @SerialName("1")
    QR_CODE_URL(1), // QR code type URL value
    @SerialName("2")
    QR_CODE_LOGIN(2), // QR code type login value
}

/**
 * The type of login. 1 for app, 2 for browser, 3 for embedded.
 */
@Serializable
enum class ElearningTypeOfLogin(val value: Int) {
    @SerialName("1")
    APP(1),
    @SerialName("2")
    BROWSER(2), // SSO in browser window is required.
    @SerialName("3")
    EMBEDDED(3), // SSO in embedded browser is required.
}

/**
 * Identity provider.
 */
@Serializable
data class ElearningCoreSiteIdentityProvider(
    @SerialName("name")
    val name: String, // The identity provider name.
    @SerialName("iconurl")
    val iconUrl: String, // The icon URL for the provider.
    @SerialName("url")
    val url: String, // The URL of the provider.
)

/**
 * Result of WS tool_mobile_get_public_config.
 */
@Serializable
data class ElearningGetPublicConfigResponseData(
    @SerialName("wwwroot")
    val wwwroot: String, // Site URL.
    @SerialName("httpswwwroot")
    val httpswwwroot: String, // Site https URL (if httpslogin is enabled).
    @SerialName("sitename")
    val sitename: String, // Site name.
    @SerialName("guestlogin")
    val guestlogin: Int, // Whether guest login is enabled.
    @SerialName("rememberusername")
    val rememberusername: Int, // Values: 0 for No, 1 for Yes, 2 for optional.
    @SerialName("authloginviaemail")
    val authloginviaemail: Int, // Whether log in via email is enabled.
    @SerialName("registerauth")
    val registerauth: String, // Authentication method for user registration.
    @SerialName("forgottenpasswordurl")
    val forgottenpasswordurl: String, // Forgotten password URL.
    @SerialName("authinstructions")
    val authinstructions: String, // Authentication instructions.
    @SerialName("authnoneenabled")
    val authnoneenabled: Int, // Whether auth none is enabled.
    @SerialName("enablewebservices")
    val enablewebservices: Int, // Whether Web Services are enabled.
    @SerialName("enablemobilewebservice")
    val enablemobilewebservice: Int, // Whether the Mobile service is enabled.
    @SerialName("maintenanceenabled")
    val maintenanceenabled: Int, // Whether site maintenance is enabled.
    @SerialName("maintenancemessage")
    val maintenancemessage: String, // Maintenance message.
    @SerialName("logourl")
    val logourl: String? = null, // The site logo URL.
    @SerialName("compactlogourl")
    val compactlogourl: String? = null, // The site compact logo URL.
    @SerialName("typeoflogin")
    val typeoflogin: ElearningTypeOfLogin, // The type of login. 1 for app, 2 for browser, 3 for embedded.
    @SerialName("launchurl")
    val launchurl: String? = null, // SSO login launch URL.
    @SerialName("mobilecssurl")
    val mobilecssurl: String? = null, // Mobile custom CSS theme.
    @SerialName("tool_mobile_disabledfeatures")
    val toolMobileDisabledFeatures: String? = null, // Disabled features in the app.
    @SerialName("identityproviders")
    val identityproviders: List<ElearningCoreSiteIdentityProvider>? = null, // Identity providers.
    @SerialName("country")
    val country: String? = null, // Default site country.
    @SerialName("agedigitalconsentverification")
    val agedigitalconsentverification: Boolean? = null, // Whether age digital consent verification is enabled.
    @SerialName("supportname")
    val supportname: String? = null, // Site support contact name (only if age verification is enabled).
    @SerialName("supportemail")
    val supportemail: String? = null, // Site support contact email (only if age verification is enabled).
    @SerialName("supportavailability")
    val supportavailability: ElearningCoreSiteConfigSupportAvailability? = null,
    @SerialName("supportpage")
    val supportpage: String? = null, // Site support contact url.
    @SerialName("autolang")
    val autolang: Int? = null, // Whether to detect default language from browser setting.
    @SerialName("lang")
    val lang: String? = null, // Default language for the site.
    @SerialName("langmenu")
    val langmenu: Int? = null, // Whether the language menu should be displayed.
    @SerialName("langlist")
    val langlist: String? = null, // Languages on language menu.
    @SerialName("locale")
    val locale: String? = null, // Sitewide locale.
    @SerialName("tool_mobile_minimumversion")
    val toolMobileMinimumVersion: String? = null, // Minimum required version to access.
    @SerialName("tool_mobile_iosappid")
    val toolMobileIosAppId: String? = null, // IOS app's unique identifier.
    @SerialName("tool_mobile_androidappid")
    val toolMobileAndroidAppId: String? = null, // Android app's unique identifier.
    @SerialName("tool_mobile_setuplink")
    val toolMobileSetupLink: String? = null, // App download page.
    @SerialName("tool_mobile_qrcodetype")
    val toolMobileQrCodeType: ElearningCoreSiteQRCodeType? = null,
    @SerialName("warnings")
    val warnings: List<ElearningCoreWSExternalWarning>? = null,
    @SerialName("showloginform")
    val showloginform: Int? = null, // @since 4.5. Display default login form.
) : ElearningResponseData

/**
 * External warning from web service.
 */
@Serializable
data class ElearningCoreWSExternalWarning(
    @SerialName("item")
    val item: String? = null,
    @SerialName("itemid")
    val itemid: Int? = null,
    @SerialName("warningcode")
    val warningcode: String,
    @SerialName("message")
    val message: String,
)