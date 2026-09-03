package it.attendance100.mybicocca.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import it.attendance100.mybicocca.core.version.isNightlyBuild
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES
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

    private fun Preferences.parseRelease(
        versionKey: Preferences.Key<String>,
        titleKey: Preferences.Key<String>,
        notesKey: Preferences.Key<String>,
        urlKey: Preferences.Key<String>,
        publishedMsKey: Preferences.Key<Long>,
        preReleaseKey: Preferences.Key<Boolean>?,
        assetsKey: Preferences.Key<String>,
        commitShaKey: Preferences.Key<String>? = null,
        isNightly: Boolean = false
    ): AppRelease? {
        val version = this[versionKey] ?: return null
        val assetsJson = this[assetsKey]
        val assetsList = if (assetsJson == null) emptyList() else {
            try {
                Json.decodeFromString<List<AppReleaseAsset>>(assetsJson)
            } catch (_: Exception) {
                emptyList()
            }
        }
        return AppRelease(
            versionName = version,
            title = this[titleKey] ?: if (isNightly) "Nightly Build" else version,
            notes = this[notesKey].orEmpty(),
            pageUrl = this[urlKey].orEmpty(),
            publishedAt = this[publishedMsKey]?.let(Instant::ofEpochMilli),
            isPreRelease = preReleaseKey?.let { this[it] } ?: isNightly,
            assets = assetsList,
            commitSha = commitShaKey?.let { this[it] }
        )
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.saveRelease(
        release: AppRelease,
        versionKey: Preferences.Key<String>,
        titleKey: Preferences.Key<String>,
        notesKey: Preferences.Key<String>,
        urlKey: Preferences.Key<String>,
        publishedMsKey: Preferences.Key<Long>,
        preReleaseKey: Preferences.Key<Boolean>?,
        assetsKey: Preferences.Key<String>,
        commitShaKey: Preferences.Key<String>? = null
    ) {
        this[versionKey] = release.versionName
        this[titleKey] = release.title
        this[notesKey] = release.notes
        this[urlKey] = release.pageUrl
        if (release.publishedAt != null) {
            this[publishedMsKey] = release.publishedAt.toEpochMilli()
        } else {
            this.remove(publishedMsKey)
        }
        if (preReleaseKey != null) {
            this[preReleaseKey] = release.isPreRelease
        }
        this[assetsKey] = Json.encodeToString(release.assets)
        if (commitShaKey != null) {
            if (release.commitSha != null) {
                this[commitShaKey] = release.commitSha
            } else {
                this.remove(commitShaKey)
            }
        }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.clearRelease(
        versionKey: Preferences.Key<String>,
        titleKey: Preferences.Key<String>,
        notesKey: Preferences.Key<String>,
        urlKey: Preferences.Key<String>,
        publishedMsKey: Preferences.Key<Long>,
        preReleaseKey: Preferences.Key<Boolean>?,
        assetsKey: Preferences.Key<String>,
        commitShaKey: Preferences.Key<String>? = null
    ) {
        this.remove(versionKey)
        this.remove(titleKey)
        this.remove(notesKey)
        this.remove(urlKey)
        this.remove(publishedMsKey)
        if (preReleaseKey != null) {
            this.remove(preReleaseKey)
        }
        this.remove(assetsKey)
        if (commitShaKey != null) {
            this.remove(commitShaKey)
        }
    }

    val state: Flow<PersistedUpdateState> = dataStore.data.map { prefs ->
        val available = prefs[AVAILABLE_KEY] ?: false
        val release = if (available) {
            prefs.parseRelease(
                versionKey = REL_VERSION_KEY,
                titleKey = REL_TITLE_KEY,
                notesKey = REL_NOTES_KEY,
                urlKey = REL_URL_KEY,
                publishedMsKey = REL_PUBLISHED_MS_KEY,
                preReleaseKey = REL_PRERELEASE_KEY,
                assetsKey = REL_ASSETS_KEY
            )
        } else null
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
            prefs.clearRelease(
                versionKey = REL_VERSION_KEY,
                titleKey = REL_TITLE_KEY,
                notesKey = REL_NOTES_KEY,
                urlKey = REL_URL_KEY,
                publishedMsKey = REL_PUBLISHED_MS_KEY,
                preReleaseKey = REL_PRERELEASE_KEY,
                assetsKey = REL_ASSETS_KEY
            )
        }
    }

    /** Records an available release as the current status. */
    suspend fun setUpdateAvailable(release: AppRelease, checkedAtMs: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_CHECKED_MS_KEY] = checkedAtMs
            prefs[AVAILABLE_KEY] = true
            prefs.saveRelease(
                release = release,
                versionKey = REL_VERSION_KEY,
                titleKey = REL_TITLE_KEY,
                notesKey = REL_NOTES_KEY,
                urlKey = REL_URL_KEY,
                publishedMsKey = REL_PUBLISHED_MS_KEY,
                preReleaseKey = REL_PRERELEASE_KEY,
                assetsKey = REL_ASSETS_KEY
            )
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

        val CHECK_INTERVAL_MINUTES_KEY = intPreferencesKey("update_check_interval_minutes")

        val STABLE_AUTO_DOWNLOAD_KEY = booleanPreferencesKey("update_stable_auto_download")
        val NIGHTLY_AUTO_DOWNLOAD_KEY = booleanPreferencesKey("update_nightly_auto_download")

        val DOWNLOADED_APK_PATH_KEY = stringPreferencesKey("update_downloaded_apk_path")
        val DOWNLOADED_APK_SIZE_KEY = longPreferencesKey("update_downloaded_apk_size")
        val DOWNLOADED_APK_VERSION_KEY = stringPreferencesKey("update_downloaded_apk_version")
        val DOWNLOADED_APK_COMMIT_SHA_KEY = stringPreferencesKey("update_downloaded_apk_commit_sha")

        val NIGHTLY_ENABLED_KEY = booleanPreferencesKey("update_nightly_enabled")
        val NIGHTLY_LAST_CHECKED_MS_KEY = longPreferencesKey("update_nightly_last_checked_ms")
        val NIGHTLY_LAST_PUBLISHED_MS_KEY = longPreferencesKey("update_nightly_last_published_ms")
        val NIGHTLY_LAST_DIGEST_KEY = stringPreferencesKey("update_nightly_last_digest")
        val NIGHTLY_AVAILABLE_KEY = booleanPreferencesKey("update_nightly_available")
        val NIGHTLY_REL_VERSION_NAME_KEY = stringPreferencesKey("update_nightly_rel_version_name")
        val NIGHTLY_REL_TITLE_KEY = stringPreferencesKey("update_nightly_rel_title")
        val NIGHTLY_REL_NOTES_KEY = stringPreferencesKey("update_nightly_rel_notes")
        val NIGHTLY_REL_URL_KEY = stringPreferencesKey("update_nightly_rel_url")
        val NIGHTLY_REL_PUBLISHED_MS_KEY = longPreferencesKey("update_nightly_rel_published_ms")
        val NIGHTLY_REL_ASSETS_KEY = stringPreferencesKey("update_nightly_rel_assets")
        val NIGHTLY_REL_COMMIT_SHA_KEY = stringPreferencesKey("update_nightly_rel_commit_sha")
    }

    /** How often (in minutes) the periodic background update check fires. */
    val checkIntervalMinutes: Flow<Int> =
        dataStore.data.map { it[CHECK_INTERVAL_MINUTES_KEY] ?: DEFAULT_UPDATE_CHECK_INTERVAL_MINUTES }

    suspend fun setCheckIntervalMinutes(minutes: Int) {
        dataStore.edit { it[CHECK_INTERVAL_MINUTES_KEY] = minutes }
    }

    // Off by default: stable releases are rare and large, so spending someone's data on one they
    // haven't asked for isn't a fair default. Nightlies are opt-in already, and whoever opted in
    // wants each build as it lands.
    val stableAutoDownload: Flow<Boolean> = dataStore.data.map { it[STABLE_AUTO_DOWNLOAD_KEY] ?: false }

    suspend fun setStableAutoDownload(enabled: Boolean) {
        dataStore.edit { it[STABLE_AUTO_DOWNLOAD_KEY] = enabled }
    }

    val nightlyAutoDownload: Flow<Boolean> = dataStore.data.map { it[NIGHTLY_AUTO_DOWNLOAD_KEY] ?: true }

    suspend fun setNightlyAutoDownload(enabled: Boolean) {
        dataStore.edit { it[NIGHTLY_AUTO_DOWNLOAD_KEY] = enabled }
    }

    /** A finished download waiting to be installed, remembered across process death. */
    val downloadedApk: Flow<DownloadedApk?> = dataStore.data.map { prefs ->
        val path = prefs[DOWNLOADED_APK_PATH_KEY] ?: return@map null
        DownloadedApk(
            path = path,
            size = prefs[DOWNLOADED_APK_SIZE_KEY] ?: 0L,
            versionName = prefs[DOWNLOADED_APK_VERSION_KEY].orEmpty(),
            commitSha = prefs[DOWNLOADED_APK_COMMIT_SHA_KEY],
        )
    }

    suspend fun setDownloadedApk(path: String, size: Long, versionName: String, commitSha: String?) {
        dataStore.edit { prefs ->
            prefs[DOWNLOADED_APK_PATH_KEY] = path
            prefs[DOWNLOADED_APK_SIZE_KEY] = size
            prefs[DOWNLOADED_APK_VERSION_KEY] = versionName
            if (commitSha != null) {
                prefs[DOWNLOADED_APK_COMMIT_SHA_KEY] = commitSha
            } else {
                prefs.remove(DOWNLOADED_APK_COMMIT_SHA_KEY)
            }
        }
    }

    suspend fun clearDownloadedApk() {
        dataStore.edit { prefs ->
            prefs.remove(DOWNLOADED_APK_PATH_KEY)
            prefs.remove(DOWNLOADED_APK_SIZE_KEY)
            prefs.remove(DOWNLOADED_APK_VERSION_KEY)
            prefs.remove(DOWNLOADED_APK_COMMIT_SHA_KEY)
        }
    }

    // Defaults on for a nightly build itself — you're already running one, so the toggle should
    // reflect that rather than making you re-enable the channel you're already on.
    val nightlyEnabled: Flow<Boolean> = dataStore.data.map { it[NIGHTLY_ENABLED_KEY] ?: isNightlyBuild }

    suspend fun setNightlyEnabled(enabled: Boolean) {
        dataStore.edit { it[NIGHTLY_ENABLED_KEY] = enabled }
    }

    val nightlyState: Flow<PersistedNightlyState> = dataStore.data.map { prefs ->
        val available = prefs[NIGHTLY_AVAILABLE_KEY] ?: false
        val release = if (available) {
            prefs.parseRelease(
                versionKey = NIGHTLY_REL_VERSION_NAME_KEY,
                titleKey = NIGHTLY_REL_TITLE_KEY,
                notesKey = NIGHTLY_REL_NOTES_KEY,
                urlKey = NIGHTLY_REL_URL_KEY,
                publishedMsKey = NIGHTLY_REL_PUBLISHED_MS_KEY,
                preReleaseKey = null,
                assetsKey = NIGHTLY_REL_ASSETS_KEY,
                commitShaKey = NIGHTLY_REL_COMMIT_SHA_KEY,
                isNightly = true
            )
        } else null
        PersistedNightlyState(
            lastCheckedAtMs = prefs[NIGHTLY_LAST_CHECKED_MS_KEY],
            lastSeenPublishedAtMs = prefs[NIGHTLY_LAST_PUBLISHED_MS_KEY],
            lastSeenDigest = prefs[NIGHTLY_LAST_DIGEST_KEY],
            available = available,
            release = release,
        )
    }

    suspend fun updateNightlyCheckedAt(checkedAtMs: Long) {
        dataStore.edit { prefs ->
            prefs[NIGHTLY_LAST_CHECKED_MS_KEY] = checkedAtMs
        }
    }

    suspend fun setNightlyUpToDate(checkedAtMs: Long) {
        dataStore.edit { prefs ->
            prefs[NIGHTLY_LAST_CHECKED_MS_KEY] = checkedAtMs
            prefs[NIGHTLY_AVAILABLE_KEY] = false
            prefs.clearRelease(
                versionKey = NIGHTLY_REL_VERSION_NAME_KEY,
                titleKey = NIGHTLY_REL_TITLE_KEY,
                notesKey = NIGHTLY_REL_NOTES_KEY,
                urlKey = NIGHTLY_REL_URL_KEY,
                publishedMsKey = NIGHTLY_REL_PUBLISHED_MS_KEY,
                preReleaseKey = null,
                assetsKey = NIGHTLY_REL_ASSETS_KEY,
                commitShaKey = NIGHTLY_REL_COMMIT_SHA_KEY
            )
        }
    }

    suspend fun setNightlyUpdateAvailable(
        release: AppRelease,
        publishedAtMs: Long,
        digest: String?,
        checkedAtMs: Long,
    ) {
        dataStore.edit { prefs ->
            prefs[NIGHTLY_LAST_CHECKED_MS_KEY] = checkedAtMs
            prefs[NIGHTLY_LAST_PUBLISHED_MS_KEY] = publishedAtMs
            digest?.let { prefs[NIGHTLY_LAST_DIGEST_KEY] = it }
                ?: prefs.remove(NIGHTLY_LAST_DIGEST_KEY)
            prefs[NIGHTLY_AVAILABLE_KEY] = true
            prefs.saveRelease(
                release = release,
                versionKey = NIGHTLY_REL_VERSION_NAME_KEY,
                titleKey = NIGHTLY_REL_TITLE_KEY,
                notesKey = NIGHTLY_REL_NOTES_KEY,
                urlKey = NIGHTLY_REL_URL_KEY,
                publishedMsKey = NIGHTLY_REL_PUBLISHED_MS_KEY,
                preReleaseKey = null,
                assetsKey = NIGHTLY_REL_ASSETS_KEY,
                commitShaKey = NIGHTLY_REL_COMMIT_SHA_KEY
            )
        }
    }

    suspend fun clearNightlyState() {
        dataStore.edit { prefs ->
            prefs[NIGHTLY_AVAILABLE_KEY] = false
            prefs.remove(NIGHTLY_LAST_CHECKED_MS_KEY)
            prefs.remove(NIGHTLY_LAST_PUBLISHED_MS_KEY)
            prefs.remove(NIGHTLY_LAST_DIGEST_KEY)
            prefs.clearRelease(
                versionKey = NIGHTLY_REL_VERSION_NAME_KEY,
                titleKey = NIGHTLY_REL_TITLE_KEY,
                notesKey = NIGHTLY_REL_NOTES_KEY,
                urlKey = NIGHTLY_REL_URL_KEY,
                publishedMsKey = NIGHTLY_REL_PUBLISHED_MS_KEY,
                preReleaseKey = null,
                assetsKey = NIGHTLY_REL_ASSETS_KEY,
                commitShaKey = NIGHTLY_REL_COMMIT_SHA_KEY
            )
        }
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

data class PersistedNightlyState(
    val lastCheckedAtMs: Long?,
    val lastSeenPublishedAtMs: Long?,
    val lastSeenDigest: String?,
    val available: Boolean,
    val release: AppRelease?,
)

/**
 * A downloaded-and-verified APK still waiting to be installed.
 *
 * @property path Absolute path of the file in the app's cache.
 * @property size Its size when it passed verification, re-checked before the record is trusted.
 * @property versionName The release's version, used to recognise an already-installed stable build.
 * @property commitSha The release's commit, or null when the source didn't give one. This is what
 *   identifies a nightly: two nightlies share a versionCode *and* a versionName, so the commit is
 *   the only thing that distinguishes the one on disk from the one already running.
 */
data class DownloadedApk(
    val path: String,
    val size: Long,
    val versionName: String,
    val commitSha: String?,
)
