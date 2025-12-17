package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCareerStats(
    @SerializedName("valueCode")
    val valueCode: String? = null,

    @SerializedName("valueDescr")
    val valueDescr: String? = null,

    @SerializedName("examsDone")
    val examsDone: Int? = null,

    @SerializedName("totalToDo")
    val totalToDo: Double? = null,

    @SerializedName("totalDone")
    val totalDone: Double? = null
)