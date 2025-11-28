package it.attendance100.mybicocca.data.datasources.notification

import it.attendance100.mybicocca.domain.model.*

/**
 * Abstraction for notification retrieval
 * Defines methods to fetch user notifications
 */
interface NotificationDataSource {
  suspend fun getUnreadNotifications(): List<Notification>
}
