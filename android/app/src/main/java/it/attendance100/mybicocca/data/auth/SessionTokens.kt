package it.attendance100.mybicocca.data.auth

data class SessionTokens(
    val wsToken: String? = null,
    val moodleSessionCookie: String? = null,
    val jwt: String? = null,
)
