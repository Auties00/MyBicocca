package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Study plan activity
 *
 * @param adCod Activity code
 * @param adDes Activity description
 * @param cfu Credits
 * @param anno Year
 * @param stato Status
 * @param voto Grade
 * @param dataEsame
 */


data class AttivitaLibretto(

    /* Activity code */
    @SerializedName("adCod")
    val adCod: String? = null,

    /* Activity description */
    @SerializedName("adDes")
    val adDes: String? = null,

    /* Credits */
    @SerializedName("cfu")
    val cfu: java.math.BigDecimal? = null,

    /* Year */
    @SerializedName("anno")
    val anno: Int? = null,

    /* Status */
    @SerializedName("stato")
    val stato: String? = null,

    /* Grade */
    @SerializedName("voto")
    val voto: String? = null,

    @SerializedName("dataEsame")
    val dataEsame: java.time.LocalDate? = null

)

