package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.data.api.bicoccapp.*
import it.attendance100.mybicocca.data.api.esse3.*
import it.attendance100.mybicocca.di.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.manager.*
import kotlinx.coroutines.flow.*
import java.net.*
import java.time.*
import javax.inject.*
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
			val personIdStr = url.queryParameter("id")
			val matricIdStr = url.queryParameter("favourite_career")

			android.util.Log.d("UserRepository", "Login callback params: accessToken=${accessToken != null}, client=${client != null}, uid=$uid, fiscalCode=$fiscalCode, personId=$personIdStr, matricId=$matricIdStr")

			if (accessToken != null && client != null && uid != null) {
				storageManager.authAccessToken = accessToken
				storageManager.authClient = client
				storageManager.authUid = uid
				storageManager.authFiscalCode = fiscalCode

				personIdStr?.toIntOrNull()?.let { storageManager.userPersonId = it }
				matricIdStr?.toIntOrNull()?.let { storageManager.userMatricId = it }

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

	override suspend fun syncUser() {
		// Try to fetch profile. API might need different identifiers.
		// We now capture fiscalCode, personId, and matricId from login.
		val fiscalCode = storageManager.authFiscalCode

		// If we have data from login, we can optimistically insert a partial User entity
		// so the UI shows something even if getProfile fails.
		if (storageManager.userMatricId != -1) {
			val partialUser = User(
				id = 0,
				name = "", // Unknown yet
				surname = "", // Unknown yet
				matricola = storageManager.userMatricId.toString(),
				course = "",
				year = "",
				email = storageManager.authUid ?: "",
			)
			database.userDao().insertUser(partialUser)
		}

		// Try fetching full profile
		// Some endpoints might prefer personId or matricId over fiscalCode?
		// Let's try passing what we have.
		// Note: BicoccappUserApi.getProfile currently only takes fiscal_code.
		// If that fails, we rely on syncCareerStats to fill in gaps if possible.
		try {
			val response = bicoccappApi.user.getProfile(fiscalCode)
			if (response.isSuccessful) {
				val dto = response.body()
				dto?.user?.let { userDto ->
					// ... (existing mapping logic)
					val matricola = dto.careers.firstOrNull()?.matricCode ?: storageManager.userMatricId.toString()
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
				}
			} else {
				android.util.Log.e("UserRepository", "syncUser failed: ${response.code()} ${response.message()}")
			}
		} catch (e: Exception) {
			android.util.Log.e("UserRepository", "syncUser exception", e)
		}
	}

	override suspend fun syncCareerStats() {
		// Use the matricId captured during login
		val matricId = if (storageManager.userMatricId != -1) storageManager.userMatricId else null

		val response = bicoccappApi.user.getCareer(matricId = matricId)
		if (response.isSuccessful) {
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
			database.userDao().insertCareerStats(entity)
		}
    }

	override suspend fun syncTaxes() {
		val response = bicoccappApi.user.getTaxes()
		if (response.isSuccessful) {
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
			database.userDao().insertTaxes(domainList)
		}
	}

	override suspend fun syncAlerts() {
		val response = bicoccappApi.messages.getAlerts()
		if (response.isSuccessful) {
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
			database.userDao().insertAlerts(domainList)
		}
    }
}