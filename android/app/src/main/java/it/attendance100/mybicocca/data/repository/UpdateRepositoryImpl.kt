package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.version.SemVer
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.mapper.update.toAppReleaseOrNull
import it.attendance100.mybicocca.data.mapper.update.toNightlyAppReleaseOrNull
import it.attendance100.mybicocca.data.update.GithubReleaseApi
import it.attendance100.mybicocca.data.update.InstallSourceProvider
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DistributionSource
import it.attendance100.mybicocca.domain.model.update.UpdateCheckResult
import it.attendance100.mybicocca.domain.model.update.UpdateStatus
import it.attendance100.mybicocca.domain.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Update repository over the GitHub Releases API and the persisted [UpdateStateStore].
 *
 * The persisted state is the single source of truth the Settings tile observes; every check
 * writes through to it so an available update survives restarts and shows whichever trigger
 * found it. [checkForUpdates] is TTL-gated (a day) unless forced by the manual button, and
 * serialized by a mutex so a manual tap and the foreground daily check never race the network
 * or the store. Only the background path emits [newUpdateEvents], and only for a version the
 * user has not already been notified about, so the app-wide snackbar fires once per new release
 * rather than on every foreground.
 *
 * Version comparison is installed [BuildConfig.VERSION_NAME] vs the release tag, via [SemVer];
 * an unparseable tag is treated as not-newer rather than a phantom update.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val githubApi: GithubReleaseApi,
    private val store: UpdateStateStore,
    private val installSourceProvider: InstallSourceProvider,
) : UpdateRepository {

    private val checkMutex = Mutex()
    private val nightlyMutex = Mutex()
    private val _events = Channel<AppRelease>(Channel.BUFFERED)
    private val _nightlyEvents = Channel<AppRelease>(Channel.BUFFERED)

    private val distributionSource: DistributionSource by lazy { installSourceProvider.resolve() }

    override val newUpdateEvents: Flow<AppRelease> = _events.receiveAsFlow()
    override val newNightlyUpdateEvents: Flow<AppRelease> = _nightlyEvents.receiveAsFlow()

    override fun observeNightlyEnabled(): Flow<Boolean> = store.nightlyEnabled

    override suspend fun setNightlyEnabled(enabled: Boolean) {
        store.setNightlyEnabled(enabled)
        if (!enabled) {
            store.clearNightlyState()
        } else {
            // Immediately run a forced check to populate the state
            checkForNightlyUpdate(force = true)
        }
    }

    override fun observeStableAutoDownload(): Flow<Boolean> = store.stableAutoDownload
    override suspend fun setStableAutoDownload(enabled: Boolean) = store.setStableAutoDownload(enabled)

    override fun observeNightlyAutoDownload(): Flow<Boolean> = store.nightlyAutoDownload
    override suspend fun setNightlyAutoDownload(enabled: Boolean) = store.setNightlyAutoDownload(enabled)

    override fun observeNightlyAutoInstall(): Flow<Boolean> = store.nightlyAutoInstall
    override suspend fun setNightlyAutoInstall(enabled: Boolean) = store.setNightlyAutoInstall(enabled)

    override fun observeCheckIntervalMinutes(): Flow<Int> = store.checkIntervalMinutes
    override suspend fun setCheckIntervalMinutes(minutes: Int) = store.setCheckIntervalMinutes(minutes)

    override fun observeNightlyStatus(): Flow<UpdateStatus> =
        store.nightlyState
            .map { persisted ->
                when {
                    persisted.lastCheckedAtMs == null -> UpdateStatus.Unknown
                    persisted.available && persisted.release != null && 
                            persisted.release.commitSha != BuildConfig.COMMIT_SHA ->
                        UpdateStatus.UpdateAvailable(persisted.release)
                    else -> UpdateStatus.UpToDate
                }
            }
            .distinctUntilChanged()

    override fun observeStatus(): Flow<UpdateStatus> =
        store.state
            .map { persisted ->
                when {
                    persisted.lastCheckedAtMs == null -> UpdateStatus.Unknown
                    // Re-validate the persisted flag against the running build: once the user has
                    // installed the update, the release is no longer newer than us, so the tile
                    // clears immediately instead of waiting for the next daily check to rewrite it.
                    persisted.available && persisted.release != null &&
                            SemVer.isNewer(
                                persisted.release.versionName,
                                BuildConfig.VERSION_NAME.substringBefore("-")
                            ) ->
                        UpdateStatus.UpdateAvailable(persisted.release)

                    else -> UpdateStatus.UpToDate
                }
            }
            .distinctUntilChanged()

    override suspend fun checkForUpdates(force: Boolean): UpdateCheckResult = coroutineScope {
        // Trigger nightly check in parallel (it has its own internal error boundary)
        val nightlyJob = if (store.nightlyEnabled.first()) {
            async { checkForNightlyUpdate(force) }
        } else null
        
        val stableResult = checkMutex.withLock {
            val current = store.state.first()
            val now = System.currentTimeMillis()

            if (!force && current.lastCheckedAtMs != null && now - current.lastCheckedAtMs < DAILY_TTL_MS) {
                return@withLock current.toCheckResult()
            }

            val latest = try {
                withContext(Dispatchers.IO) { githubApi.getLatestRelease()?.toAppReleaseOrNull() }
            } catch (cause: Throwable) {
                return@withLock UpdateCheckResult.Failed(cause)
            }

            if (latest == null) {
                store.setUpToDate(now)
                return@withLock UpdateCheckResult.UpToDate
            }

            val isNightly = BuildConfig.VERSION_NAME.contains("nightly", ignoreCase = true)
            val currentVersion = BuildConfig.VERSION_NAME.substringBefore("-")
            val isNewer = SemVer.isNewer(latest.versionName, currentVersion)
            // A nightly is newer than its stable base, so we shouldn't allow re-installing the stable version
            // over it just because the base versions match.
            val isSameAndForced = force && latest.versionName == currentVersion && !isNightly

            if (!isNewer && !isSameAndForced) {
                store.setUpToDate(now)
                return@withLock UpdateCheckResult.UpToDate
            }

            store.setUpdateAvailable(latest, now)

            // First time this version surfaces, mark it notified so it is announced at most once. The
            // app-wide snackbar is raised only by the silent daily check; the manual button shows its
            // own result in the About sheet, so it marks-but-does-not-emit to avoid a later repeat.
            if (latest.versionName != current.lastNotifiedVersion) {
                store.setLastNotifiedVersion(latest.versionName)
                if (!force) _events.trySend(latest)
            }

            UpdateCheckResult.UpdateAvailable(latest)
        }
        
        val nightlyResult = nightlyJob?.await()
        if (nightlyResult is UpdateCheckResult.UpdateAvailable) {
            return@coroutineScope nightlyResult
        }
        return@coroutineScope stableResult
    }

    private suspend fun checkForNightlyUpdate(force: Boolean): UpdateCheckResult? = nightlyMutex.withLock {
        val current = store.nightlyState.first()
        val now = System.currentTimeMillis()

        if (!force && current.lastCheckedAtMs != null) {
            val currentSlot = now / (30L * 60 * 1000)
            val lastSlot = current.lastCheckedAtMs / (30L * 60 * 1000)
            if (currentSlot == lastSlot) return@withLock null
        }

        val remoteDto = try {
            withContext(Dispatchers.IO) { githubApi.getReleaseByTag("nightly") }
        } catch (cause: Throwable) {
            // Nightly checks silently swallow errors since it's an internal beta feature
            return@withLock null
        }

        if (remoteDto == null) {
            store.setNightlyUpToDate(now)
            return@withLock null
        }

        val remotePublishedMs = runCatching { java.time.Instant.parse(remoteDto.publishedAt).toEpochMilli() }.getOrNull()
        if (remotePublishedMs == null) {
            store.setNightlyUpToDate(now)
            return@withLock null
        }

        val remoteDigest = remoteDto.assets.firstOrNull()?.digest

        // Freshness check: if both timestamp and digest match what we last saw, it's not new.
        // If digest is null (missing on GitHub), we rely solely on timestamp.
        val isNew = current.lastSeenPublishedAtMs != remotePublishedMs ||
                (remoteDigest != null && current.lastSeenDigest != remoteDigest)

        if (!isNew) {
            store.updateNightlyCheckedAt(now)
            return@withLock null
        }

        val release = remoteDto.toNightlyAppReleaseOrNull(remotePublishedMs) ?: return@withLock null
        store.setNightlyUpdateAvailable(release, remotePublishedMs, remoteDigest, now)
        _nightlyEvents.trySend(release)
        
        UpdateCheckResult.UpdateAvailable(release)
    }

    override suspend fun releases(): List<AppRelease> = withContext(Dispatchers.IO) {
        val stablereleases = githubApi.getReleases().mapNotNull { it.toAppReleaseOrNull() }
        
        // Include nightly in the What's New page if channel is enabled
        val allReleases = if (store.nightlyEnabled.first()) {
            val nightlyState = store.nightlyState.first()
            if (nightlyState.available && nightlyState.release != null) {
                stablereleases + nightlyState.release
            } else stablereleases
        } else {
            stablereleases
        }
        
        allReleases.sortedByDescending { it.publishedAt }
    }

    override fun updatePageUrl(release: AppRelease): String = when (distributionSource) {
        DistributionSource.GITHUB -> release.pageUrl
        DistributionSource.PLAY_STORE ->
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    }

    private fun PersistedUpdateState.toCheckResult(): UpdateCheckResult =
        if (available && release != null && SemVer.isNewer(
                release.versionName,
                BuildConfig.VERSION_NAME.substringBefore("-")
            )
        )
            UpdateCheckResult.UpdateAvailable(release)
        else UpdateCheckResult.UpToDate

    private companion object {
        const val DAILY_TTL_MS = 24L * 60 * 60 * 1000
        const val NIGHTLY_TTL_MS = 12L * 60 * 60 * 1000
    }
}
