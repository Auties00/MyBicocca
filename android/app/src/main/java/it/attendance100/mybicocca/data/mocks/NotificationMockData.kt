package it.attendance100.mybicocca.data.mocks

import it.attendance100.mybicocca.domain.model.*

object NotificationMockData {
  val notifications = listOf(
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
}
