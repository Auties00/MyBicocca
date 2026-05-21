package it.attendance100.mybicocca.data.remote.bicoccapp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Response containing points of interest on the campus.
 */
@Serializable
internal data class BicoccappPointOfInterestsResponse(
    /**
     * Map data containing filters and locations.
     */
    @SerialName("maps")
    val maps: BicoccappPointOfInterestsMaps
)

/**
 * Contains map filters and locations for points of interest.
 */
@Serializable
data class BicoccappPointOfInterestsMaps(
    /**
     * List of available filters for the map.
     */
    @SerialName("filters")
    val filters: List<BicoccappMapFilter> = emptyList(),

    /**
     * List of locations on the map.
     */
    @SerialName("map_locations")
    val locations: List<BicoccappMapLocation> = emptyList()
)

/**
 * Filter definition for map locations.
 */
@Serializable
data class BicoccappMapFilter(
    /**
     * Type identifier for the filter.
     */
    @SerialName("type")
    val type: String,
    /**
     * Color associated with the filter (hex string).
     */
    @SerialName("color")
    val color: String
)

/**
 * A specific location on the map.
 */
@Serializable
data class BicoccappMapLocation(
    /**
     * Name of the location.
     */
    @SerialName("name")
    val name: String,
    /**
     * Latitude coordinate.
     */
    @SerialName("latitude")
    val latitude: String,
    /**
     * Longitude coordinate.
     */
    @SerialName("longitude")
    val longitude: String,
    /**
     * Description of the location.
     */
    @SerialName("description")
    val description: String,
    /**
     * Type category of the location.
     */
    @SerialName("type")
    val type: String
)

/**
 * Response containing teacher details.
 */
@Serializable
internal data class BicoccappTeacherDetailsResponse(
    /**
     * Teacher object containing profile details.
     */
    @SerialName("teacher")
    val teacher: BicoccappTeacherDetails
)

/**
 * Represents a university teacher/professor.
 */
@Serializable
data class BicoccappTeacherDetails(
    /**
     * Title (e.g., Prof., Dott.).
     */
    @SerialName("title")
    val title: String,

    /**
     * First name.
     */
    @SerialName("name")
    val name: String,

    /**
     * Last name.
     */
    @SerialName("surname")
    val surname: String,

    /**
     * Institutional email address.
     */
    @SerialName("email")
    val email: String,

    /**
     * Alternate email address.
     */
    @SerialName("email_alternate")
    val emailAlternate: String? = null,

    /**
     * Unique code for the teacher.
     */
    @SerialName("teacherCode")
    val teacherCode: String,

    /**
     * Gender of the teacher.
     */
    @SerialName("gender")
    val gender: BicoccappGender,

    /**
     * Unique key identifier.
     */
    @SerialName("teacherKey")
    val teacherKey: String,

    /**
     * Reception hours info.
     */
    @SerialName("receivesOn")
    val receptionHours: String? = null,

    /**
     * List of roles held by the teacher.
     */
    @SerialName("roles")
    val roles: List<BicoccappTeacherRole> = emptyList(),

    /**
     * List of phone numbers.
     */
    @SerialName("phones")
    val phones: List<BicoccappTeacherPhone> = emptyList(),

    /**
     * List of rooms associated with the teacher.
     */
    @SerialName("rooms")
    val rooms: List<BicoccappTeacherRoom> = emptyList(),

    /**
     * List of offices associated with the teacher.
     */
    @SerialName("offices")
    val offices: List<BicoccappTeacherOffice> = emptyList()
)

/**
 * Role of a teacher.
 */
@Serializable
data class BicoccappTeacherRole(
    /**
     * Description of the role.
     */
    @SerialName("roleDescr")
    val description: String
)

/**
 * Phone contact of a teacher.
 */
@Serializable
data class BicoccappTeacherPhone(
    /**
     * Office phone number.
     */
    @SerialName("officePhone")
    val officePhone: String
)

/**
 * Room location for a teacher.
 */
@Serializable
data class BicoccappTeacherRoom(
    /**
     * Location description of the room.
     */
    @SerialName("roomPlace")
    val location: String
)

/**
 * Office location for a teacher.
 */
@Serializable
data class BicoccappTeacherOffice(
    /**
     * Description of the office.
     */
    @SerialName("officeDescription")
    val description: String
)

@Serializable(with = BicoccappTeacherSerializer::class)
data class BicoccappTeacher(
    val key: String,
    val code: String,
    val fullName: String,
    val email: String
)