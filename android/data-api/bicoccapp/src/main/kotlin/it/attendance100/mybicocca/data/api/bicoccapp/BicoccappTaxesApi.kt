package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTaxesResponse

/**
 * API for managing user taxes.
 */
interface BicoccappTaxesApi {
    /**
     * Retrieves the student's tuition fee payment history.
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @return Fee summary with installments, payment status, and deadlines.
     */
    @GET("user_fees")
    suspend fun getTaxes(
        @Query("personId") personId: String,
        @Query("matricId") enrollmentId: String
    ): BicoccappTaxesResponse
}
