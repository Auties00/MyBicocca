package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserTaxes(
    @SerialName("fees")
    val fees: List<BicoccappTax> = emptyList()
)

