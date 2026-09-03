package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.version.SemVer
import it.attendance100.mybicocca.core.version.isNightlyBuild
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.mapper.update.toAppReleaseOrNull
import it.attendance100.mybicocca.data.mapper.update.toNightlyAppReleaseOrNull
import it.attendance100.mybicocca.data.update.ApkDownloader
import it.attendance100.mybicocca.data.update.GithubReleaseApi
import it.attendance100.mybicocca.data.update.InstallSourceProvider
import it.attendance100.mybicocca.data.update.availableRelease
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.model.update.DistributionSource
import it.attendance100.mybicocca.domain.model.update.DownloadState
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
import kotlinx.coroutines.flow.StateFlow
import java.io.File
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
 * writes through to it, and is serialized by a mutex so a manual tap and a background check never
 * race the network or the store. `announce` is independent of `force`: a version is marked
 * "already notified" the first time any check discovers it, whether or not that check announced
 * it, so a suppressed discovery still blocks a later announcing check from re-raising the
 * snackbar for a version the user already saw directly.
 *
 * Version comparison is installed [BuildConfig.VERSION_NAME] vs the release tag, via [SemVer];
 * an unparseable tag is treated as not-newer rather than a phantom update.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val githubApi: GithubReleaseApi,
    private val store: UpdateStateStore,
    private val installSourceProvider: InstallSourceProvider,
    private val apkDownloader: ApkDownloader,
) : UpdateRepository {

    override val downloadState: StateFlow<DownloadState> = apkDownloader.downloadState

    override fun startDownload(release: AppRelease) = apkDownloader.startDownload(release)

    override fun installApk(file: File) = apkDownloader.installApk(file)

    override fun resetDownload() = apkDownloader.resetState()

    override fun dismissDownloadError() = apkDownloader.dismissError()

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
            // Forced check to populate the state; not announced, the user is already looking at
            // the toggle they just flipped.
            checkForNightlyUpdate(force = true, announce = false)
        }
    }

    override fun observeStableAutoDownload(): Flow<Boolean> = store.stableAutoDownload
    override suspend fun setStableAutoDownload(enabled: Boolean) = store.setStableAutoDownload(enabled)

    override fun observeNightlyAutoDownload(): Flow<Boolean> = store.nightlyAutoDownload
    override suspend fun setNightlyAutoDownload(enabled: Boolean) = store.setNightlyAutoDownload(enabled)


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

    override suspend fun checkForUpdates(force: Boolean, announce: Boolean): UpdateCheckResult = coroutineScope {
        // Trigger nightly check in parallel (it has its own internal error boundary)
        val nightlyJob = if (store.nightlyEnabled.first()) {
            async { checkForNightlyUpdate(force, announce) }
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

            val currentVersion = BuildConfig.VERSION_NAME.substringBefore("-")
            val isNewer = SemVer.isNewer(latest.versionName, currentVersion)
            // A nightly is newer than its stable base, so matching base versions isn't "same".
            val isSameAndForced = force && latest.versionName == currentVersion && !isNightlyBuild

            if (!isNewer && !isSameAndForced) {
                store.setUpToDate(now)
                return@withLock UpdateCheckResult.UpToDate
            }

            store.setUpdateAvailable(latest, now)

            // First time this version surfaces, mark it notified regardless of whether it's announced.
            if (latest.versionName != current.lastNotifiedVersion) {
                store.setLastNotifiedVersion(latest.versionName)
                if (announce) _events.trySend(latest)
            }

            UpdateCheckResult.UpdateAvailable(latest)
        }
        
        val nightlyResult = nightlyJob?.await()
        if (nightlyResult is UpdateCheckResult.UpdateAvailable) {
            return@coroutineScope nightlyResult
        }
        return@coroutineScope stableResult
    }

    override suspend fun availableRelease(): AppRelease? =
        store.state.first().availableRelease() ?: store.nightlyState.first().availableRelease()

    override suspend fun getLatestStableRelease(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val latest = githubApi.getLatestRelease()?.toAppReleaseOrNull()
            if (latest != null) UpdateCheckResult.UpdateAvailable(latest) else UpdateCheckResult.UpToDate
        } catch (cause: Throwable) {
            UpdateCheckResult.Failed(cause)
        }
    }

    private suspend fun checkForNightlyUpdate(force: Boolean, announce: Boolean): UpdateCheckResult? = nightlyMutex.withLock {
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
        if (announce) _nightlyEvents.trySend(release)

        UpdateCheckResult.UpdateAvailable(release)
    }

    override suspend fun releases(): List<AppRelease> = withContext(Dispatchers.IO) {
        // The nightly is a real GitHub release (rolling, tag "nightly"), so it comes back in this
        // list too — but the generic mapper renders it off its raw tag, and it has no business
        // showing at all to someone who never opted into the channel. It's added from the persisted
        // nightly state below instead, which maps it properly.
        val stableReleases = githubApi.getReleases()
            .filterNot { it.prerelease }
            .mapNotNull { it.toAppReleaseOrNull() }

        val nightly = if (store.nightlyEnabled.first()) {
            store.nightlyState.first().takeIf { it.available }?.release
        } else {
            null
        }

        // Keyed on the release page rather than the version: the two mappings of a nightly don't
        // agree on a version name (one uses the "nightly" tag, the other a formatted publish date),
        // but both carry the same GitHub URL. Nightly goes first so it survives as the richer copy.
        (listOfNotNull(nightly) + stableReleases)
            .distinctBy { it.pageUrl }
            .sortedByDescending { it.publishedAt }
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
