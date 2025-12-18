package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * # BicoccApp Campus API
 *
 * This interface provides endpoints for accessing campus-related information,
 * including physical locations, points of interest, and faculty directory.
 *
 * ## Features
 *
 * - **Campus Map:** Buildings, departments, and facilities locations
 * - **Points of Interest:** Cafeterias, libraries, study rooms, services
 * - **Faculty Directory:** Teacher profiles and contact information
 *
 * ## Campus Locations
 *
 * The University of Milano-Bicocca has multiple campus areas:
 * - **U1-U7:** Main campus buildings in Piazza dell'Ateneo Nuovo
 * - **U8-U9:** Science and technology buildings
 * - **U14:** Medicine and healthcare facilities
 * - **External locations:** Partner institutions and satellite campuses
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Fetch all campus points of interest
 * val pois = campusApi.getPointsOfInterest()
 *
 * // Get teacher information by email
 * val teacher = campusApi.getTeacherByEmail(
 *     email = "mario.rossi@unimib.it"
 * )
 * ```
 */
interface BicoccappCampusApi {

    /**
     * Retrieves all campus points of interest (POIs).
     *
     * This endpoint returns a comprehensive list of locations and facilities
     * across all university campuses. The data can be used to:
     * - Display interactive campus maps
     * - Provide navigation and wayfinding
     * - Show facility operating hours and availability
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `point_of_interests`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse] object contains categorized location data:
     *
     * ### Buildings
     * - Building code (e.g., "U6", "U7")
     * - Full name and description
     * - Address and GPS coordinates
     * - Accessibility information
     *
     * ### Facilities
     * - **Cafeterias:** Operating hours, menu links
     * - **Libraries:** Opening hours, study room availability
     * - **Computer Labs:** Location, capacity, software available
     * - **Study Rooms:** Booking information, capacity
     *
     * ### Services
     * - **Administrative Offices:** Segreteria, URP
     * - **Student Services:** Counseling, career services
     * - **Healthcare:** Infirmary, emergency contacts
     *
     * ### Departments
     * - Department name and code
     * - Building location
     * - Contact information
     * - Associated degree programs
     *
     * ## Map Integration
     * Each POI includes:
     * - `latitude` / `longitude`: For map marker placement
     * - `floor`: Building floor number
     * - `room`: Specific room identifier
     * - `iconType`: Suggested map marker icon
     *
     * ## Caching Recommendations
     * POI data changes infrequently:
     * - Cache for 7 days under normal conditions
     * - Refresh on app launch (background)
     * - Force refresh on user request
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse] with all campus
     *         locations organized by category. Includes coordinates for
     *         map display and metadata for filtering/searching.
     *
     * @see getTeacherByEmail For finding teacher office locations
     */
    @GET("point_of_interests")
    suspend fun getPointsOfInterest(): Response<BicoccappPointOfInterestsResponse>

    /**
     * Retrieves detailed information about a specific teacher.
     *
     * This endpoint returns the full profile of a university faculty member,
     * including their academic role, office location, research interests,
     * and contact information.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `teacher`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherResponse] object contains:
     *
     * ### Personal Information
     * - `name`: Full name with academic title
     * - `email`: Institutional email address
     * - `phone`: Office phone number
     * - `photoUrl`: Profile picture URL
     *
     * ### Academic Information
     * - `role`: Position (Professor, Researcher, Teaching Assistant)
     * - `department`: Associated department
     * - `researchAreas`: List of research interests
     * - `publications`: Link to publication list
     *
     * ### Office Information
     * - `building`: Campus building code
     * - `floor`: Floor number
     * - `room`: Room number
     * - `officeHours`: Reception hours schedule
     *
     * ### Contact Preferences
     * - `preferredContactMethod`: Email, phone, or appointment
     * - `appointmentUrl`: Link to booking system
     * - `acceptsAppointments`: Whether teacher accepts student meetings
     *
     * ## Use Cases
     * - Display teacher profile in course details
     * - Enable students to contact professors
     * - Show office location on campus map
     * - Schedule appointments with faculty
     *
     * ## Error Handling
     * - **404 Not Found:** Teacher email not registered in the system
     * - **400 Bad Request:** Invalid email format provided
     *
     * @param email The institutional email address of the teacher to look up.
     *              Must be a valid `@unimib.it` email address.
     *              Example: `"mario.rossi@unimib.it"`
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherResponse] with the complete profile.
     *         Returns 404 if no teacher matches the provided email.
     *
     * @see getPointsOfInterest For campus building locations
     */
    @GET("teacher")
    suspend fun getTeacherByEmail(
        @Query("teacherEmail") email: String
    ): Response<BicoccappTeacherResponse>
}
