package it.attendance100.mybicocca.domain.model

enum class NotificationType {
  EXAM_RESULT,
  LECTURE,
  GENERAL
}

/**
 * Notification business model
 * Represents a system notification for the user
 */
data class Notification(
  val id: String,
  val title: String,
  val timeAgo: String,
  val type: NotificationType,
)
