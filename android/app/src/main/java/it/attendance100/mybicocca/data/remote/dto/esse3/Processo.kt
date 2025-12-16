package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Workflow process
 *
 * @param id
 * @param tipo
 * @param stato
 * @param dataInizio
 * @param dataFine
 * @param stepCorrente
 */


data class Processo(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("stato")
    val stato: String? = null,

    @SerializedName("dataInizio")
    val dataInizio: java.time.OffsetDateTime? = null,

    @SerializedName("dataFine")
    val dataFine: java.time.OffsetDateTime? = null,

    @SerializedName("stepCorrente")
    val stepCorrente: String? = null

)

