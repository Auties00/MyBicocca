package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Exam session
 *
 * @param id
 * @param adCod Activity code
 * @param adDes Activity description
 * @param dataAppello Exam date
 * @param oraAppello Exam time
 * @param aula Room
 * @param docente Professor
 * @param stato Status
 */


data class Appello(

    @SerializedName("id")
    val id: Int? = null,

    /* Activity code */
    @SerializedName("adCod")
    val adCod: String? = null,

    /* Activity description */
    @SerializedName("adDes")
    val adDes: String? = null,

    /* Exam date */
    @SerializedName("dataAppello")
    val dataAppello: java.time.LocalDate? = null,

    /* Exam time */
    @SerializedName("oraAppello")
    val oraAppello: String? = null,

    /* Room */
    @SerializedName("aula")
    val aula: String? = null,

    /* Professor */
    @SerializedName("docente")
    val docente: String? = null,

    /* Status */
    @SerializedName("stato")
    val stato: String? = null

)

