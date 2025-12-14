package it.attendance100.mybicocca.data.datasources.user

import it.attendance100.mybicocca.data.api.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*

class RemoteUserDataSource @Inject constructor(
  private val api: MyBicoccaApiService,
  private val preferencesManager: PreferencesManager,
) : UserDataSource {

  override suspend fun getUser(): User {
    val fiscalCode = preferencesManager.authFiscalCode
    android.util.Log.d("RemoteUserDataSource", "getUser: Starting live API fetch for fiscalCode: $fiscalCode")
    val response = api.getUserProfile(fiscalCode)

    if (response.user == null) {
      android.util.Log.e("RemoteUserDataSource", "getUser: API returned null user")
      throw ApiException(500, "Invalid response: User data is missing")
    }

    android.util.Log.d("RemoteUserDataSource", "getUser: Live API fetch successful")
    val userDetail = response.user
    val career = response.careers?.firstOrNull()

    // Cache IDs for future calls
    if (career != null) {
      preferencesManager.userStudentId = career.studentId
      preferencesManager.userMatricId = career.matricId
      preferencesManager.userPersonId = userDetail.personId
      preferencesManager.userTypeTitleCode = career.typeTitleCode
    }

    return User(
      name = userDetail.name,
      surname = userDetail.surname,
      matricola = career?.matricCode ?: "",
      course = career?.courseDescription ?: "",
      year = career?.courseYear?.toString() ?: "",
      email = userDetail.email
    )
  }

  override suspend fun getCareerStats(): CareerStats = coroutineScope {
    val fiscalCode = preferencesManager.authFiscalCode
    android.util.Log.d("RemoteUserDataSource", "getCareerStats: Starting fetch. FiscalCode: $fiscalCode")

    var stuId: Int? = preferencesManager.userStudentId.takeIf { it != -1 }
    var matId: Int? = preferencesManager.userMatricId.takeIf { it != -1 }
    var personId: Int? = preferencesManager.userPersonId.takeIf { it != -1 }
    var typeTitleCode: String? = preferencesManager.userTypeTitleCode

    if (stuId == null || matId == null || personId == null) {
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: IDs not in cache, fetching profile...")
      val userProfileResponse = api.getUserProfile(fiscalCode)
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Profile fetched. User: ${userProfileResponse.user?.name}")

      val careerInfo = userProfileResponse.careers?.firstOrNull()
      personId = userProfileResponse.user?.personId

      stuId = careerInfo?.studentId
      matId = careerInfo?.matricId
      typeTitleCode = careerInfo?.typeTitleCode

      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Extracted IDs - stuId: $stuId, matId: $matId, personId: $personId, typeTitleCode: $typeTitleCode")

      // Cache them now
      if (stuId != null && matId != null && personId != null) {
        preferencesManager.userStudentId = stuId
        preferencesManager.userMatricId = matId
        preferencesManager.userPersonId = personId
        preferencesManager.userTypeTitleCode = typeTitleCode
      } else {
        android.util.Log.w("RemoteUserDataSource", "getCareerStats: Failed to extract IDs from profile")
      }
    } else {
      android.util.Log.v("RemoteUserDataSource", "  getCareerStats: IDs found in cache.")
    }

    if (stuId == null) {
      android.util.Log.w("RemoteUserDataSource", "getCareerStats: Missing IDs, returning empty stats.")
      return@coroutineScope CareerStats(
        mediaAritmetica = 0f,
        mediaPonderata = 0f,
        esamiSostenuti = 0,
        esamiTotali = 0,
        cfuAcquisiti = 0,
        cfuTotali = 0,
        grades = emptyList(),
        passedExams = emptyList(),
        remainingExams = emptyList()
      )
    }

    val careerDeferred = async {
      android.util.Log.v("RemoteUserDataSource", "  getCareerStats: Calling getUserCareer...")
      api.getUserCareer(stuId, matId, personId, typeTitleCode)
    }
    val examsDeferred = async {
      android.util.Log.v("RemoteUserDataSource", "  getCareerStats: Calling getUserExams...")
      api.getUserExams(matId)
    }

    val careerResponse = careerDeferred.await()
    val examsResponse = examsDeferred.await()

    android.util.Log.d("RemoteUserDataSource", "getCareerStats: Live API responses received.")
    android.util.Log.d("RemoteUserDataSource", "getCareerStats: Fetched ${examsResponse.career.exams.size} passed exams and ${examsResponse.career.remainings.size} remaining exams from Live API")

    val stats = careerResponse.career.stats
    val averages = careerResponse.career.averages

    // Find weighted and arithmetic averages
    // Usually base 30 is what we want for display, but let's check the logic.
    // We'll pick the one with base 30 for the grades average.
    val avg30 = averages.find { it.base == 30 }

    val grades = examsResponse.career.notations.map { notation ->
      val examDetail = examsResponse.career.exams.find { it.dateExam == notation.dateExam }
      val isLode = notation.isLode
      val value = if (isLode) 31f else notation.grade

      GradePoint(
        value = value,
        date = notation.dateExam,
        name = examDetail?.activityDescr ?: "Esame",
        cfu = examDetail?.cfu ?: "?",
        isLode = isLode
      )
    }

    // Calculate total exams from the exams list + remainings list
    val totalExams = (examsResponse.career.exams.size + examsResponse.career.remainings.size)

    val passedExams = examsResponse.career.exams.map {
      Exam(
        name = it.activityDescr,
        cfu = it.cfu,
        grade = it.grade,
        date = it.dateExam,
        status = it.status,
        isLode = it.isLode
      )
    }

    val remainingExams = examsResponse.career.remainings.map {
      Exam(
        name = it.activityDescr,
        cfu = it.cfu,
        grade = null,
        date = null,
        status = it.status,
        isLode = false
      )
    }

    CareerStats(
      mediaAritmetica = avg30?.arithmetic ?: 0f,
      mediaPonderata = avg30?.weighted ?: 0f,
      esamiSostenuti = stats.examsDone,
      esamiTotali = totalExams,
      cfuAcquisiti = stats.cfuDone.toInt(),
      cfuTotali = stats.totalToDo.toInt(),
      grades = grades,
      passedExams = passedExams,
      remainingExams = remainingExams
    )
  }
}
