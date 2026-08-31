package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import kotlinx.coroutines.flow.Flow

/**
 * App self-update contract. Hides which backend serves release metadata (GitHub today, the Play
 * listing later) and how an available update is delivered, behind a single surface: an observable
 * persisted [observeStatus] that drives the Settings tile, a one-shot [checkForUpdates] for the
 * manual button and periodic worker, [newUpdateEvents] for the app-wide "new version" snackbar,
 * the [releases] list behind the "What's New" page, [getLatestStableRelease] for switching back
 * off the beta channel, and a store-aware [updatePageUrl] for the tap target.
 */
interface UpdateRepository {

    /** The last completed check's persisted outcome; survives restarts. */
    fun observeStatus(): Flow<UpdateStatus>

    /**
     * Emits once each time a check discovers a version newer than the one the user was last
     * notified about, so the shell can raise the "new version available" snackbar — regardless of
     * whether that check ran in the foreground, from the periodic background worker, or was
     * force-refreshed. Suppressed per-call by [checkForUpdates]'s `announce` parameter for checks
     * that already surface their own result directly (the manual "Check for Updates" button), so
     * the user isn't shown the same discovery twice.
     */
    val newUpdateEvents: Flow<AppRelease>

    /**
     * Checks the distribution source for a newer release and updates the persisted status.
     * When [force] is false the check is skipped (returning the cached outcome) while the last
     * check is still within its daily freshness window; the manual button and periodic worker
     * pass true. [announce] controls [newUpdateEvents]/[newNightlyUpdateEvents] independently of
     * [force] — it's not "was this forced," it's "is anyone already being shown this result
     * directly," so pass false from call sites that already surface their own feedback UI.
     *
     * This answers "is there something newer than what's running" — deliberately unsuitable for
     * "restore to stable" (a nightly's version routinely equals or already exceeds the latest
     * stable tag, so this would report up-to-date and never surface a release to install); use
     * [getLatestStableRelease] for that instead.
     */
    suspend fun checkForUpdates(force: Boolean, announce: Boolean = true): UpdateCheckResult

    /**
     * Fetches the latest stable release directly, with no comparison against the running build —
     * for "restore to stable", which wants the current stable release regardless of whether it's
     * numerically newer than the nightly in use (see [checkForUpdates]'s doc for why that check
     * can't serve this). Not TTL-gated, not persisted to [observeStatus], never announces via
     * [newUpdateEvents] — a one-off, user-driven fetch, not part of the regular check machinery.
     * Result is always [UpdateCheckResult.Failed] or [UpdateCheckResult.UpdateAvailable], never
     * [UpdateCheckResult.UpToDate] in practice (that would require GitHub reporting zero releases
     * for a repo that demonstrably has at least one, its nightly).
     */
    suspend fun getLatestStableRelease(): UpdateCheckResult

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

    /** How often (in minutes) the periodic background update check fires. */
    fun observeCheckIntervalMinutes(): Flow<Int>
    suspend fun setCheckIntervalMinutes(minutes: Int)
}
