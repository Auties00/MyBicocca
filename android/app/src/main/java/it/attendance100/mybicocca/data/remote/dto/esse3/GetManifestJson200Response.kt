package it.attendance100.mybicocca.data.remote.dto.esse3

import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param name
 * @param icons
 * @param themeColor
 * @param backgroundColor
 * @param display
 */


data class GetManifestJson200Response(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("icons")
    val icons: List<GetManifestJson200ResponseIconsInner>? = null,

    @SerializedName("theme_color")
    val themeColor: String? = null,

    @SerializedName("background_color")
    val backgroundColor: String? = null,

    @SerializedName("display")
    val display: String? = null

)

