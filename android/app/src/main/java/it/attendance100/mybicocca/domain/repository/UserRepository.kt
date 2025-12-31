package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.Alert
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.Tax
import it.attendance100.mybicocca.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.net.URI

interface UserRepository {
    fun isLoggedIn(): Boolean
    suspend fun startLogin(): Flow<URI>
	suspend fun finishLogin(code: String, state: String, cookie: String): Boolean
	suspend fun logout()

	fun observeUser(): LiveData<User?>
	fun observeCareerStats(): LiveData<CareerStats?>
	fun observeTaxes(): LiveData<List<Tax>>
	fun observeAlerts(): LiveData<List<Alert>>

	suspend fun syncUser(): Boolean
	suspend fun syncCareerStats(): Boolean
	suspend fun syncTaxes(): Boolean
	suspend fun syncAlerts(): Boolean
}
