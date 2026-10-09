package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.ProvideHapticManager
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo.component.ReleaseNotesView
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgLowest
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pinned header of the What's New entry page. The title always reads "What's New", so the page
 * never re-titles itself once the load resolves; the subtitle counts the releases merged since
 * the installed build (or listed, when there is nothing to merge), and is null while loading.
 */
@Composable
fun whatsNewHeader(viewModel: WhatsNewViewModel = hiltViewModel()): SheetHeaderSpec {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val merged = (state as? WhatsNewUiState.Loaded)?.merged
    return SheetHeaderSpec(
        title = stringResource(R.string.settings_whats_new_title),
        subtitle = if (merged != null) {
            pluralStringResource(
                R.plurals.whats_new_subtitle_merged,
                merged.mergedVersionCount,
                merged.mergedVersionCount,
                merged.sinceVersion,
            )
        } else {
            releasesSubtitle(state)
        },
    )
}

/** Pinned header of the "All versions" page: how many releases the list holds. */
@Composable
fun whatsNewAllVersionsHeader(viewModel: WhatsNewViewModel = hiltViewModel()): SheetHeaderSpec {
    val state by viewModel.state.collectAsStateWithLifecycle()
    return SheetHeaderSpec(
        title = stringResource(R.string.whats_new_all_versions),
        subtitle = releasesSubtitle(state),
    )
}

/** Release count once loaded; the installed version when there is no list to count. */
@Composable
private fun releasesSubtitle(state: WhatsNewUiState): String? = when (state) {
    WhatsNewUiState.Loading -> null
    WhatsNewUiState.Error -> stringResource(R.string.whats_new_subtitle_installed, BuildConfig.VERSION_NAME)
    is WhatsNewUiState.Loaded -> if (state.releases.isEmpty()) {
        stringResource(R.string.whats_new_subtitle_installed, BuildConfig.VERSION_NAME)
    } else {
        pluralStringResource(R.plurals.whats_new_subtitle_versions, state.releases.size, state.releases.size)
    }
}

/**
 * The What's New entry page (depth 1 of the About modal, titled by [whatsNewHeader]). When the
 * user is two or more releases behind it shows the merged changelog (one combined, version-tagged
 * card) under an "All versions" action that pushes [WhatsNewAllVersionsScene]; otherwise — up to
 * date or one release behind, so there is nothing to merge — it shows the all-versions list
 * directly. Sized to fill the height handed in via [modifier].
 */
@Composable
fun WhatsNewScene(
    onAllVersions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhatsNewEntryContent(
        state = state,
        onAllVersions = onAllVersions,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * The "All versions" page (depth 2, reached from the merged page's button, titled by
 * [whatsNewAllVersionsHeader]): every release newest-first as its own card with its full,
 * unmerged notes, each opening that release's GitHub page. Shares the [WhatsNewViewModel]
 * instance (and therefore the single load) with the entry.
 */
@Composable
fun WhatsNewAllVersionsScene(
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhatsNewAllVersionsContent(
        state = state,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
private fun WhatsNewEntryContent(
    state: WhatsNewUiState,
    onAllVersions: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val merged = (state as? WhatsNewUiState.Loaded)?.merged

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        when (state) {
            WhatsNewUiState.Loading -> CenteredBlock(Modifier.weight(1f)) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }

            WhatsNewUiState.Error -> CenteredBlock(Modifier.weight(1f)) { ErrorContent(onRetry) }

            is WhatsNewUiState.Loaded -> when {
                merged != null -> MergedNotes(
                    merged = merged,
                    onAllVersions = onAllVersions,
                    modifier = Modifier.weight(1f),
                )
                state.releases.isEmpty() -> CenteredBlock(Modifier.weight(1f)) { NoVersionsContent() }
                else -> ReleaseList(releases = state.releases, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WhatsNewAllVersionsContent(
    state: WhatsNewUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        when (state) {
            WhatsNewUiState.Loading -> CenteredBlock(Modifier.weight(1f)) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }

            WhatsNewUiState.Error -> CenteredBlock(Modifier.weight(1f)) { ErrorContent(onRetry) }

            is WhatsNewUiState.Loaded ->
                if (state.releases.isEmpty()) {
                    CenteredBlock(Modifier.weight(1f)) { NoVersionsContent() }
                } else {
                    ReleaseList(releases = state.releases, modifier = Modifier.weight(1f))
                }
        }
    }
}

/** The newest-first list of per-release cards, shared by the entry (no-merge) and All-versions pages. */
@Composable
private fun ReleaseList(releases: List<ReleaseItem>, modifier: Modifier = Modifier) {
    val locale = currentLocale()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(releases, key = { it.release.versionName }) { item ->
            ReleaseCard(item = item, dateFormatter = dateFormatter)
        }
    }
}

/**
 * The merged changelog: a caption ("changes since vX") beside the "All versions" action, above a
 * single release-style [ReleaseCard] built from the latest release's header and the combined,
 * version-tagged notes — so it reads like an ordinary release card, just with a per-line version
 * chip and the whole-card tap opening the latest release.
 */
@Composable
private fun MergedNotes(
    merged: MergedSummary,
    onAllVersions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val locale = currentLocale()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
    val mergedItem = ReleaseItem(
        release = AppRelease(
            versionName = merged.latestVersion,
            title = merged.latestVersion,
            notes = "",
            pageUrl = merged.latestPageUrl,
            publishedAt = merged.latestPublishedAt,
            isPreRelease = false,
        ),
        notes = merged.notes,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.whats_new_merged_subtitle, merged.sinceVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { haptic.tap(); onAllVersions() }) {
                Text(stringResource(R.string.whats_new_all_versions))
            }
        }
        ReleaseCard(item = mergedItem, dateFormatter = dateFormatter)
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    val haptic = rememberHapticManager()
    EmptyState(
        icon = Icons.Outlined.ErrorOutline,
        title = stringResource(R.string.whats_new_error),
        body = "",
        action = {
            TextButton(onClick = { haptic.tap(); onRetry() }) {
                Text(stringResource(R.string.whats_new_retry))
            }
        },
    )
}

@Composable
private fun NoVersionsContent() {
    EmptyState(
        icon = Icons.Outlined.NewReleases,
        title = stringResource(R.string.settings_no_other_versions),
        body = "",
    )
}

/** Fills the space the header leaves and centers its content (loading / empty / error states). */
@Composable
private fun CenteredBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * One release as a standalone card: a header (title, version + date, link glyph) above the parsed
 * release notes recreated GitHub-style by [ReleaseNotesView]. The whole card is the single tap
 * target and opens that release's page in an in-app Custom Tab; inline links inside the notes are
 * styled but not separately tappable. Releases with no notes show just the header.
 */
@Composable
private fun ReleaseCard(
    item: ReleaseItem,
    dateFormatter: DateTimeFormatter,
) {
    val release = item.release
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = rememberHapticManager()
    val published = release.publishedAt
        ?.atZone(ZoneId.systemDefault())
        ?.let { dateFormatter.format(it) }
    val meta = listOfNotNull("v${release.versionName}", published).joinToString(" · ")

    Surface(
        onClick = {
            haptic.tap()
            CustomTabsIntent.Builder().setShowTitle(true).build()
                .launchUrl(context, release.pageUrl.toUri())
        },
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 16.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.app_name) + " " + release.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            if (!item.notes.isEmpty) {
                Spacer(Modifier.height(14.dp))
                ReleaseNotesView(notes = item.notes)
            }
        }
    }
}

// region Previews

@Preview(name = "Release Card · Light", group = "Mock Releases", showSystemUi = false)
@Composable
private fun ReleaseCardPreviewLight(
    @PreviewParameter(ReleaseItemProvider::class) item: ReleaseItem
) {
    BicoccaTheme(dark = false) {
        val locale = currentLocale()
        val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(16.dp)) {
                ReleaseCard(item = item, dateFormatter = dateFormatter)
            }
        }
    }
}

@Preview(
    name = "Release Card · Dark",
    group = "Mock Releases",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgLowest,
    showBackground = true
)

@Composable
private fun ReleaseCardPreviewDark(
    @PreviewParameter(ReleaseItemProvider::class) item: ReleaseItem
) {
    BicoccaTheme(dark = true) {
        val locale = currentLocale()
        val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(16.dp)) {
                ReleaseCard(item = item, dateFormatter = dateFormatter)
            }
        }
    }
}

@Composable
private fun WhatsNewMergedPreviewContent() {
    ProvideHapticManager(enabled = true) {
        WhatsNewEntryContent(
            state = mockLoadedState(),
            onAllVersions = {},
            onRetry = {},
        )
    }
}

/** The entry page when there is nothing to merge (0–1 newer): it shows the list directly. */
@Composable
private fun WhatsNewEntryNoMergePreviewContent() {
    ProvideHapticManager(enabled = true) {
        WhatsNewEntryContent(
            state = mockLoadedState().copy(merged = null),
            onAllVersions = {},
            onRetry = {},
        )
    }
}

@Composable
private fun WhatsNewAllVersionsPreviewContent() {
    ProvideHapticManager(enabled = true) {
        WhatsNewAllVersionsContent(
            state = mockLoadedState(),
            onRetry = {},
        )
    }
}

@Preview(name = "What's New · Merged · Light")
@Composable
private fun WhatsNewMergedPreviewLight() {
    BicoccaTheme(dark = false) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewMergedPreviewContent() }
    }
}

@Preview(
    name = "What's New · Merged · Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgLowest,
)
@Composable
private fun WhatsNewMergedPreviewDark() {
    BicoccaTheme(dark = true) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewMergedPreviewContent() }
    }
}

@Preview(name = "What's New · Entry (no merge) · Light")
@Composable
private fun WhatsNewEntryNoMergePreviewLight() {
    BicoccaTheme(dark = false) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewEntryNoMergePreviewContent() }
    }
}

@Preview(
    name = "What's New · Entry (no merge) · Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgLowest,
)
@Composable
private fun WhatsNewEntryNoMergePreviewDark() {
    BicoccaTheme(dark = true) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewEntryNoMergePreviewContent() }
    }
}

@Preview(name = "What's New · All versions · Light")
@Composable
private fun WhatsNewAllVersionsPreviewLight() {
    BicoccaTheme(dark = false) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewAllVersionsPreviewContent() }
    }
}

@Preview(
    name = "What's New · All versions · Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgLowest,
)
@Composable
private fun WhatsNewAllVersionsPreviewDark() {
    BicoccaTheme(dark = true) {
        Surface(color = MaterialTheme.colorScheme.background) { WhatsNewAllVersionsPreviewContent() }
    }
}

// endregion
