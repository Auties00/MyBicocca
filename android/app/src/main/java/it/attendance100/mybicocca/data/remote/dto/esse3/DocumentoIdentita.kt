package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Identity document
 *
 * @param tipo Document type
 * @param numero Document number
 * @param dataRilascio Issue date
 * @param dataScadenza Expiry date
 * @param enteRilascio Issuing authority
 */


data class DocumentoIdentita(

    /* Document type */
    @SerializedName("tipo")
    val tipo: String? = null,

    /* Document number */
    @SerializedName("numero")
    val numero: String? = null,

    /* Issue date */
    @SerializedName("dataRilascio")
    val dataRilascio: java.time.LocalDate? = null,

    /* Expiry date */
    @SerializedName("dataScadenza")
    val dataScadenza: java.time.LocalDate? = null,

    /* Issuing authority */
    @SerializedName("enteRilascio")
    val enteRilascio: String? = null

)

