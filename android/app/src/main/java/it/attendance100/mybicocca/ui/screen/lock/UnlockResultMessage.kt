package it.attendance100.mybicocca.ui.screen.lock

import it.attendance100.mybicocca.domain.model.security.UnlockResult

/** Inline error copy for a failed unlock attempt; null on [UnlockResult.Success]. */
fun UnlockResult.errorMessage(): String? = when (this) {
    UnlockResult.Success -> null
    UnlockResult.WrongPassword -> "Password errata"
    UnlockResult.Error -> "Impossibile verificare"
}
