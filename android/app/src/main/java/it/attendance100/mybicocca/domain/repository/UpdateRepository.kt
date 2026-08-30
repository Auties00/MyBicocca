package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import kotlinx.coroutines.flow.Flow

/**
 * App self-update contract. Hides which backend serves release metadata (GitHub today, the Play
 * listing later) and how an available update is delivered, behind a single surface: an observable
 * persisted [observeStatus] that drives the Settings tile, a one-shot [checkForUpdates] for the
 * manual button, [newUpdateEvents] for the app-wide "new version" snackbar fired by the daily
 * check, the [releases] list behind the "What's New" page, and a store-aware [updatePageUrl] for
 * the tap target.
 */
interface UpdateRepository {

    /** The last completed check's persisted outcome; survives restarts. */
    fun observeStatus(): Flow<UpdateStatus>

    /**
     * Emits once each time the daily/background check discovers a version newer than the one the
     * user was last notified about, so the shell can raise the "new version available" snackbar
     * without nagging on every foreground. The manual check does not emit here — it returns its
     * outcome through [checkForUpdates] instead.
     */
    val newUpdateEvents: Flow<AppRelease>

    /**
     * Checks the distribution source for a newer release and updates the persisted status.
     * When [force] is false the check is skipped (returning the cached outcome) while the last
     * check is still within its daily freshness window; the manual button passes true.
     */
    suspend fun checkForUpdates(force: Boolean): UpdateCheckResult

    /** All published releases, newest first, for the "What's New" page. */
    suspend fun releases(): List<AppRelease>

    /** The store-aware destination for [release]: its GitHub page now, the Play listing later. */
    fun updatePageUrl(release: AppRelease): String

    /** Whether the beta/nightly channel is currently enabled. */
    fun observeNightlyEnabled(): Flow<Boolean>

    /** Enables or disables the nightly update channel. Disabling clears any pending nightly update immediately. */
    suspend fun setNightlyEnabled(enabled: Boolean)

    /** The last nightly check's persisted outcome. */
    fun observeNightlyStatus(): Flow<UpdateStatus>

    /** One-shot events for the nightly channel (mirrors [newUpdateEvents]). */
    val newNightlyUpdateEvents: Flow<AppRelease>

    fun observeStableAutoDownload(): Flow<Boolean>
    suspend fun setStableAutoDownload(enabled: Boolean)

    fun observeNightlyAutoDownload(): Flow<Boolean>
    suspend fun setNightlyAutoDownload(enabled: Boolean)

    fun observeNightlyAutoInstall(): Flow<Boolean>
    suspend fun setNightlyAutoInstall(enabled: Boolean)
}
