package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccapWizardCategory(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("code")
    val code: String? = null,
)