package it.attendance100.mybicocca.manager

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.credentials.CredentialsStore
import javax.inject.Inject
import javax.inject.Singleton

enum class UnlockResult { Success, WrongPassword, Error }

/**
 * Verifies a password against the active account for the app-lock fallback.
 *
 * It always compares against the credential stored encrypted on-device
 * ([CredentialsStore], keystore-backed) which is the real password the user last signed in with,
 * refreshed on every successful login.
 *
 * Returns [UnlockResult.Error] only when there is no active account or no stored credential to compare
 * against (the password fallback is then unavailable; biometric remains the only way in. Or app reset).
 */
@Singleton
class AppLockVerifier @Inject constructor(
    private val sessionManager: SessionManager,
    private val credentialsStore: CredentialsStore,
) {

    suspend fun verifyPassword(password: String): UnlockResult {
        val account = sessionManager.activeAccount.value ?: return UnlockResult.Error
        if (password.isEmpty()) return UnlockResult.WrongPassword
        val stored = credentialsStore.read(account.id) ?: return UnlockResult.Error
        return if (stored.password == password) UnlockResult.Success else UnlockResult.WrongPassword
    }
}
