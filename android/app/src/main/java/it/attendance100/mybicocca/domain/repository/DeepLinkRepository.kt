package it.attendance100.mybicocca.domain.repository

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
 * Two links are handled: Moodle mod_attendance QR scans (the raw attendance.php URL, consumed
 * by the Presenze flow) and Affluences reservation email links (parsed into a
 * [LibraryDeepLinkAction], consumed by the Biblioteca flow).
 */
interface DeepLinkRepository {
    fun observePendingPresenceScan(): Flow<String?>
    fun submitPresenceScan(rawLink: String)
    fun consumePresenceScan()

    fun observePendingLibraryAction(): Flow<LibraryDeepLinkAction?>
    fun submitLibraryAction(action: LibraryDeepLinkAction)
    fun consumeLibraryAction()
}
