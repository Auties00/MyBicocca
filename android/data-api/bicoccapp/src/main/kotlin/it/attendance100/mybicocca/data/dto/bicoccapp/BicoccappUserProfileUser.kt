package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserProfileUser(
    @SerialName("appuser_id")
    val appuserId: Int? = null,

    @SerialName("personId")
    val personId: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("fiscalCode")
    val fiscalCode: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("surname")
    val surname: String? = null,

    @SerialName("mobile")
    val mobile: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("fotoId")
    val fotoId: Int? = null,

    @SerialName("photo")
    val photo: String? = null,

    @SerialName("has_calendar")
    val hasCalendar: Boolean? = null
)

