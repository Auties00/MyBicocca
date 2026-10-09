package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.notification.NotificationRoute
import it.attendance100.mybicocca.domain.model.library.LibraryDeepLinkAction
import kotlinx.coroutines.flow.Flow

/**
 * In-memory hand-off for deep links opened from outside the app.
 *
 * The activity parses incoming intents and submits the payload here; the owning feature's
 * ViewModel observes it, consumes it, and runs the flow. A pending value sticks until consumed,
 * so a link that arrives before the target screen exists is not lost — but nothing is persisted,
 * so links do not survive process death.
 *
 * Three payloads are handled: Moodle mod_attendance QR scans (the raw attendance.php URL,
 * consumed by the Presenze flow), Affluences reservation email links (parsed into a
 * [LibraryDeepLinkAction], consumed by the Biblioteca flow), and a [NotificationRoute] carried
 * by a tap on one of this app's own notifications.
 */
interface DeepLinkRepository {
    fun observePendingPresenceScan(): Flow<String?>
    fun submitPresenceScan(rawLink: String)
    fun consumePresenceScan()

    fun observePendingLibraryAction(): Flow<LibraryDeepLinkAction?>
    fun submitLibraryAction(action: LibraryDeepLinkAction)
    fun consumeLibraryAction()

    fun observePendingNotificationRoute(): Flow<NotificationRoute?>
    fun submitNotificationRoute(route: NotificationRoute)
    fun consumeNotificationRoute()
}
