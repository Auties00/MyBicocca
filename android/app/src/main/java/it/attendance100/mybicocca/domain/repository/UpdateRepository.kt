package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DownloadState
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

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

    /** Raises the shell's "new version available" snackbar. See [checkForUpdates]'s `announce`. */
    val newUpdateEvents: Flow<AppRelease>

    /**
     * Checks the distribution source for a newer release and updates the persisted status. When
     * [force] is false the check is skipped (returning the cached outcome) within the daily
     * freshness window. [announce] is independent of [force]: it's "is anyone already being shown
     * this result directly," so pass false from call sites that surface their own feedback UI.
     *
     * Answers "is there something newer than what's running" — unsuitable for "restore to stable"
     * (see [getLatestStableRelease]).
     */
    suspend fun checkForUpdates(force: Boolean, announce: Boolean = true): UpdateCheckResult

    /**
     * Fetches the latest stable release with no comparison against the running build, for
     * "restore to stable": a nightly's version routinely equals or already exceeds the latest
     * stable tag, so [checkForUpdates] would report up-to-date and never surface it. Not
     * TTL-gated, not persisted, never announces — a one-off fetch outside the regular check flow.
     */
    suspend fun getLatestStableRelease(): UpdateCheckResult

    /** All published releases, newest first, for the "What's New" page. */
    suspend fun releases(): List<AppRelease>

    /**
     * The release currently worth offering, stable first then nightly, or null if there is none.
     *
     * Reads the persisted state rather than the network, for a caller acting on something the
     * user has already been told about — a notification tap, which can arrive long after the
     * check that produced it and even in a later process.
     */
    suspend fun availableRelease(): AppRelease?

    /**
     * The in-flight update download, so a screen can reflect it without depending on whatever
     * performs the download.
     */
    val downloadState: StateFlow<DownloadState>

    /** Starts downloading [release] in the background, if nothing is downloading already. */
    fun startDownload(release: AppRelease)

    /** Opens the system installer for a downloaded APK. The user always confirms. */
    fun installApk(file: File)

    /** Forgets a finished or failed download, including the persisted record of the APK. */
    fun resetDownload()

    /** Clears an error state without touching anything else. */
    fun dismissDownloadError()

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

    /** How often (in minutes) the periodic background update check fires. */
    fun observeCheckIntervalMinutes(): Flow<Int>
    suspend fun setCheckIntervalMinutes(minutes: Int)
}
