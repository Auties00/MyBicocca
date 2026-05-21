package it.attendance100.mybicocca.data.remote.bicoccapp.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappUserProfile
import kotlinx.serialization.json.Json

/**
 * API client for user profile operations.
 */
class BicoccappProfileApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves the user's profile information.
     *
     * @param fiscalCode User's fiscal code (codice fiscale).
     * @return User profile with personal information and careers.
     */
    suspend fun getProfile(
        fiscalCode: String
    ): BicoccappUserProfile {
        return executeJsonGet("/user_profile") {
            parameter("fiscalCode", fiscalCode)
        }
    }
}
