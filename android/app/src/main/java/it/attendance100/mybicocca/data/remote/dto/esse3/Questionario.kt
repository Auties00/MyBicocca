package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Questionnaire
 *
 * @param id
 * @param titolo
 * @param tipo
 * @param stato
 * @param obbligatorio
 */


data class Questionario(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("titolo")
    val titolo: String? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("stato")
    val stato: String? = null,

    @SerializedName("obbligatorio")
    val obbligatorio: Boolean? = null

)

