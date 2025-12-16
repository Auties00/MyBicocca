package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Address information
 *
 * @param via Street address
 * @param civico House number
 * @param cap Postal code
 * @param comune City
 * @param provincia Province
 * @param nazione Country
 * @param tipoIndirizzo Address type: residence or domicile
 */


data class Indirizzo(

    /* Street address */
    @SerializedName("via")
    val via: String? = null,

    /* House number */
    @SerializedName("civico")
    val civico: String? = null,

    /* Postal code */
    @SerializedName("cap")
    val cap: String? = null,

    /* City */
    @SerializedName("comune")
    val comune: String? = null,

    /* Province */
    @SerializedName("provincia")
    val provincia: String? = null,

    /* Country */
    @SerializedName("nazione")
    val nazione: String? = null,

    /* Address type: residence or domicile */
    @SerializedName("tipoIndirizzo")
    val tipoIndirizzo: TipoIndirizzo? = null

) {

    /**
     * Address type: residence or domicile
     *
     * Values: RES,DOM
     */
    enum class TipoIndirizzo(val value: String) {
        @SerializedName(value = "RES")
        RES("RES"),
        @SerializedName(value = "DOM")
        DOM("DOM");
    }

}

