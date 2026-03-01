package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsMaps
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacher
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherDetails
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherDetailsResponse
import kotlinx.serialization.json.Json

/**
 * API client for campus-related operations.
 */
class BicoccappCampusApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves all campus points of interest (buildings, facilities, services).
     *
     * @return Points of interest with filters and locations.
     */
    suspend fun getPointsOfInterest(): BicoccappPointOfInterestsMaps {
        val response = executeJsonGet<BicoccappPointOfInterestsResponse>("/point_of_interests")
        return response.maps
    }

    /**
     * Retrieves a teacher's profile by their institutional email.
     *
     * @param teacher the teacher's reference
     * @return Teacher profile details.
     */
    suspend fun getTeacherDetails(teacher: BicoccappTeacher): BicoccappTeacherDetails {
        return getTeacherDetails(teacher.email)
    }

    /**
     * Retrieves a teacher's profile by their institutional email.
     *
     * @param email Teacher's @unimib.it email address.
     * @return Teacher profile details.
     */
    suspend fun getTeacherDetails(email: String): BicoccappTeacherDetails {
        val response = executeJsonGet<BicoccappTeacherDetailsResponse>("/teacher") {
            parameter("teacherEmail", email)
        }
        return response.teacher
    }

    /**
     * Sends an appointment request to a teacher.
     *
     * @param teacherKey Teacher's identifier (typically their email).
     * @param appUserId Student's app user numeric identifier.
     * @param messageBody Message explaining the appointment purpose.
     * @throws ApiRequestException if the appointment cannot be booked
     */
    suspend fun sendAppointmentRequest(
        teacherKey: String,
        appUserId: String,
        messageBody: String
    ): Boolean {
        val response = executePost("/messages/appointment") {
            setBody(FormDataContent(Parameters.build {
                append("teacher_key", teacherKey)
                append("student_id", appUserId)
                append("message_body", messageBody)
            }))
        }
        return response.status.isSuccess()
    }
}
