package it.attendance100.mybicocca.data.datasources.notification

import it.attendance100.mybicocca.data.mocks.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*

class MockNotificationDataSource @Inject constructor() : NotificationDataSource {
  override suspend fun getUnreadNotifications(): List<Notification> = NotificationMockData.notifications
}
