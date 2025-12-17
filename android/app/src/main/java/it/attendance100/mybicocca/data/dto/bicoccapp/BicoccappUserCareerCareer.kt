package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserCareerCareer(
    @SerializedName("averages")
    val averages: List<BicoccappCareerAverage> = emptyList(),

    @SerializedName("stats")
    val stats: BicoccappCareerStats? = null,
)

