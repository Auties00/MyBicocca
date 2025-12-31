package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import it.attendance100.mybicocca.data.api.bicoccapp.BicoccappApi
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.di.AppDatabase
import it.attendance100.mybicocca.domain.model.Alert
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.Tax
import it.attendance100.mybicocca.domain.model.User
import it.attendance100.mybicocca.manager.StorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URI
import java.time.LocalDateTime
import javax.inject.Inject
import it.attendance100.mybicocca.domain.repository.UserRepository as IUserRepository

class UserRepository @Inject constructor(
	private val bicoccappApi: BicoccappApi,
	private val esse3Api: Esse3Api,
	private val database: AppDatabase,
	private val storageManager: StorageManager,
) : IUserRepository {

    override fun isLoggedIn(): Boolean {
	    return storageManager.isLoggedIn()
    }

	override suspend fun startLogin(): Flow<URI> = flow {
		val response = bicoccappApi.auth.initiateLogin()
		val location = response.headers()["Location"]
		if (location != null) {
			emit(URI.create(location))
		}
	}

	override suspend fun finishLogin(code: String, state: String, cookie: String): Boolean {
		val response = bicoccappApi.auth.handleLoginCallback(code, state, cookie)
		// OkHttp follows redirects by default. The final URL should contain the tokens.
		if (response.isSuccessful) {
			val url = response.raw().request.url
			val accessToken = url.queryParameter("access_token")
			val client = url.queryParameter("client")
			val uid = url.queryParameter("uid")
			val fiscalCode = url.queryParameter("fiscal_code")
			val matricIdStr = url.queryParameter("favourite_career")

			android.util.Log.d("UserRepository", "Login callback params: accessToken=${accessToken != null}, client=${client != null}, uid=$uid, fiscalCode=$fiscalCode, matricId=$matricIdStr")

			if (accessToken != null && client != null && uid != null) {
				storageManager.authAccessToken = accessToken
				storageManager.authClient = client
				storageManager.authUid = uid
				storageManager.authFiscalCode = fiscalCode
                storageManager.authEnrollmentId = matricIdStr
				return true
			}
		}
		return false
	}

	override suspend fun logout() {
		storageManager.clearAuth()
		database.clearAllTables()
	}

	override fun observeUser(): LiveData<User?> {
		return database.userDao().getUser().asLiveData()
	}

	override fun observeCareerStats(): LiveData<CareerStats?> {
		return database.userDao().getCareerStats().asLiveData()
	}

	override fun observeTaxes(): LiveData<List<Tax>> {
		return database.userDao().observeTaxes()
    }

	override fun observeAlerts(): LiveData<List<Alert>> {
		return database.userDao().observeAlerts()
    }

	override suspend fun syncUser(): Boolean {
		// Try to fetch profile. API might need different identifiers.
		// We now capture fiscalCode, personId, and matricId from login.
        val fiscalCode = storageManager.authFiscalCode ?: return false

        // If we have data from login, we can optimistically insert a partial User entity
		// so the UI shows something even if getProfile fails.
		if (storageManager.authEnrollmentId != null) {
			val partialUser = User(
				id = 0,
				name = "", // Unknown yet
				surname = "", // Unknown yet
				matricola = storageManager.authEnrollmentId.toString(),
				course = "",
				year = "",
				email = storageManager.authUid ?: "",
			)
			database.userDao().insertUser(partialUser)
		}

        val response = bicoccappApi.user.getProfile(fiscalCode)
        if (!response.isSuccessful) {
            android.util.Log.e("UserRepository", "syncUser failed: ${response.code()} ${response.message()}")
            return false
        }

        val dto = response.body()
        val userDto = dto?.user ?: return false

        val matricola = dto.careers.firstOrNull()?.enrollmentId ?: storageManager.authEnrollmentId.toString()
        val entity = User(
            id = 0,
            name = userDto.name ?: "",
            surname = userDto.surname ?: "",
            matricola = matricola,
            course = dto.careers.firstOrNull()?.toString() ?: "",
            year = dto.careers.firstOrNull()?.toString() ?: "",
            email = userDto.email ?: storageManager.authUid ?: "",
        )
        database.userDao().insertUser(entity)

        return true
	}

	override suspend fun syncCareerStats(): Boolean {
		val fiscalCode = storageManager.authFiscalCode ?: return false
        val authEnrollmentId = storageManager.authEnrollmentId ?: return false

        val userResponse = bicoccappApi.user.getProfile(fiscalCode)
        if(!userResponse.isSuccessful) return false
        val user = userResponse.body()?.user ?: return false
        val personId = user.personId?.toString() ?: return false
        val userId = user.userId ?: return false

		val response = bicoccappApi.user.getCareer(personId, authEnrollmentId, userId)
        if (!response.isSuccessful) return false

        val dto = response.body()
        val careerData = dto?.career
        val statsData = careerData?.stats
        val averageData = careerData?.averages?.firstOrNull() // Taking first available average

        val entity = CareerStats(
            id = 0,
            mediaAritmetica = averageData?.arithmetic?.toFloat() ?: 0f,
            mediaPonderata = averageData?.weighted?.toFloat() ?: 0f,
            esamiSostenuti = statsData?.examsDone ?: 0,
            esamiTotali = 0,
            cfuAcquisiti = statsData?.totalDone?.toInt() ?: 0,
            cfuTotali = statsData?.totalToDo?.toInt() ?: 0,
            grades = emptyList(),
            passedExams = emptyList(),
            remainingExams = emptyList(),
        )

        database.userDao()
            .insertCareerStats(entity)

        return true
    }

	override suspend fun syncTaxes(): Boolean {
        val fiscalCode = storageManager.authFiscalCode ?: return false
        val authEnrollmentId = storageManager.authEnrollmentId ?: return false

        val userResponse = bicoccappApi.user.getProfile(fiscalCode)
        if(!userResponse.isSuccessful) return false
        val user = userResponse.body()?.user ?: return false
        val personId = user.personId?.toString() ?: return false

		val response = bicoccappApi.user.getTaxes(personId, authEnrollmentId)
        if (!response.isSuccessful) return false

        val dto = response.body()?.career?.fees ?: emptyList()

        val domainList = dto.map { t ->
            Tax(
                id = t.invoiceId ?: 0,
                description = t.description ?: "",
                amount = t.invoiceAmount?.toFloatOrNull() ?: 0f,
                amountPaid = t.invoiceAmountPaid?.toFloatOrNull() ?: 0f,
                dueDate = null, // Parse date
                paymentDate = null,
                isPaid = t.paidFlag == 1,
                isExpired = false,
            )
        }

        database.userDao()
            .insertTaxes(domainList)

        return true
	}

	override suspend fun syncAlerts(): Boolean {
		val response = bicoccappApi.messages.getAlerts()
        if (!response.isSuccessful) return false

        val dto = response.body()?.alerts ?: emptyList()
        val domainList = dto.map { a ->
            Alert(
                id = a.alertId ?: 0L,
                title = a.title ?: "",
                date = LocalDateTime.now(), // Placeholder, parse date string
                isRead = a.read == true,
                message = "", // DTO rows logic needed? BicoccappAlert has rows: List<String>
            )
        }

        database.userDao()
            .insertAlerts(domainList)

        return true
    }
}