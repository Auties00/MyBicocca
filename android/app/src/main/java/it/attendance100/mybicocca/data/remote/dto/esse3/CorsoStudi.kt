package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Study course
 *
 * @param codice Course code
 * @param denominazione Course name
 * @param tipo Course type
 * @param classe Degree class
 * @param annoAccademico Academic year
 */


data class CorsoStudi(

    /* Course code */
    @SerializedName("codice")
    val codice: String? = null,

    /* Course name */
    @SerializedName("denominazione")
    val denominazione: String? = null,

    /* Course type */
    @SerializedName("tipo")
    val tipo: String? = null,

    /* Degree class */
    @SerializedName("classe")
    val classe: String? = null,

    /* Academic year */
    @SerializedName("annoAccademico")
    val annoAccademico: String? = null

)

