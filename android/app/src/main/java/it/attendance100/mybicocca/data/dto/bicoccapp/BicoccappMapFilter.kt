package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappMapFilter(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("color")
    val color: String? = null
)