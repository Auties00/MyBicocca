package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCareerAverage(
    @SerializedName("base")
    val base: Int? = null,

    @SerializedName("basedefinition")
    val baseDefinition: String? = null,

    @SerializedName("weighted")
    val weighted: Double? = null,

    @SerializedName("arithmetic")
    val arithmetic: Double? = null
)