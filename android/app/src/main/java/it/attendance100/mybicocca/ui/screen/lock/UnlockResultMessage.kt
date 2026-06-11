package it.attendance100.mybicocca.ui.screen.lock

import androidx.annotation.StringRes
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.security.UnlockResult

/** Inline error copy (string resource) for a failed unlock attempt; null on [UnlockResult.Success]. */
@StringRes
fun UnlockResult.errorMessageRes(): Int? = when (this) {
    UnlockResult.Success -> null
    UnlockResult.WrongPassword -> R.string.lock_error_wrong_password
    UnlockResult.Error -> R.string.lock_error_cannot_verify
}
