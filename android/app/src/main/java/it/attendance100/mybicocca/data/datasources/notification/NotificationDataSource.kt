package it.attendance100.mybicocca.data.datasources.notification

import it.attendance100.mybicocca.domain.model.*

interface NotificationDataSource {
  suspend fun getUnreadNotifications(): List<Notification>
}
