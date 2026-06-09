package it.attendance100.mybicocca.ui.screen.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.search.SearchResultCategory
import it.attendance100.mybicocca.ui.component.bar.fadeThroughExpanded
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.search.component.SearchHistoryRow
import it.attendance100.mybicocca.ui.screen.search.component.SearchResultRow
import it.attendance100.mybicocca.ui.screen.search.subscreen.dictation.DictationDialog

// Full-screen search body following the M3 search-view anatomy: the app bar above acts as
// the header (surfaceContainer), a full-width outlineVariant divider marks the seam, and
// this content area sits one tonal step below on plain surface — separation comes from the
// tonal ladder, not shadows. Not a nav route: it rides the bar's own searchProgress, so the
// predictive-back gesture that scrubs the bar collapse scrubs this overlay in lockstep.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    viewModel: SearchViewModel,
    progress: Float,
    // How far a sub-page covers the shell. Search can stay alive under a pushed sub-page:
    // the overlay fades out for the push (and back in as the pop scrubs) but stays composed,
    // so query, results and scroll position survive the round trip.
    subPageProgress: Float,
    topInset: Dp,
    onOpenResult: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val dictating by viewModel.dictating.collectAsStateWithLifecycle()
    val soundLevel by viewModel.soundLevel.collectAsStateWithLifecycle()

    // Rides the dictating flag rather than a route: the session can also end on its own
    // (final transcript, recognizer timeout) and the dialog must follow it out.
    DictationDialog(
        visible = dictating,
        transcript = query,
        soundLevel = soundLevel,
        onFinish = viewModel::stopDictation,
    )

    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    // M3: the keyboard retracts as soon as the user starts browsing the results.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling -> if (scrolling) keyboard?.hide() }
    }
    // The dictated text lands cursor-at-end in the field (see SearchFieldContent); raising
    // the keyboard right as the dialog leaves invites refining it by typing.
    var wasDictating by remember { mutableStateOf(false) }
    LaunchedEffect(dictating) {
        if (wasDictating && !dictating) keyboard?.show()
        wasDictating = dictating
    }

    val scheme = MaterialTheme.colorScheme
    // Vertical pull on the history wipes it whole: the pull-to-refresh machinery with a
    // delete badge instead of a spinner. Single rows go with a horizontal swipe.
    val pullState = rememberPullToRefreshState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Fade-through over the upper half of the morph (same ramp as the bar's
                // expanded content) plus a subtle grow-down from under the bar, so the body
                // reads as unfolding out of the pill rather than popping in. A covering
                // sub-page fades it out symmetrically (and the predictive-back scrub fades
                // it back in).
                alpha = fadeThroughExpanded(progress) * (1f - subPageProgress)
                // Alpha 0 alone would keep the overlay hit-testable above the settled
                // sub-page; shifting the layer off-screen drops it from hit testing while
                // keeping it composed, so list state survives.
                if (alpha == 0f) translationY = size.height
                transformOrigin = TransformOrigin(0.5f, 0f)
                val scale = 0.96f + 0.04f * progress
                scaleX = scale
                scaleY = scale
            }
            // Plain surface, one tonal step below the bar's surfaceContainer header. The
            // opaque background plus the no-op clickable also swallow taps meant for the
            // hidden tab underneath.
            .background(scheme.surface)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset),
        ) {
            // Pinned seam between header and content — outlineVariant per the divider role
            // (outline is reserved for important boundaries like text fields).
            HorizontalDivider(color = scheme.outlineVariant)

            // While the keyboard is up, a downward pull is keyboard territory (the top-half
            // dismiss gesture) — the wipe must not arm under it.
            val keyboardOpen = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .pullToRefresh(
                        isRefreshing = false,
                        state = pullState,
                        enabled = query.isBlank() && history.isNotEmpty() && !keyboardOpen,
                        onRefresh = viewModel::clearHistory,
                    ),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    // Results get a frame symmetric with the bottom inset (the first section
                    // header drops its own spacing — see below); other states keep the
                    // tighter top since their items carry their own insets.
                    top = if (query.isNotBlank() && results.isNotEmpty()) 24.dp else 8.dp,
                    bottom = 24.dp,
                ),
            ) {
                if (query.isBlank()) {
                    if (history.isNotEmpty()) {
                        items(
                            count = history.size,
                            key = { "history-${history[it].query}" },
                        ) { index ->
                            val entry = history[index]
                            SearchHistoryRow(
                                query = entry.query,
                                shape = groupItemShape(index, history.size),
                                onClick = {
                                    viewModel.setQuery(entry.query)
                                    viewModel.submit()
                                },
                                // Insert is an invitation to refine: cursor lands at the end
                                // (field-side) and the keyboard comes up ready for typing.
                                onInsert = {
                                    viewModel.setQuery(entry.query)
                                    keyboard?.show()
                                },
                                onRemove = { viewModel.removeFromHistory(entry.query) },
                                modifier = Modifier
                                    // Slides the survivors into their new slots (and group
                                    // shapes) when a swiped row leaves.
                                    .animateItem()
                                    .padding(top = if (index == 0) 8.dp else GroupGap),
                            )
                        }
                    } else {
                        item(key = "history-empty") {
                            Box(Modifier.fillParentMaxSize()) {
                                EmptyState(
                                    icon = Icons.Outlined.History,
                                    title = "Nessuna ricerca recente",
                                    body = "Le tue ricerche recenti appariranno qui",
                                )
                            }
                        }
                    }
                } else {
                    if (results.isEmpty()) {
                        item(key = "no-results") {
                            Box(Modifier.fillParentMaxSize()) {
                                EmptyState(
                                    icon = Icons.Outlined.SearchOff,
                                    title = "Nessun risultato",
                                    body = "Nessun risultato per “$query”",
                                )
                            }
                        }
                    } else {
                        // Sections keep the category order; rows inside are already ranked.
                        // Each section renders as a connected segmented group — the same
                        // expressive list-group idiom as the Registry directory.
                        val grouped = results
                            .groupBy { it.category }
                            .entries
                            .sortedBy { it.key.priority }
                        grouped.forEachIndexed { groupIndex, (category, rows) ->
                            item(key = "header-${category.name}") {
                                SectionLabel(
                                    text = category.label(),
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        // Section spacing only BETWEEN groups; the first sits
                                        // flush on the symmetric content frame above.
                                        top = if (groupIndex == 0) 0.dp else 20.dp,
                                        bottom = 8.dp,
                                    ),
                                )
                            }
                            items(
                                count = rows.size,
                                key = { "result-${category.name}-$it" },
                            ) { index ->
                                val result = rows[index]
                                SearchResultRow(
                                    icon = result.icon(),
                                    title = result.title,
                                    subtitle = result.subtitle,
                                    shape = groupItemShape(index, rows.size),
                                    onClick = { onOpenResult(result) },
                                    modifier = Modifier.padding(top = if (index == 0) 0.dp else GroupGap),
                                )
                            }
                        }
                    }
                }
            }
        }
        HistoryClearIndicator(
            state = pullState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topInset),
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

// Pull-to-clear indicator: a delete badge descends and blooms with the drag, then arms
// (errorContainer -> error) the moment the release would wipe the history.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryClearIndicator(
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val armed = state.distanceFraction >= 1f
    val container by animateColorAsState(
        targetValue = if (armed) scheme.error else scheme.errorContainer,
        label = "history-clear-container",
    )
    val content by animateColorAsState(
        targetValue = if (armed) scheme.onError else scheme.onErrorContainer,
        label = "history-clear-content",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                val f = state.distanceFraction
                alpha = f.coerceIn(0f, 1f)
                // From fully tucked behind the seam down to a resting offset; the overshoot
                // past the threshold keeps following the finger at half speed.
                val travel = size.height + 24.dp.toPx()
                translationY = -size.height + travel * (f.coerceAtMost(1f) + (f - 1f).coerceAtLeast(0f) * 0.5f)
                val scale = 0.7f + 0.3f * f.coerceAtMost(1f)
                scaleX = scale
                scaleY = scale
            }
            .size(44.dp)
            .background(container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(22.dp),
        )
    }
}

// Connected segmented group geometry: expressive outer corners on the group's first/last
// edges, tight inner corners between siblings, hairline gaps instead of dividers.
private val GroupOuterCorner = 20.dp
private val GroupInnerCorner = 5.dp
private val GroupGap = 2.dp

private fun groupItemShape(index: Int, count: Int): Shape {
    val top = if (index == 0) GroupOuterCorner else GroupInnerCorner
    val bottom = if (index == count - 1) GroupOuterCorner else GroupInnerCorner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

private fun SearchResultCategory.label(): String = when (this) {
    SearchResultCategory.Destination -> "Pagine"
    SearchResultCategory.Course -> "Corsi"
    SearchResultCategory.CalendarEvent -> "Calendario"
    SearchResultCategory.Building -> "Edifici"
    SearchResultCategory.TranscriptEntry -> "Carriera"
}

private fun SearchResult.icon(): ImageVector = when (this) {
    is SearchResult.Destination -> Icons.Outlined.Explore
    is SearchResult.Course -> Icons.Outlined.School
    is SearchResult.CalendarEntry ->
        if (isExam) Icons.Outlined.HistoryEdu else Icons.Outlined.CalendarMonth

    is SearchResult.Building -> Icons.Outlined.Apartment
    is SearchResult.TranscriptEntry -> Icons.Outlined.WorkspacePremium
}
