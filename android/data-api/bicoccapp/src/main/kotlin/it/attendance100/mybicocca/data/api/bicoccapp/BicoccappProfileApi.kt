package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserProfile

/**
 * API for managing user profile.
 */
interface BicoccappProfileApi {
    /**
     * Retrieves the user's profile information.
     *
     * @param fiscalCode User's fiscal code (codice fiscale).
     * @return Profile with personal data, contact info, and academic identifiers.
     */
    @GET("user_profile")
    suspend fun getProfile(
        @Query("fiscalCode") fiscalCode: String
    ): BicoccappUserProfile
}
