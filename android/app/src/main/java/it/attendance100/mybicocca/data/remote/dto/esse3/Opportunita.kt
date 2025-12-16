package it.attendance100.mybicocca.data.remote.dto.esse3

import com.google.gson.annotations.SerializedName

/**
 * Internship opportunity
 *
 * @param id
 * @param titolo Title
 * @param azienda
 * @param descrizione Description
 * @param durata Duration
 * @param sede Location
 * @param scadenza Application deadline
 */


data class Opportunita(

    @SerializedName("id")
    val id: Int? = null,

    /* Title */
    @SerializedName("titolo")
    val titolo: String? = null,

    @SerializedName("azienda")
    val azienda: Azienda? = null,

    /* Description */
    @SerializedName("descrizione")
    val descrizione: String? = null,

    /* Duration */
    @SerializedName("durata")
    val durata: String? = null,

    /* Location */
    @SerializedName("sede")
    val sede: String? = null,

    /* Application deadline */
    @SerializedName("scadenza")
    val scadenza: java.time.LocalDate? = null

)

