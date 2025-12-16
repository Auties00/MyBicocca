package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Exam result
 *
 * @param appelloId
 * @param voto Grade
 * @param lode
 * @param dataEsame
 * @param stato
 */


data class Esito(

    @SerializedName("appelloId")
    val appelloId: Int? = null,

    /* Grade */
    @SerializedName("voto")
    val voto: String? = null,

    @SerializedName("lode")
    val lode: Boolean? = null,

    @SerializedName("dataEsame")
    val dataEsame: java.time.LocalDate? = null,

    @SerializedName("stato")
    val stato: String? = null

)

