package it.attendance100.mybicocca.data.remote.dto.esse3


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param src
 * @param sizes
 * @param type
 */


data class GetManifestJson200ResponseIconsInner(

    @SerializedName("src")
    val src: String? = null,

    @SerializedName("sizes")
    val sizes: String? = null,

    @SerializedName("type")
    val type: String? = null

)

