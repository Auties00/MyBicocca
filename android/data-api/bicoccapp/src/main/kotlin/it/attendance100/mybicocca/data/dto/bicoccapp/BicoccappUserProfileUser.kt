package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserProfileUser(
    @SerializedName("appuser_id")
    val appuserId: Int? = null,

    @SerializedName("personId")
    val personId: Int? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("fiscalCode")
    val fiscalCode: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("surname")
    val surname: String? = null,

    @SerializedName("mobile")
    val mobile: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("fotoId")
    val fotoId: Int? = null,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("has_calendar")
    val hasCalendar: Boolean? = null
)

