package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param ok
 * @param allegatoId
 */


data class PostChunkuploadend200Response(

    @SerializedName("ok")
    val ok: Int? = null,

    @SerializedName("allegatoId")
    val allegatoId: Int? = null

)

