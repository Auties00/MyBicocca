package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.AppReleaseAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the outcome of the last update check in the shared `mybicocca_settings` DataStore so
 * the Settings tile shows an available update across restarts without re-hitting the network,
 * and so the daily check can throttle itself and avoid re-notifying about a version the user has
 * already been told about.
 *
 * @see PersistedUpdateState for the shape exposed to the repository.
 */
@Singleton
class UpdateStateStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val state: Flow<PersistedUpdateState> = dataStore.data.map { prefs ->
        val available = prefs[AVAILABLE_KEY] ?: false
        val version = prefs[REL_VERSION_KEY]
        val release = if (available && version != null) {
            val assetsJson = prefs[REL_ASSETS_KEY]
            if (assetsJson == null) {
                null
            } else {
                val assetsList = try {
                    Json.decodeFromString<List<AppReleaseAsset>>(assetsJson)
                } catch (_: Exception) {
                    emptyList()
                }

                AppRelease(
                    versionName = version,
                    title = prefs[REL_TITLE_KEY] ?: version,
                    notes = prefs[REL_NOTES_KEY].orEmpty(),
                    pageUrl = prefs[REL_URL_KEY].orEmpty(),
                    publishedAt = prefs[REL_PUBLISHED_MS_KEY]?.let(Instant::ofEpochMilli),
                    isPreRelease = prefs[REL_PRERELEASE_KEY] ?: false,
                    assets = assetsList
                )
            }
        } else {
            null
        }
        PersistedUpdateState(
            lastCheckedAtMs = prefs[LAST_CHECKED_MS_KEY],
            available = available,
            release = release,
            lastNotifiedVersion = prefs[LAST_NOTIFIED_VERSION_KEY],
        )
    }

    /** Records an "up to date" outcome, clearing any previously stored available release. */
    suspend fun setUpToDate(checkedAtMs: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_CHECKED_MS_KEY] = checkedAtMs
            prefs[AVAILABLE_KEY] = false
            prefs.remove(REL_VERSION_KEY)
            prefs.remove(REL_TITLE_KEY)
            prefs.remove(REL_NOTES_KEY)
            prefs.remove(REL_URL_KEY)
            prefs.remove(REL_PUBLISHED_MS_KEY)
            prefs.remove(REL_PRERELEASE_KEY)
            prefs.remove(REL_ASSETS_KEY)
        }
    }

    /** Records an available release as the current status. */
    suspend fun setUpdateAvailable(release: AppRelease, checkedAtMs: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_CHECKED_MS_KEY] = checkedAtMs
            prefs[AVAILABLE_KEY] = true
            prefs[REL_VERSION_KEY] = release.versionName
            prefs[REL_TITLE_KEY] = release.title
            prefs[REL_NOTES_KEY] = release.notes
            prefs[REL_URL_KEY] = release.pageUrl
            release.publishedAt?.let { prefs[REL_PUBLISHED_MS_KEY] = it.toEpochMilli() }
                ?: prefs.remove(REL_PUBLISHED_MS_KEY)
            prefs[REL_PRERELEASE_KEY] = release.isPreRelease
            prefs[REL_ASSETS_KEY] = Json.encodeToString(release.assets)
        }
    }

    /** Marks [version] as the one the user has been notified about, suppressing repeat snackbars. */
    suspend fun setLastNotifiedVersion(version: String) {
        dataStore.edit { prefs -> prefs[LAST_NOTIFIED_VERSION_KEY] = version }
    }

    private companion object {
        val LAST_CHECKED_MS_KEY = longPreferencesKey("update_last_checked_ms")
        val AVAILABLE_KEY = booleanPreferencesKey("update_available")
        val REL_VERSION_KEY = stringPreferencesKey("update_release_version")
        val REL_TITLE_KEY = stringPreferencesKey("update_release_title")
        val REL_NOTES_KEY = stringPreferencesKey("update_release_notes")
        val REL_URL_KEY = stringPreferencesKey("update_release_url")
        val REL_PUBLISHED_MS_KEY = longPreferencesKey("update_release_published_ms")
        val REL_PRERELEASE_KEY = booleanPreferencesKey("update_release_prerelease")
        val REL_ASSETS_KEY = stringPreferencesKey("update_release_assets")
        val LAST_NOTIFIED_VERSION_KEY = stringPreferencesKey("update_last_notified_version")
    }
}

/**
 * Snapshot of the persisted update state.
 *
 * @property lastCheckedAtMs Epoch-ms of the last completed check, or null if none has run.
 * @property available Whether the last check found a newer release.
 * @property release The available release when [available] is true, else null.
 * @property lastNotifiedVersion The version the user was last shown a snackbar for, or null.
 */
data class PersistedUpdateState(
    val lastCheckedAtMs: Long?,
    val available: Boolean,
    val release: AppRelease?,
    val lastNotifiedVersion: String?,
)
