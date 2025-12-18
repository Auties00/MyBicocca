package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappWizardCategoriesResponse(
    @SerializedName("categories")
    val categories: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccapWizardCategory> = emptyList()
)


