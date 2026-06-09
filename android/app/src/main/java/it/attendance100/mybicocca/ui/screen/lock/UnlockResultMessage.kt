package it.attendance100.mybicocca.ui.screen.lock

import it.attendance100.mybicocca.domain.model.security.UnlockResult

fun UnlockResult.errorMessage(): String? = when (this) {
    UnlockResult.Success -> null
    UnlockResult.WrongPassword -> "Password errata"
    UnlockResult.Error -> "Impossibile verificare"
}
