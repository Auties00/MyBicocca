package it.attendance100.mybicocca.data.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// What an Affluences reservation email link asks the app to do.
enum class LibraryActionKind { Cancel }

// Cancel a reservation, identified by the reservationToken carried in the email link.
data class LibraryDeepLinkAction(
    val kind: LibraryActionKind,
    val reservationToken: String,
)

// Hand-off for an Affluences confirm/cancel link opened from outside the app (email → deep link):
// the activity drops the parsed action here, MainShell opens the Biblioteca sheet and the
// ViewModel consumes it to run the action against the server. Mirrors PendingPresenceScan.
@Singleton
class PendingLibraryAction @Inject constructor() {

    private val _pending = MutableStateFlow<LibraryDeepLinkAction?>(null)
    val pending: StateFlow<LibraryDeepLinkAction?> = _pending.asStateFlow()

    fun submit(action: LibraryDeepLinkAction) {
        _pending.value = action
    }

    fun consume() {
        _pending.value = null
    }
}
