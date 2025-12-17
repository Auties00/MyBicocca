package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappWizardDegreesResponse(
    @SerializedName("degrees")
    val degrees: List<BicoccappWizardDegree>? = emptyList()
)

