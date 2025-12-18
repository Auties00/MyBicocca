package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import java.net.*

interface UserRepository {
    fun isLoggedIn(): Boolean
    suspend fun startLogin(): Flow<URI>
	suspend fun finishLogin(code: String, state: String, cookie: String): Boolean
	suspend fun logout()

	fun observeUser(): LiveData<User?>
	fun observeCareerStats(): LiveData<CareerStats?>
	fun observeTaxes(): LiveData<List<Tax>>
	fun observeAlerts(): LiveData<List<Alert>>

	suspend fun syncUser()
	suspend fun syncCareerStats()
	suspend fun syncTaxes()
	suspend fun syncAlerts()
}
