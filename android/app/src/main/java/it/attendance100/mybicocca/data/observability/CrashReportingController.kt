package it.attendance100.mybicocca.data.observability

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.core.version.buildNumber
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
    @ApplicationContext private val context: Context,
    private val repository: PrivacySettingsRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    fun start() {
        scope.launch {
            repository.observeCrashReportingEnabled().collect { enabled ->
                runCatching {
                    FirebaseCrashlytics.getInstance().apply {
                        isCrashlyticsCollectionEnabled = enabled
                        // versionName stays clean; this is what identifies the exact build.
                        setCustomKey("build_number", buildNumber(context))
                    }
                }
            }
        }
    }
}
