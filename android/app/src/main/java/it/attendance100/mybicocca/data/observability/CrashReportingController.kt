package it.attendance100.mybicocca.data.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.repository.PrivacySettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies the persisted crash-reporting preference to the Crashlytics SDK, at startup and on every later change.
 * The SDK persists the flag on its own, so crashes that happen on the next launch before DataStore has been read still honor the user's last choice.
 */
@Singleton
class CrashReportingController @Inject constructor(
    private val repository: PrivacySettingsRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    fun start() {
        scope.launch {
            repository.observeCrashReportingEnabled().collect { enabled ->
                runCatching {
                    FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
                }
            }
        }
    }
}
