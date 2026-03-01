package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTax
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTaxesResponse
import kotlinx.serialization.json.Json

/**
 * API client for tax/fee-related operations.
 */
class BicoccappTaxesApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves user's tax/fee information.
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @return Tax/fee information.
     */
    suspend fun getTaxes(
        personId: String,
        enrollmentId: String
    ): List<BicoccappTax> {
        val response = executeJsonGet<BicoccappTaxesResponse>("/user_fees") {
            parameter("personId", personId)
            parameter("matricId", enrollmentId)
        }
        return response.career.fees
    }
}
