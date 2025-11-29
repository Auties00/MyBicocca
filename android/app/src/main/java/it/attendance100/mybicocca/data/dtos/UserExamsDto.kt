package it.attendance100.mybicocca.data.dtos

import com.google.gson.annotations.*

data class UserExamsResponse(
  @SerializedName("career") val career: ExamsCareerDetail,
)

data class ExamsCareerDetail(
  @SerializedName("notations") val notations: List<ExamNotation>,
)

data class ExamNotation(
  @SerializedName("year") val year: Int,
  @SerializedName("dateExam") val dateExam: String,
  @SerializedName("laudFlag") val laudFlag: Int,
  @SerializedName("grade") val grade: Float,
)
