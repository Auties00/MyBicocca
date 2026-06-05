package it.attendance100.mybicocca.data.auth

data class SessionTokens(
    val wsToken: String? = null,
    val moodleSessionCookie: String? = null,
    val jwt: String? = null,
    // Serialized Esse3 web-SSO (legacy scrape) session cookies — see SessionCache
    // for the encoding. Null until the first certificati access mints them via SAML.
    val esse3LegacyCookies: String? = null,
)
