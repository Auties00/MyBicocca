package it.attendance100.mybicocca.data.api.esse3

data class Esse3AuthSession(
    val authToken: String,
    val internalAuthToken: String?,
    val jwt: String?,
    val userId: String,
    val fiscalCode: String?
)
