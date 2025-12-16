package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Company
 *
 * @param id
 * @param denominazione Company name
 * @param settore Sector
 * @param localita Location
 * @param logo Logo URL
 */


data class Azienda(

    @SerializedName("id")
    val id: Int? = null,

    /* Company name */
    @SerializedName("denominazione")
    val denominazione: String? = null,

    /* Sector */
    @SerializedName("settore")
    val settore: String? = null,

    /* Location */
    @SerializedName("localita")
    val localita: String? = null,

    /* Logo URL */
    @SerializedName("logo")
    val logo: java.net.URI? = null

)

