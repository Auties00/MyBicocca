package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Attachment/document
 *
 * @param id
 * @param nome File name
 * @param tipo MIME type
 * @param dimensione File size in bytes
 * @param dataCaricamento
 */


data class Allegato(

    @SerializedName("id")
    val id: Int? = null,

    /* File name */
    @SerializedName("nome")
    val nome: String? = null,

    /* MIME type */
    @SerializedName("tipo")
    val tipo: String? = null,

    /* File size in bytes */
    @SerializedName("dimensione")
    val dimensione: Int? = null,

    @SerializedName("dataCaricamento")
    val dataCaricamento: java.time.OffsetDateTime? = null

)

