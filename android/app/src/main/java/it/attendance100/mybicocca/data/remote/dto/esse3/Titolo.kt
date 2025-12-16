package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Academic degree/qualification
 *
 * @param id
 * @param tipo Degree type
 * @param denominazione Degree name
 * @param ateneo University
 * @param annoConseguimento Year obtained
 * @param voto Grade
 * @param lode With honors
 */


data class Titolo(

    @SerializedName("id")
    val id: Int? = null,

    /* Degree type */
    @SerializedName("tipo")
    val tipo: String? = null,

    /* Degree name */
    @SerializedName("denominazione")
    val denominazione: String? = null,

    /* University */
    @SerializedName("ateneo")
    val ateneo: String? = null,

    /* Year obtained */
    @SerializedName("annoConseguimento")
    val annoConseguimento: Int? = null,

    /* Grade */
    @SerializedName("voto")
    val voto: String? = null,

    /* With honors */
    @SerializedName("lode")
    val lode: Boolean? = null

)

