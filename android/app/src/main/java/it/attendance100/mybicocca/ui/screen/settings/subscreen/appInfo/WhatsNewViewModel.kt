package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.release.MergedReleaseSource
import it.attendance100.mybicocca.core.release.ReleaseNotes
import it.attendance100.mybicocca.core.release.mergeReleaseNotes
import it.attendance100.mybicocca.core.release.parseReleaseNotes
import it.attendance100.mybicocca.core.version.SemVer
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.usecase.update.GetReleasesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Backs the "What's New" page. Loads the published releases once, parses each Markdown body into
 * renderable [ReleaseNotes] (off the main thread), and prepares two views the UI switches between:
 * a [MergedSummary] combining everything new since the installed build into one changelog, and the
 * full per-release list. Maps to a loading spinner, an error with [retry], or an empty state.
 */
@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getReleases: GetReleasesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<WhatsNewUiState>(WhatsNewUiState.Loading)
    val state: StateFlow<WhatsNewUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = WhatsNewUiState.Loading
        viewModelScope.launch {
            _state.value = runCatching {
                val releases = getReleases()
                withContext(Dispatchers.Default) { buildLoaded(releases) }
            }.fold(
                onSuccess = { it },
                onFailure = { WhatsNewUiState.Error },
            )
        }
    }

    /**
     * Parses every release and builds the merged summary only when the user is at least two
     * releases behind the installed [BuildConfig.VERSION_NAME]; up to date or a single release
     * behind, [MergedSummary] is null and the What's New page opens straight to the all-versions
     * list (merging one or zero releases would add a screen with nothing to combine).
     */
    private fun buildLoaded(releases: List<AppRelease>): WhatsNewUiState {
        val items = releases.map { ReleaseItem(it, parseReleaseNotes(it.notes)) }
        val latest =
            items.firstOrNull() ?: return WhatsNewUiState.Loaded(merged = null, releases = items)

        val newer =
            items.filter { SemVer.isNewer(it.release.versionName, BuildConfig.VERSION_NAME) }
        val merged = if (newer.size >= MERGE_THRESHOLD) {
            MergedSummary(
                latestVersion = latest.release.versionName,
                sinceVersion = BuildConfig.VERSION_NAME,
                latestPublishedAt = latest.release.publishedAt,
                latestPageUrl = latest.release.pageUrl,
                notes = mergeReleaseNotes(
                    sources = newer.map { MergedReleaseSource(it.release.versionName, it.notes) },
                    otherLabel = context.getString(R.string.whats_new_other_changes),
                ),
                mergedVersionCount = newer.size,
            )
        } else {
            null
        }

        return WhatsNewUiState.Loaded(merged = merged, releases = items)
    }

    private companion object {
        /** Below this many newer releases, the merged page is skipped for the all-versions list. */
        const val MERGE_THRESHOLD = 2
    }
}

/** A release paired with its body parsed into renderable note blocks. */
data class ReleaseItem(
    val release: AppRelease,
    val notes: ReleaseNotes,
)

/**
 * The merged "What's New" view: the latest release's identity (for the card header and its GitHub
 * link) over the [notes] combined from every version newer than [sinceVersion] (the installed
 * build). Only produced when at least two releases are newer; [mergedVersionCount] is how many were
 * folded in.
 */
data class MergedSummary(
    val latestVersion: String,
    val sinceVersion: String,
    val latestPublishedAt: Instant?,
    val latestPageUrl: String,
    val notes: ReleaseNotes,
    val mergedVersionCount: Int,
)

/** UI state for the "What's New" page. [Loaded] with a null [merged] / empty list is the empty state. */
sealed interface WhatsNewUiState {
    data object Loading : WhatsNewUiState
    data object Error : WhatsNewUiState
    data class Loaded(val merged: MergedSummary?, val releases: List<ReleaseItem>) : WhatsNewUiState
}
