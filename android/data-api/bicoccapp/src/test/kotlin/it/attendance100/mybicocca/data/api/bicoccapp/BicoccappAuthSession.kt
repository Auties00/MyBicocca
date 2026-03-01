package it.attendance100.mybicocca.data.api.bicoccapp

data class BicoccappAuthSession(
    val accessToken: String,
    val client: String,
    val uid: String,
    val fiscalCode: String,
    val matricId: Long
)