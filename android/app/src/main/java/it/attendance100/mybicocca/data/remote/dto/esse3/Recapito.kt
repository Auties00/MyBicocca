package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Contact information
 *
 * @param tipo Contact type
 * @param valore Contact value
 * @param principale Is primary contact
 */


data class Recapito(

    /* Contact type */
    @SerializedName("tipo")
    val tipo: String? = null,

    /* Contact value */
    @SerializedName("valore")
    val valore: String? = null,

    /* Is primary contact */
    @SerializedName("principale")
    val principale: Boolean? = null

)

