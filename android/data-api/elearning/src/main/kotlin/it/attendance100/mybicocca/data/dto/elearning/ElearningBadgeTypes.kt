package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a badge.
 */
@Serializable
data class ElearningBadge(
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
    @SerialName("message")
    val message: String? = null,
    @SerialName("messagesubject")
    val messageSubject: String? = null,
    @SerialName("attachment")
    val attachment: Int? = null,
    @SerialName("status")
    val status: Int? = null,
    @SerialName("version")
    val version: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("badgeurl")
    val badgeUrl: String? = null
) {
    val isSiteBadge: Boolean get() = type == 1
    val isCourseBadge: Boolean get() = type == 2
    val isActive: Boolean get() = status == 1 || status == 3
}

/**
 * Represents a user's awarded badge.
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
