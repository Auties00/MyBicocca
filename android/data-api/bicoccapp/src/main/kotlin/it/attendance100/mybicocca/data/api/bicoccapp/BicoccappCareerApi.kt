package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.*
import io.ktor.client.request.*
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCareerRegistration
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserCareerCareer
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserCareerResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserRegistrationsResponse
import kotlinx.serialization.json.Json

/**
 * API client for academic career operations.
 */
class BicoccappCareerApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves the student's academic career details including averages and statistics.
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @param studentId Student record identifier.
     * @return Career details with averages and stats.
     */
    suspend fun getCareer(
        personId: String,
        enrollmentId: String,
        studentId: String
    ): BicoccappUserCareerCareer {
        val response = executeJsonGet<BicoccappUserCareerResponse>("/user_career") {
            parameter("personId", personId)
            parameter("matricId", enrollmentId)
            parameter("studentId", studentId)
        }
        return response.career
    }

    /**
     * Retrieves the student's enrollment history across academic years.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Registration history.
     */
    suspend fun getRegistrations(
        enrollmentId: String
    ): List<BicoccappCareerRegistration> {
        val response = executeJsonGet<BicoccappUserRegistrationsResponse>("/user_registrations") {
            parameter("matricId", enrollmentId)
        }
        return response.career.registrations
    }
}
