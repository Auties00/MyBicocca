package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserProfileCareersInner(
    @SerializedName("matricId")
    val matricId: Int? = null,

    @SerializedName("selected")
    val selected: Boolean? = null,

    @SerializedName("oldCode")
    val oldCode: Boolean? = null,

    @SerializedName("matricCode")
    val matricCode: String? = null,

    @SerializedName("studentId")
    val studentId: Int? = null
)

