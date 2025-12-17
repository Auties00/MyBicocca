package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappExamsSessionsResponse(
    @SerializedName("career")
    val career: BicoccappUserAppealsCareer? = null
)

