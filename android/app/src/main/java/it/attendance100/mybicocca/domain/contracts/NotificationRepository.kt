package it.attendance100.mybicocca.domain.contracts

import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*

interface NotificationRepository {
  fun getUnreadNotifications(): Flow<List<Notification>>
}
