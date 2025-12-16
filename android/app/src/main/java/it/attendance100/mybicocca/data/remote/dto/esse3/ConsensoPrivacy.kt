package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Privacy consent
 *
 * @param id
 * @param tipo
 * @param accettato
 * @param dataAccettazione
 */


data class ConsensoPrivacy(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("accettato")
    val accettato: Boolean? = null,

    @SerializedName("dataAccettazione")
    val dataAccettazione: java.time.OffsetDateTime? = null

)

