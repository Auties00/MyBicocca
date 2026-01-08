package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing the user's profile and careers.
 */
@Serializable
data class BicoccappUserProfile(
    /**
     * User personal information.
     */
    @SerialName("user")
    val user: BicoccappUserProfileUser,

    /**
     * List of associated academic careers.
     */
    @SerialName("careers")
    val careers: List<BicoccappUserProfileCareersInner> = emptyList()
)

/**
 * User personal details.
 */
@Serializable
data class BicoccappUserProfileUser(
    /**
     * Application user ID.
     */
    @SerialName("appuser_id")
    val appUserId: Int,

    /**
     * Person ID (internal system identifier).
     */
    @SerialName("personId")
    val personId: Int,

    /**
     * User ID (often same as matricola or similar).
     */
    @SerialName("userId")
    val userId: String,

    /**
     * Fiscal Code (Codice Fiscale).
     */
    @SerialName("fiscalCode")
    val fiscalCode: String,

    /**
     * First name.
     */
    @SerialName("name")
    val firstName: String,

    /**
     * Last name.
     */
    @SerialName("surname")
    val lastName: String,

    /**
     * Mobile phone number.
     */
    @SerialName("mobile")
    val mobilePhone: String? = null,

    /**
     * Email address.
     */
    @SerialName("email")
    val email: String,

    /**
     * Photo ID.
     */
    @SerialName("fotoId")
    val photoId: Int? = null,

    /**
     * Base64 encoded photo or URL.
     */
    @SerialName("photo")
    val photo: String? = null,

    /**
     * Indicates if the user has a calendar configured.
     */
    @SerialName("has_calendar")
    val hasCalendar: Boolean
)

/**
 * Represents a student's career entry.
 */
@Serializable
data class BicoccappUserProfileCareersInner(
    /**
     * Enrollment ID (Matricola numeric ID).
     */
    @SerialName("matricId")
    val enrollmentId: Int,

    /**
     * Indicates if this career is currently selected.
     */
    @SerialName("selected")
    val isSelected: Boolean,

    /**
     * Indicates if it uses old coding.
     */
    @SerialName("oldCode")
    val isOldCode: Boolean,

    /**
     * Enrollment code (Matricola string).
     */
    @SerialName("matricCode")
    val enrollmentCode: String,

    /**
     * Student ID associated with this career.
     */
    @SerialName("studentId")
    val studentId: Int,

    /**
     * Type of course code (e.g., L2).
     */
    @SerialName("typeCourseCode")
    val typeCourseCode: String,

    /**
     * Description of the course type.
     */
    @SerialName("typeCourseDescr")
    val typeCourseDescription: String,

    /**
     * CDS ID.
     */
    @SerialName("cdsId")
    val degreeCourseId: Int,

    /**
     * CDS Description.
     */
    @SerialName("cdsDescr")
    val degreeCourseDescription: String,

    /**
     * Student enrollment status code.
     */
    @SerialName("statusStudentCode")
    val status: String,


    /**
     * Student enrollment status.
     */
    @SerialName("statusStudentDescr")
    val statusDescription: String,

    /**
     * Academic year.
     */
    @SerialName("yearId")
    val yearId: Int
)