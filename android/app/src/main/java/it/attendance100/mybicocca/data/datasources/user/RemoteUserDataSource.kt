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
    val response = api.getUserProfile(fiscalCode)
    val userDetail = response.user
    val career = response.careers.firstOrNull()

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
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Profile fetched. User: ${userProfileResponse.user.name}")

      val careerInfo = userProfileResponse.careers.firstOrNull()
      personId = userProfileResponse.user.personId

      stuId = careerInfo?.studentId
      matId = careerInfo?.matricId
      typeTitleCode = careerInfo?.typeTitleCode

      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Extracted IDs - stuId: $stuId, matId: $matId, personId: $personId, typeTitleCode: $typeTitleCode")

      // Cache them now
      if (stuId != null && matId != null) {
        preferencesManager.userStudentId = stuId
        preferencesManager.userMatricId = matId
        preferencesManager.userPersonId = personId
        preferencesManager.userTypeTitleCode = typeTitleCode
      }
    } else {
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: IDs found in cache.")
    }

    if (stuId == null || matId == null) {
      android.util.Log.w("RemoteUserDataSource", "getCareerStats: Missing IDs, returning empty stats.")
      return@coroutineScope CareerStats(
        mediaAritmetica = 0f,
        mediaPonderata = 0f,
        esamiSostenuti = 0,
        esamiTotali = 0,
        cfuAcquisiti = 0,
        cfuTotali = 0,
        grades = emptyList()
      )
    }

    val careerDeferred = async {
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Calling getUserCareer...")
      api.getUserCareer(stuId, matId, personId, typeTitleCode)
    }
    val examsDeferred = async {
      android.util.Log.d("RemoteUserDataSource", "getCareerStats: Calling getUserExams...")
      api.getUserExams(matId)
    }

    val careerResponse = careerDeferred.await()
    val examsResponse = examsDeferred.await()

    android.util.Log.d("RemoteUserDataSource", "getCareerStats: Responses received.")

    val stats = careerResponse.career.stats
    val averages = careerResponse.career.averages

    // Find weighted and arithmetic averages
    // Usually base 30 is what we want for display, but let's check the logic.
    // We'll pick the one with base 30 for the grades average.
    val avg30 = averages.find { it.base == 30 }

    val grades = examsResponse.career.notations.map { it.grade }

    CareerStats(
      mediaAritmetica = avg30?.arithmetic ?: 0f,
      mediaPonderata = avg30?.weighted ?: 0f,
      esamiSostenuti = stats.examsDone,
      esamiTotali = 0, // Not provided directly, maybe calculate or leave 0
      cfuAcquisiti = stats.cfuDone.toInt(),
      cfuTotali = stats.totalToDo.toInt(),
      grades = grades
    )
  }
}
