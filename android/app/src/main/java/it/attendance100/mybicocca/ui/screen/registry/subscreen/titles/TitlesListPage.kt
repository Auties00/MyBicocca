package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.model.document.TitleCategory
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.icon
import it.attendance100.mybicocca.ui.screen.registry.subscreen.titles.ext.labelRes

/**
 * Root page of the "Titoli" sheet: the student's qualifications as a category-grouped
 * directory list, one chevron row per title. Picking a title pushes its detail page in
 * place while the header morphs (back button slides in, title and subtitle crossfade)
 * rather than swapping. The sheet container, pinned morphing header and the
 * list-to-detail page transition are owned by BottomSheetSceneStrategy; the title detail
 * is a separate back-stack entry (SheetRoute.TitleDetail rendering [TitleDetailPage]).
 *
 * The ViewModel outlives the sheet (shell-scoped): a re-open shows the cached snapshot
 * instantly while a background refresh is kicked.
 */
@Composable
fun TitlesListPage(
    viewModel: TitlesViewModel,
    onOpenDetail: (titleId: String) -> Unit,
) {
    val titlesLoadable by viewModel.titles.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        if (viewModel.titles.value is Loadable.Loaded) viewModel.refresh()
    }

    TitlesListBody(
        titles = titlesLoadable.valueOrNull(),
        syncStatus = syncStatus,
        onRetry = viewModel::refresh,
        onTitleClick = { onOpenDetail(it.id) },
    )
}

/**
 * List body states: an error message with retry when the first load failed, a loading
 * indicator (held for a minimum beat so quick fetches don't flash it) until data lands,
 * an empty message when no titles are registered, and otherwise the rows grouped by
 * category, ordered HighSchool -> Italian -> Foreign to match life sequence.
 */
@Composable
private fun TitlesListBody(
    titles: List<AcademicTitle>?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onTitleClick: (AcademicTitle) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = titles == null)
    val settled = titles != null && !showLoading

    Box(modifier = Modifier.testTag(TitlesTestTags.ROOT)) {
    when {
        failure != null && titles == null -> Box(modifier = Modifier.testTag(TitlesTestTags.STATE_ERROR)) {
            SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.common_load_failed),
                body = stringResource(R.string.titles_load_failed_body),
                action = { RetryButton(onClick = onRetry) },
            )
        }

        !settled -> Box(modifier = Modifier.testTag(TitlesTestTags.STATE_LOADING)) {
            SheetLoadingIndicator(label = stringResource(R.string.titles_loading))
        }

        titles.isEmpty() -> Box(modifier = Modifier.testTag(TitlesTestTags.STATE_EMPTY)) {
            SheetMessage(
                icon = Icons.Outlined.School,
                title = stringResource(R.string.titles_empty_title),
                body = stringResource(R.string.titles_empty_body),
            )
        }

        else -> {
            val grouped = remember(titles) {
                titles.groupBy { it.category }
                    .toSortedMap(compareBy { it.ordinal })
            }
            LazyColumn(
                modifier = Modifier
                    .testTag(TitlesTestTags.STATE_CONTENT)
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                grouped.forEach { (category, items) ->
                    item(key = "header_${category.name}") {
                        CategoryHeader(category = category)
                    }
                    items.forEachIndexed { index, title ->
                        item(key = title.id) {
                            TitleRow(
                                title = title,
                                isFirst = index == 0,
                                isLast = index == items.lastIndex,
                                onClick = { onTitleClick(title) },
                                modifier = Modifier.testTag(TitlesTestTags.row(title.id)),
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun CategoryHeader(category: TitleCategory) {
    Text(
        text = stringResource(category.labelRes),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

/**
 * Segmented M3E group row: a circle category-icon chip, headline with a supporting
 * subtitle, and a trailing chevron. 28dp corners cap the group's ends, 6dp where rows
 * touch. Tapping pushes the title's detail page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TitleRow(
    title: AcademicTitle,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = scheme.primaryContainer) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = title.category.icon,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title.headline(),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                title.listSubtitle()?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * "3 titoli"-style count shown as the sheet's header subtitle; null when the list is
 * empty. Public so MainShell's sheet entry can build the pinned header from the
 * shell-hoisted ViewModel's titles.
 */
@Composable
fun titlesHeaderSubtitle(titles: List<AcademicTitle>): String? {
    if (titles.isEmpty()) return null
    return if (titles.size == 1) {
        stringResource(R.string.titles_count_one)
    } else {
        stringResource(R.string.titles_count_other, titles.size)
    }
}

/** Display headline: the subject when present, else the type description, else the institution. */
@Composable
fun AcademicTitle.headline(): String =
    subject?.takeIf { it.isNotBlank() }
        ?: typeDescription?.takeIf { it.isNotBlank() }
        ?: institution
        ?: stringResource(R.string.titles_headline_fallback)

/**
 * Detail-page header subtitle: the title type when the headline is the subject, else the
 * awarding institution.
 */
@Composable
fun AcademicTitle.headlineSubtitle(): String? {
    val headline = headline()
    return typeDescription?.takeIf { it.isNotBlank() && it != headline }
        ?: institution?.takeIf { it.isNotBlank() && it != headline }
}

@Composable
private fun AcademicTitle.listSubtitle(): String? {
    val headline = headline()
    val parts = buildList {
        typeDescription?.takeIf { it.isNotBlank() && it != headline }?.let { add(it) }
        institution?.takeIf { it.isNotBlank() && it != headline }?.let { add(it) }
        if (category == TitleCategory.Foreign) {
            country?.takeIf { it.isNotBlank() }?.let { add(it) }
        }
        year?.let { add(it) }
    }
    return parts.joinToString(" · ").takeIf { it.isNotBlank() }
}
