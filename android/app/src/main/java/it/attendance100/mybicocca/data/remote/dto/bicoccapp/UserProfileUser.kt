package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param appuserId
 * @param personId
 * @param userId
 * @param fiscalCode
 * @param name
 * @param surname
 * @param mobile
 * @param email
 * @param fotoId
 * @param photo
 * @param hasCalendar
 */


data class UserProfileUser(

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

