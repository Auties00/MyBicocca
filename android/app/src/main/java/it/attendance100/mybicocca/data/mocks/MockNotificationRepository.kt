package it.attendance100.mybicocca.data.mocks

import it.attendance100.mybicocca.domain.contracts.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import javax.inject.*

class MockNotificationRepository @Inject constructor() : NotificationRepository {
  override fun getUnreadNotifications(): Flow<List<Notification>> = flowOf(
    listOf(
      Notification(
        id = "1",
        title = "New exam results available",
        timeAgo = "2 hours ago",
        type = NotificationType.EXAM_RESULT
      ),
      Notification(
        id = "2",
        title = "Upcoming lecture tomorrow",
        timeAgo = "5 hours ago",
        type = NotificationType.LECTURE
      )
    )
  )
}
