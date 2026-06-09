package it.attendance100.mybicocca.data.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Hand-off for a mod_attendance QR opened from outside the app (deep link): the
// activity drops the raw link here, MainShell routes to the attendance screen, and
// the attendance ViewModel consumes it to run the marking flow.
@Singleton
class PendingPresenceScan @Inject constructor() {

    private val _pending = MutableStateFlow<String?>(null)
    val pending: StateFlow<String?> = _pending.asStateFlow()

    fun submit(raw: String) {
        _pending.value = raw
    }

    fun consume() {
        _pending.value = null
    }
}
