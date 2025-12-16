package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Admission competition
 *
 * @param id
 * @param denominazione
 * @param annoAccademico
 * @param dataInizio
 * @param dataFine
 * @param stato
 */


data class Concorso(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("denominazione")
    val denominazione: String? = null,

    @SerializedName("annoAccademico")
    val annoAccademico: String? = null,

    @SerializedName("dataInizio")
    val dataInizio: java.time.LocalDate? = null,

    @SerializedName("dataFine")
    val dataFine: java.time.LocalDate? = null,

    @SerializedName("stato")
    val stato: String? = null

)

