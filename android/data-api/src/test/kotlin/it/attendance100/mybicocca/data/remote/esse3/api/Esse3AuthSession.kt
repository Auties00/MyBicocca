package it.attendance100.mybicocca.data.remote.esse3.api

data class Esse3AuthSession(
    val authToken: String,
    val internalAuthToken: String?,
    val jwt: String?,
    val userId: String,
    val fiscalCode: String?
)
