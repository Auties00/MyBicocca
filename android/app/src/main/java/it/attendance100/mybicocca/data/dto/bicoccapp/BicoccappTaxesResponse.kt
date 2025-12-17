package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappTaxesResponse(
    @SerializedName("career")
    val career: BicoccappUserTaxes? = null
)

