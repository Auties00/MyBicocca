package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Internship application
 *
 * @param id
 * @param opportunitaId
 * @param dataCandidatura
 * @param stato
 */


data class Candidatura(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("opportunitaId")
    val opportunitaId: Int? = null,

    @SerializedName("dataCandidatura")
    val dataCandidatura: java.time.OffsetDateTime? = null,

    @SerializedName("stato")
    val stato: String? = null

)

