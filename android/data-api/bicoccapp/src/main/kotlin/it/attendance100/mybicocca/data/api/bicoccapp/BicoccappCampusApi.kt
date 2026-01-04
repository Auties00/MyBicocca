package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherResponse

/**
 * Campus API for locations, facilities, and faculty directory.
 */
interface BicoccappCampusApi {

    /**
     * Retrieves all campus points of interest (buildings, facilities, services).
     *
     * @return Categorized locations with coordinates and metadata.
     */
    @GET("point_of_interests")
    suspend fun getPointsOfInterest(): Response<BicoccappPointOfInterestsResponse>

    /**
     * Retrieves a teacher's profile by their institutional email.
     *
     * @param email Teacher's @unimib.it email address.
     * @return Teacher profile with contact info and office location, or 404 if not found.
     */
    @GET("teacher")
    suspend fun getTeacherByEmail(
        @Query("teacherEmail") email: String
    ): Response<BicoccappTeacherResponse>
}
