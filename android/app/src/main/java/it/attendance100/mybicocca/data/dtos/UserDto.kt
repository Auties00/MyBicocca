package it.attendance100.mybicocca.data.dtos

import com.google.gson.annotations.*

// --- /user_profile ---
data class UserProfileResponse(
  @SerializedName("user") val user: UserDetail,
  @SerializedName("careers") val careers: List<CareerInfo>,
)

data class UserDetail(
  @SerializedName("name") val name: String,
  @SerializedName("surname") val surname: String,
  @SerializedName("email") val email: String,
  @SerializedName("personId") val personId: Int,
)

data class CareerInfo(
  @SerializedName("matricId") val matricId: Int,
  @SerializedName("studentId") val studentId: Int,
  @SerializedName("matricCode") val matricCode: String,
  @SerializedName("typeCourseDescr") val courseDescription: String?,
  @SerializedName("yearRegId") val courseYear: Int?,
  @SerializedName("typeTitleCode") val typeTitleCode: String?,
)

// --- /user_career ---
data class UserCareerResponse(
  @SerializedName("career") val career: CareerDetail,
)

data class CareerDetail(
  @SerializedName("averages") val averages: List<AverageInfo>,
  @SerializedName("stats") val stats: StatInfo,
)

data class AverageInfo(
  @SerializedName("base") val base: Int,
  @SerializedName("basedefinition") val baseDefinition: String,
  @SerializedName("arithmetic") val arithmetic: Float,
  @SerializedName("weighted") val weighted: Float,
)

data class StatInfo(
  @SerializedName("examsDone") val examsDone: Int,
  @SerializedName("totalDone") val cfuDone: Float,
  @SerializedName("totalToDo") val totalToDo: Float,
)