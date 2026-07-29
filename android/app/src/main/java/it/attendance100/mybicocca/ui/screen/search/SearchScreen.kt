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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Today
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.search.RelativeDay
import it.attendance100.mybicocca.domain.model.search.SearchResult
import it.attendance100.mybicocca.domain.model.search.SearchResultCategory
import it.attendance100.mybicocca.ui.component.bar.fadeThroughExpanded
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.search.component.DictationDialog
import it.attendance100.mybicocca.ui.screen.search.component.SearchHistoryRow
import it.attendance100.mybicocca.ui.screen.search.component.SearchResultRow
import it.attendance100.mybicocca.ui.screen.search.component.TopHitCard

/**
 * Full-screen global-search body following the M3 search-view anatomy: the app bar above
 * acts as the header (surfaceContainer), a pinned full-width outlineVariant divider marks
 * the seam (outline stays reserved for important boundaries like text fields), and this
 * content area sits one tonal step below on plain surface — separation comes from the tonal
 * ladder, not shadows. The opaque background plus a no-op clickable swallow taps meant for
 * the hidden tab underneath.
 *
 * Not a nav route: it rides the bar's own search [progress], so the predictive-back gesture
 * that scrubs the bar collapse scrubs this overlay in lockstep. The body fade-throughs over
 * the upper half of the morph (the same ramp as the bar's expanded content) while growing
 * down from under the bar, so it reads as unfolding out of the pill rather than popping in.
 * [subPageProgress] is how far a sub-page covers the shell: search stays alive under a
 * pushed sub-page — the overlay fades out for the push (and back in as the pop scrubs) but
 * stays composed, so query, results and scroll position survive the round trip. At alpha 0
 * the layer is also shifted off-screen, because transparency alone would leave it
 * hit-testable above the settled sub-page; the shift drops it from hit testing while
 * keeping it composed.
 *
 * A blank query shows the recent searches as one segmented group (or an empty state): a row
 * tap re-runs the search, the trailing arrow inserts the text into the field uncommitted
 * with the keyboard up — an invitation to refine — and a horizontal swipe removes a single
 * row, survivors sliding into their new slots and group shapes. A vertical pull wipes the
 * whole history through the pull-to-refresh machinery with a delete badge instead of a
 * spinner; the wipe never arms while the keyboard is up, where a downward pull belongs to
 * the IME's own dismiss gesture. A non-blank query shows ranked results grouped into
 * category sections rendered as connected segmented groups (the registry directory's
 * expressive list-group idiom); sections keep the category order and rows inside arrive
 * already ranked. A clearly-winning first result — at least acronym-tier score with an
 * unambiguous lead over the runner-up — gets the hero top-hit card above the sections, so
 * it means "press and go" rather than another section; no matches render a dedicated empty
 * state. The results list takes a top frame symmetric with the bottom inset (the first
 * section header drops its own top spacing, and section spacing only sits between groups);
 * the other states keep a tighter top since their items carry their own insets.
 *
 * The keyboard retracts as soon as the user starts browsing results (per M3), and when a
 * dictation session ends it comes straight back up: the dictated text lands cursor-at-end
 * in the field, ready to be refined by typing. The dictation dialog rides the ViewModel's
 * dictating flag rather than a route, because the session can also end on its own (final
 * transcript, recognizer timeout) and the dialog must follow it out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    viewModel: SearchViewModel,
    progress: Float,
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

    DictationDialog(
        visible = dictating,
        transcript = query,
        soundLevel = soundLevel,
        onFinish = viewModel::stopDictation,
    )

    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling -> if (scrolling) keyboard?.hide() }
    }
    var wasDictating by remember { mutableStateOf(false) }
    LaunchedEffect(dictating) {
        if (wasDictating && !dictating) keyboard?.show()
        wasDictating = dictating
    }

    val scheme = MaterialTheme.colorScheme
    val pullState = rememberPullToRefreshState()
    Box(
        modifier = modifier
            .testTag(SearchOverlayTestTags.ROOT)
            .fillMaxSize()
            .graphicsLayer {
                alpha = fadeThroughExpanded(progress) * (1f - subPageProgress)
                if (alpha == 0f) translationY = size.height
                transformOrigin = TransformOrigin(0.5f, 0f)
                val scale = 0.96f + 0.04f * progress
                scaleX = scale
                scaleY = scale
            }
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
            HorizontalDivider(color = scheme.outlineVariant)

            val keyboardOpen = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
            val listTag = if (query.isBlank()) {
                SearchOverlayTestTags.HISTORY_LIST
            } else {
                SearchOverlayTestTags.RESULTS_LIST
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .testTag(listTag)
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
                                onInsert = {
                                    viewModel.setQuery(entry.query)
                                    keyboard?.show()
                                },
                                onRemove = { viewModel.removeFromHistory(entry.query) },
                                modifier = Modifier
                                    .testTag(SearchOverlayTestTags.historyItem(entry.query))
                                    .animateItem()
                                    .padding(top = if (index == 0) 8.dp else GroupGap),
                            )
                        }
                    } else {
                        item(key = "history-empty") {
                            Box(Modifier
                                .testTag(SearchOverlayTestTags.HISTORY_EMPTY)
                                .fillParentMaxSize()) {
                                EmptyState(
                                    icon = Icons.Outlined.History,
                                    title = stringResource(R.string.search_no_recent_title),
                                    body = stringResource(R.string.search_no_recent_body),
                                )
                            }
                        }
                    }
                } else {
                    if (results.isEmpty()) {
                        item(key = "no-results") {
                            Box(Modifier
                                .testTag(SearchOverlayTestTags.NO_RESULTS)
                                .fillParentMaxSize()) {
                                EmptyState(
                                    icon = Icons.Outlined.SearchOff,
                                    title = stringResource(R.string.common_no_results),
                                    body = stringResource(R.string.search_no_results_for, query),
                                )
                            }
                        }
                    } else {
                        val topHit = results.firstOrNull()?.takeIf { best ->
                            best.score >= TopHitMinScore && (
                                results.getOrNull(1)
                                    ?.let { best.score - it.score >= TopHitMargin } != false
                                )
                        }
                        if (topHit != null) {
                            item(key = "top-hit") {
                                TopHitCard(
                                    icon = topHit.icon(),
                                    title = topHit.displayTitle(),
                                    subtitle = topHit.displaySubtitle(),
                                    query = query,
                                    onClick = { onOpenResult(topHit) },
                                    modifier = Modifier.testTag(SearchOverlayTestTags.TOP_HIT),
                                )
                            }
                        }

                        val grouped = results
                            .filterNot { it === topHit }
                            .groupBy { it.category }
                            .entries
                            .sortedBy { it.key.priority }
                        grouped.forEachIndexed { groupIndex, (category, rows) ->
                            item(key = "header-${category.name}") {
                                SectionLabel(
                                    text = category.label(),
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        top = if (groupIndex == 0 && topHit == null) 0.dp else 20.dp,
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
                                    title = result.displayTitle(),
                                    subtitle = result.displaySubtitle(),
                                    query = query,
                                    shape = groupItemShape(index, rows.size),
                                    onClick = { onOpenResult(result) },
                                    accent = result is SearchResult.Action,
                                    modifier = Modifier
                                        .testTag(SearchOverlayTestTags.resultItem(result.key))
                                        .padding(top = if (index == 0) 0.dp else GroupGap),
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

/**
 * Pull-to-clear indicator: a delete badge descends from behind the seam and blooms with the
 * drag — fading and scaling in, with overshoot past the threshold following the finger at
 * half speed — then arms (errorContainer -> error) the moment a release would wipe the
 * history.
 */
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
                val travel = size.height + 24.dp.toPx()
                translationY =
                    -size.height + travel * (f.coerceAtMost(1f) + (f - 1f).coerceAtLeast(0f) * 0.5f)
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

private val GroupOuterCorner = 20.dp
private val GroupInnerCorner = 5.dp
private val GroupGap = 2.dp

/**
 * Connected segmented-group geometry: expressive outer corners on the group's first/last
 * edges, tight inner corners between siblings, hairline gaps instead of dividers.
 */
private fun groupItemShape(index: Int, count: Int): Shape {
    val top = if (index == 0) GroupOuterCorner else GroupInnerCorner
    val bottom = if (index == count - 1) GroupOuterCorner else GroupInnerCorner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
private fun SearchResultCategory.label(): String = stringResource(
    when (this) {
        SearchResultCategory.Action -> R.string.search_category_actions
        SearchResultCategory.Destination -> R.string.search_category_pages
        SearchResultCategory.Course -> R.string.search_category_courses
        SearchResultCategory.Assignment -> R.string.search_category_assignments
        SearchResultCategory.Quiz -> R.string.search_category_quizzes
        SearchResultCategory.CalendarEvent -> R.string.search_category_calendar
        SearchResultCategory.Place -> R.string.search_category_places
        SearchResultCategory.TranscriptEntry -> R.string.search_category_career
    },
)

private fun SearchResult.icon(): ImageVector = when (this) {
    is SearchResult.Action -> Icons.Outlined.Bolt
    is SearchResult.Destination -> Icons.Outlined.Explore
    is SearchResult.Course -> Icons.Outlined.School
    is SearchResult.Assignment -> Icons.AutoMirrored.Outlined.Assignment
    is SearchResult.Quiz -> Icons.Outlined.Quiz
    is SearchResult.CalendarEntry ->
        if (isExam) Icons.Outlined.HistoryEdu else Icons.Outlined.CalendarMonth

    is SearchResult.CalendarDay -> Icons.Outlined.Today
    is SearchResult.Building -> Icons.Outlined.Apartment
    is SearchResult.Room -> Icons.Outlined.MeetingRoom
    is SearchResult.TranscriptEntry -> Icons.Outlined.WorkspacePremium
}

/**
 * Resolves the row title, localizing the relative-day hits ("Oggi"/"Domani"/…) that the use
 * case leaves as a [RelativeDay] so localization stays in the UI. Every other hit already
 * carries its title.
 */
@Composable
private fun SearchResult.displayTitle(): String = when (this) {
    is SearchResult.Action -> if (titleRes != 0) stringResource(titleRes) else title
    is SearchResult.Destination -> if (titleRes != 0) stringResource(titleRes) else title
    is SearchResult.CalendarDay -> when (relativeDay) {
        RelativeDay.Today -> stringResource(R.string.relative_day_today)
        RelativeDay.Tomorrow -> stringResource(R.string.relative_day_tomorrow)
        RelativeDay.AfterTomorrow -> stringResource(R.string.relative_day_after_tomorrow)
        null -> title
    }

    else -> title
}

/**
 * Resolves the row subtitle for the hits whose secondary text is localized in the UI: the
 * fixed "open in calendar" caption for a date hit, and the "activity code · grade" line for a
 * transcript hit. Every other hit already carries its subtitle.
 */
@Composable
private fun SearchResult.displaySubtitle(): String? = when (this) {
    is SearchResult.Action -> if (subtitleRes != null) stringResource(subtitleRes) else subtitle
    is SearchResult.Destination -> if (subtitleRes != null) stringResource(subtitleRes) else subtitle
    is SearchResult.CalendarDay -> stringResource(R.string.search_open_in_calendar)
    is SearchResult.TranscriptEntry -> {
        val gradePrefix = stringResource(R.string.search_grade)
        val gradeLabel = grade?.let { "$gradePrefix $it${if (cumLaude) "L" else ""}" }
        listOfNotNull(activityCode, gradeLabel).joinToString(" · ").ifBlank { null }
    }

    else -> subtitle
}

/** Top-hit gate, score floor: the best result must reach at least acronym-tier strength. */
private const val TopHitMinScore = 0.8

/** Top-hit gate, required lead of the best result over the runner-up. */
private const val TopHitMargin = 0.05
