package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 * Competition registration
 *
 * @param id
 * @param concorsoId
 * @param dataIscrizione
 * @param stato
 * @param esito
 */


data class IscrizioneConcorso(

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("concorsoId")
    val concorsoId: Int? = null,

    @SerializedName("dataIscrizione")
    val dataIscrizione: java.time.OffsetDateTime? = null,

    @SerializedName("stato")
    val stato: String? = null,

    @SerializedName("esito")
    val esito: String? = null

)

