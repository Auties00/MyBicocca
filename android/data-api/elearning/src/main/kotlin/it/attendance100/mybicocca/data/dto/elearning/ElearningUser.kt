package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * User profile fields.
 */
@Serializable
data class ElearningUserProfile(
    @SerialName("id")
    val id: Int,
    @SerialName("username")
    val username: String? = null,
    @SerialName("firstname")
    val firstName: String? = null,
    @SerialName("lastname")
    val lastName: String? = null,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("department")
    val department: String? = null,
    @SerialName("institution")
    val institution: String? = null,
    @SerialName("idnumber")
    val idNumber: String? = null,
    @SerialName("firstaccess")
    val firstAccess: Long? = null,
    @SerialName("lastaccess")
    val lastAccess: Long? = null,
    @SerialName("lastcourseaccess")
    val lastCourseAccess: Long? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("descriptionformat")
    val descriptionFormat: Int? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("profileimageurlsmall")
    val profileImageUrlSmall: String? = null,
    @SerialName("profileimageurl")
    val profileImageUrl: String? = null,
    @SerialName("customfields")
    val customFields: List<ElearningUserCustomField>? = null,
    @SerialName("groups")
    val groups: List<ElearningUserGroup>? = null,
    @SerialName("roles")
    val roles: List<ElearningUserRole>? = null,
    @SerialName("enrolledcourses")
    val enrolledCourses: List<ElearningUserEnrolledCourse>? = null
)

@Serializable
data class ElearningUserCustomField(
    @SerialName("type")
    val type: String? = null,
    @SerialName("value")
    val value: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("shortname")
    val shortName: String? = null
)

@Serializable
data class ElearningUserGroup(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("descriptionformat")
    val descriptionFormat: Int? = null
)

@Serializable
data class ElearningUserRole(
    @SerialName("roleid")
    val roleId: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("sortorder")
    val sortOrder: Int? = null
)

@Serializable
data class ElearningUserEnrolledCourse(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null
)

/**
 * Preferences for users.
 */
@Serializable
data class ElearningUserPreference(
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: String? = null
)