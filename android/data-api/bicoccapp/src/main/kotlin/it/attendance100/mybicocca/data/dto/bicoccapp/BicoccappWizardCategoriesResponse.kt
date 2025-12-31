package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappWizardCategoriesResponse(
    @SerialName("categories")
    val categories: List<BicoccapWizardCategory> = emptyList()
)


