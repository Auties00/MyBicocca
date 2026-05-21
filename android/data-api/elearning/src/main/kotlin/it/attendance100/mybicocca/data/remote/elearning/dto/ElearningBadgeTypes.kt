package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve badges awarded to a user.
 *
 * @property userId Optional user identifier. If not provided, returns badges for the current user.
 * @property courseId Optional course identifier to filter badges by course.
 * @property page The page number for pagination (0-based).
 * @property perPage The number of badges per page (0 for all).
 * @property search Optional search string to filter badges by name.
 * @property onlyPublic Whether to only return public badges.
 */
@Serializable
class ElearningGetUserBadgesRequest(
    private val userId: Int? = null,
    private val courseId: Int? = null,
    private val page: Int = 0,
    private val perPage: Int = 0,
    private val search: String? = null,
    private val onlyPublic: Boolean = false
) : ElearningRequest<ElearningGetUserBadgesResponse> {
    override val functionName = "core_badges_get_user_badges"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
        courseId?.let { formData.append("courseid", it.toString()) }
        formData.append("page", page.toString())
        formData.append("perpage", perPage.toString())
        search?.let { formData.append("search", it) }
        if (onlyPublic) formData.append("onlypublic", "1")
    }
}

/**
 * Response containing a list of user badges.
 *
 * @property badges The list of badges awarded to the user.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningGetUserBadgesResponse(
    @SerialName("badges")
    val badges: List<ElearningUserBadge> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents a badge awarded to a user.
 *
 * @property id The unique identifier of the badge.
 * @property name The name of the badge.
 * @property description The description of the badge.
 * @property timeCreated The Unix timestamp when the badge was created.
 * @property timeModified The Unix timestamp when the badge was last modified.
 * @property userCreated The user identifier of who created the badge.
 * @property userModified The user identifier of who last modified the badge.
 * @property issuerName The name of the badge issuer.
 * @property issuerUrl The URL of the badge issuer.
 * @property issuerContact The contact information of the badge issuer.
 * @property expireDate The Unix timestamp when the badge expires.
 * @property expirePeriod The period in seconds after which the badge expires.
 * @property type The type of badge (1=site badge, 2=course badge).
 * @property courseId The course identifier if this is a course badge.
 * @property issuedId The unique identifier of the badge issue record.
 * @property uniqueHash The unique hash identifying this badge award.
 * @property dateIssued The Unix timestamp when the badge was issued to the user.
 * @property dateExpire The Unix timestamp when the user's badge expires.
 * @property visible Whether the badge is visible in the user's profile.
 * @property email The email address associated with this badge award.
 * @property version The version of the badge.
 * @property language The language of the badge.
 * @property badgeUrl The URL to the badge image.
 */
@Serializable
data class ElearningUserBadge(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("usercreated")
    val userCreated: Int? = null,
    @SerialName("usermodified")
    val userModified: Int? = null,
    @SerialName("issuername")
    val issuerName: String? = null,
    @SerialName("issuerurl")
    val issuerUrl: String? = null,
    @SerialName("issuercontact")
    val issuerContact: String? = null,
    @SerialName("expiredate")
    val expireDate: Long? = null,
    @SerialName("expireperiod")
    val expirePeriod: Long? = null,
    @SerialName("type")
    val type: Int? = null,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("issuedid")
    val issuedId: Int? = null,
    @SerialName("uniquehash")
    val uniqueHash: String? = null,
    @SerialName("dateissued")
    val dateIssued: Long? = null,
    @SerialName("dateexpire")
    val dateExpire: Long? = null,
    @SerialName("visible")
    val visible: Int? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("version")
    val version: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("badgeurl")
    val badgeUrl: String? = null
)
