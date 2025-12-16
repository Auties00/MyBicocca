package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Appointment/booking
 *
 * @param id
 * @param `data`
 * @param ora
 * @param durata Duration in minutes
 * @param servizio Service type
 * @param stato
 */


data class Appuntamento(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("data")
    val `data`: java.time.LocalDate? = null,

    @SerializedName("ora")
    val ora: String? = null,

    /* Duration in minutes */
    @SerializedName("durata")
    val durata: Int? = null,

    /* Service type */
    @SerializedName("servizio")
    val servizio: String? = null,

    @SerializedName("stato")
    val stato: String? = null

)

