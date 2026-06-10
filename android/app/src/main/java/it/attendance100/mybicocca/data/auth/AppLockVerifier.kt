package it.attendance100.mybicocca.data.auth

import it.attendance100.mybicocca.data.local.credentials.CredentialsStore
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verifies a password against the active account for the app-lock fallback.
 *
 * The comparison always runs against the credential stored encrypted on-device
 * ([CredentialsStore], keystore-backed), which is the real password the user last signed in
 * with, refreshed on every successful login.
 *
 * Returns [UnlockResult.Error] only when there is no active account or no stored credential to
 * compare against; the password fallback is then unavailable and biometric unlock (or an app
 * reset) remains the only way in.
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
