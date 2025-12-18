package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserCareerCareer(
    @SerializedName("averages")
    val averages: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCareerAverage> = emptyList(),

    @SerializedName("stats")
    val stats: it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCareerStats? = null,
)

