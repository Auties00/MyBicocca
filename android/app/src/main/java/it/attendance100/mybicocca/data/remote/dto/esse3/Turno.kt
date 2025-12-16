package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Time slot
 *
 * @param id
 * @param `data`
 * @param oraInizio
 * @param oraFine
 * @param postiDisponibili
 */


data class Turno(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("data")
    val `data`: java.time.LocalDate? = null,

    @SerializedName("oraInizio")
    val oraInizio: String? = null,

    @SerializedName("oraFine")
    val oraFine: String? = null,

    @SerializedName("postiDisponibili")
    val postiDisponibili: Int? = null

)

