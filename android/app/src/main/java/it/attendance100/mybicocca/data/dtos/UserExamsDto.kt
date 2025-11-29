package it.attendance100.mybicocca.data.dtos

import com.google.gson.annotations.*

data class UserExamsResponse(
  @SerializedName("career") val career: ExamsCareerDetail,
)

data class ExamsCareerDetail(
  @SerializedName("notations") val notations: List<ExamNotation>,
  @SerializedName("exams") val exams: List<ExamItem>,
  @SerializedName("remainings") val remainings: List<ExamItem>,
)

data class ExamNotation(
  @SerializedName("year") val year: Int,
  @SerializedName("dateExam") val dateExam: String,
  @SerializedName("laudFlag") val laudFlag: Int,
  @SerializedName("grade") val grade: Float,
) {
  val isCumLaude: Boolean get() = laudFlag == 1
}

data class ExamItem(
  @SerializedName("yearFreqId") val yearFreqId: Int,
  @SerializedName("activityCode") val activityCode: String,
  @SerializedName("activityDescr") val activityDescr: String,
  @SerializedName("courseYear") val courseYear: Int,
  @SerializedName("order") val order: Int,
  @SerializedName("cfu") val cfu: String, // It comes as string "8.0"
  @SerializedName("status") val status: String,
  @SerializedName("statusDescr") val statusDescr: String,
  @SerializedName("typeExamCode") val typeExamCode: String?,
  @SerializedName("typeExamDescr") val typeExamDescr: String?,
  @SerializedName("typeCourseCode") val typeCourseCode: String?,
  @SerializedName("typeCourseDescr") val typeCourseDescr: String?,
  @SerializedName("dateExam") val dateExam: String?,
  @SerializedName("grade") val grade: String?, // Can be "0", "22", "30L"
  @SerializedName("laudFlag") val laudFlag: Int,
) {
  val isCumLaude: Boolean get() = laudFlag == 1
}
