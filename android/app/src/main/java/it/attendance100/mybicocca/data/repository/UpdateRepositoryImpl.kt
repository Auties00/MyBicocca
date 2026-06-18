package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.version.SemVer
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.data.local.settings.UpdateStateStore
import it.attendance100.mybicocca.data.mapper.update.toAppReleaseOrNull
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
    private val _events = Channel<AppRelease>(Channel.BUFFERED)

    private val distributionSource: DistributionSource by lazy { installSourceProvider.resolve() }

    override val newUpdateEvents: Flow<AppRelease> = _events.receiveAsFlow()

    override fun observeStatus(): Flow<UpdateStatus> =
        store.state
            .map { persisted ->
                when {
                    persisted.lastCheckedAtMs == null -> UpdateStatus.Unknown
                    persisted.available && persisted.release != null ->
                        UpdateStatus.UpdateAvailable(persisted.release)

                    else -> UpdateStatus.UpToDate
                }
            }
            .distinctUntilChanged()

    override suspend fun checkForUpdates(force: Boolean): UpdateCheckResult = checkMutex.withLock {
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

        if (latest == null || !SemVer.isNewer(latest.versionName, BuildConfig.VERSION_NAME)) {
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

    override suspend fun releases(): List<AppRelease> = withContext(Dispatchers.IO) {
        githubApi.getReleases()
            .mapNotNull { it.toAppReleaseOrNull() }
            .sortedByDescending { it.publishedAt }
    }

    override fun updatePageUrl(release: AppRelease): String = when (distributionSource) {
        DistributionSource.GITHUB -> release.pageUrl
        DistributionSource.PLAY_STORE ->
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    }

    private fun PersistedUpdateState.toCheckResult(): UpdateCheckResult =
        if (available && release != null) UpdateCheckResult.UpdateAvailable(release)
        else UpdateCheckResult.UpToDate

    private companion object {
        const val DAILY_TTL_MS = 24L * 60 * 60 * 1000
    }
}
