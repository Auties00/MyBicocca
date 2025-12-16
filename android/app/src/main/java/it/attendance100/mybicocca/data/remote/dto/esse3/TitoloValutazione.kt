package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Evaluation credential
 *
 * @param id
 * @param tipo
 * @param descrizione
 * @param punteggio
 */


data class TitoloValutazione(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("descrizione")
    val descrizione: String? = null,

    @SerializedName("punteggio")
    val punteggio: java.math.BigDecimal? = null

)

