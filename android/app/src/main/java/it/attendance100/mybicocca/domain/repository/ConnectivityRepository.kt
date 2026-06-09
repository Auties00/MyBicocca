package it.attendance100.mybicocca.domain.repository

import kotlinx.coroutines.flow.Flow

// Hot stream of whether the device currently has validated internet access: a default
// network that both claims internet AND the system has confirmed actually reaches it, so
// a captive portal reads as offline. Backed by an app-scoped default-network callback.
interface ConnectivityRepository {
    fun observe(): Flow<Boolean>
}
