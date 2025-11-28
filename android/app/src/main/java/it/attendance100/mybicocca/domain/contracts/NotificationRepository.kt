package it.attendance100.mybicocca.domain.contracts

import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*

/**
 * Notification operations contract
 * Defines methods for retrieving notifications
 */
interface NotificationRepository {
  fun getUnreadNotifications(): Flow<List<Notification>>
}
