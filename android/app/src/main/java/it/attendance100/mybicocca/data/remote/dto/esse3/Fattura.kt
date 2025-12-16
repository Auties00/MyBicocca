package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Invoice/fee
 *
 * @param id
 * @param descrizione Description
 * @param importo Amount
 * @param dataScadenza Due date
 * @param dataPagamento Payment date
 * @param stato
 * @param iuv Unique payment code (PagoPA)
 */


data class Fattura(

    @SerializedName("id")
    val id: Int? = null,

    /* Description */
    @SerializedName("descrizione")
    val descrizione: String? = null,

    /* Amount */
    @SerializedName("importo")
    val importo: java.math.BigDecimal? = null,

    /* Due date */
    @SerializedName("dataScadenza")
    val dataScadenza: java.time.LocalDate? = null,

    /* Payment date */
    @SerializedName("dataPagamento")
    val dataPagamento: java.time.LocalDate? = null,

    @SerializedName("stato")
    val stato: Stato? = null,

    /* Unique payment code (PagoPA) */
    @SerializedName("iuv")
    val iuv: String? = null

) {

    /**
     *
     *
     * Values: PAGATA,DA_PAGARE,SCADUTA
     */
    enum class Stato(val value: String) {
        @SerializedName(value = "PAGATA")
        PAGATA("PAGATA"),
        @SerializedName(value = "DA_PAGARE")
        DA_PAGARE("DA_PAGARE"),
        @SerializedName(value = "SCADUTA")
        SCADUTA("SCADUTA");
    }

}

