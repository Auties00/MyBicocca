package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserExamsCareerNotation(
    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("dateExam")
    val dateExam: String? = null,

    @SerializedName("laudFlag")
    val laudFlag: Int? = null,

    @SerializedName("grade")
    val grade: Int? = null
)