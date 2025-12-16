package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Exam booking
 *
 * @param id
 * @param appelloId
 * @param dataPrenotazione
 * @param stato
 */


data class Prenotazione(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("appelloId")
    val appelloId: Int? = null,

    @SerializedName("dataPrenotazione")
    val dataPrenotazione: java.time.OffsetDateTime? = null,

    @SerializedName("stato")
    val stato: Stato? = null

) {

    /**
     *
     *
     * Values: CONFERMATA,IN_ATTESA,ANNULLATA
     */
    enum class Stato(val value: String) {
        @SerializedName(value = "CONFERMATA")
        CONFERMATA("CONFERMATA"),
        @SerializedName(value = "IN_ATTESA")
        IN_ATTESA("IN_ATTESA"),
        @SerializedName(value = "ANNULLATA")
        ANNULLATA("ANNULLATA");
    }

}

