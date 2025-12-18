package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserTaxes(
    @SerializedName("fees")
    val fees: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTax> = emptyList()
)

