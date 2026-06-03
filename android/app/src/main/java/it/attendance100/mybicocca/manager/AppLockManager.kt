package it.attendance100.mybicocca.manager

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.settings.ActiveAccountStore
import it.attendance100.mybicocca.data.local.settings.SecuritySettingsStore
import it.attendance100.mybicocca.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the app-lock gate: a soft, local lock that hides the signed-in UI behind a biometric /
 * password challenge on cold start and after a configurable idle period.
 */
@Singleton
class AppLockManager @Inject constructor(
    securityStore: SecuritySettingsStore,
    activeAccountStore: ActiveAccountStore,
    sessionManager: SessionManager,
    @ApplicationScope private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    private val armed = MutableStateFlow(false)

    @Volatile
    private var timeoutMillis = 0L
    private var lastBackgroundedAt: Long? = null
    private var firstStart = true

    val locked: StateFlow<Boolean> = combine(
        securityStore.appLockEnabled,
        sessionManager.activeAccount,
        armed,
    ) { enabled, account, isArmed -> enabled && isArmed && account != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    init {
        scope.launch {
            securityStore.lockTimeoutMinutes.collect { timeoutMillis = it * 60_000L }
        }
        // Cold start: arm the gate if a session already existed at launch. Reading the persisted
        // id directly keeps a *later* fresh sign-in from tripping this one-shot.
        scope.launch {
            if (activeAccountStore.activeAccountId.first() != null) armed.value = true
        }
        scope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@AppLockManager)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (firstStart) {
            // Cold-start foreground — arming is handled by the [init] block above.
            firstStart = false
            return
        }
        val backgroundedAt = lastBackgroundedAt ?: return
        if (SystemClock.elapsedRealtime() - backgroundedAt >= timeoutMillis) {
            armed.value = true
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        lastBackgroundedAt = SystemClock.elapsedRealtime()
    }

    /**
     * Marks the user as verified and disarms the gate so [locked] drops to false. Called after a
     * successful biometric/password unlock, and after the (already authenticated) Settings toggle
     * so enabling the lock doesn't immediately re-challenge.
     */
    fun unlock() {
        armed.value = false
    }
}
