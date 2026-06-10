package it.attendance100.mybicocca.data.auth

/**
 * Per-account session material cached by SessionCache; each field is null until the
 * corresponding login has produced it.
 *
 * @property wsToken Moodle web-service token carried by every Moodle WS call.
 * @property moodleSessionCookie Moodle browser-session cookie (`MoodleSession`), baked into the
 *   Moodle client for the non-WS surfaces such as file downloads and embedded web views.
 * @property jwt Esse3 REST JWT minted at login.
 * @property esse3LegacyCookies Esse3 web-SSO (legacy scrape) session cookies in the line-based
 *   encoding defined by SessionManager; null until the first feature needing the legacy
 *   session mints them via SAML.
 */
data class SessionTokens(
    val wsToken: String? = null,
    val moodleSessionCookie: String? = null,
    val jwt: String? = null,
    val esse3LegacyCookies: String? = null,
)
