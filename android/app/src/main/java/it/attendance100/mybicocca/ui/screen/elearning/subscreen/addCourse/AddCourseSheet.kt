package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseCode
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AddCourseSearchField
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AreaTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AreaTileVisual
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CatalogCourseRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CategoryRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.SearchResultRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogLevel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.buildCatalogLevels
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.LocalAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.ProvideAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.accentFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

// Add-course catalog browser, modelled on the app's multi-state sheets (see AccountSwitcherSheet):
// a PredictiveModalBottomSheet gives the scrub-to-close gesture for free, while an inner seekable
// transition turns the catalog stack into horizontally-scrubbable pages. The system back gesture
// pops one level (scrubbed) when deep, and closes the sheet when at the root. There is no explicit
// close button — the drag handle and the back gesture are the two ways out.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseSheet(
    onDismiss: () -> Unit,
    onEnrolFailed: (Throwable) -> Unit,
    onEnrolSucceeded: (CourseId, String) -> Unit,
    onRequireSignIn: () -> Unit,
    viewModel: AddCourseViewModel = hiltViewModel(),
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val stack by viewModel.stack.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val enrolment by viewModel.enrolment.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.ensureCatalogLoaded() }

    val focusManager = LocalFocusManager.current
    val dismissInput: () -> Unit = { focusManager.clearFocus(force = true) }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is AddCourseOneShotEvent.EnrolFailed -> {
                    dismissInput()
                    viewModel.resetStack()
                    onEnrolFailed(event.cause)
                    onDismiss()
                }

                is AddCourseOneShotEvent.EnrolSucceeded -> {
                    dismissInput()
                    viewModel.resetStack()
                    onEnrolSucceeded(event.courseId, event.courseName)
                    onDismiss()
                }

                AddCourseOneShotEvent.RequireSignIn -> onRequireSignIn()
            }
        }
    }

    PredictiveModalBottomSheet(
        onDismiss = {
            dismissInput()
            viewModel.resetStack()
            onDismiss()
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        ProvideAreaAccentPalette {
            AddCourseContent(
                catalog = catalog,
                stack = stack,
                searchQuery = searchQuery,
                searchResults = searchResults,
                enrolment = enrolment,
                onSetSearch = viewModel::setSearch,
                onOpen = { entry -> dismissInput(); viewModel.open(entry) },
                onBack = { dismissInput(); viewModel.back() },
                onEnrol = { course -> dismissInput(); viewModel.enrol(course.id, course.name) },
                onUserScroll = dismissInput,
            )
        }
    }
}

@Composable
private fun AddCourseContent(
    catalog: Loadable<ElearningCatalog>,
    stack: List<CatalogStackEntry>,
    searchQuery: String,
    searchResults: List<CatalogSearchHit>,
    enrolment: Map<CourseId, EnrolmentStatus>,
    onSetSearch: (String) -> Unit,
    onOpen: (CatalogStackEntry) -> Unit,
    onBack: () -> Unit,
    onEnrol: (CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
) {
    val catalogValue = (catalog as? Loadable.Loaded)?.value
    val levels = remember(catalogValue, stack) {
        catalogValue?.let { buildCatalogLevels(it, stack) }.orEmpty()
    }
    val currentLevel = levels.lastOrNull()
    val depth = currentLevel?.depth ?: 0
    val searchActive = searchQuery.isNotBlank()

    // The pages slide horizontally inside a stable-height panel, so the morph reads as a true
    // page transition (and there is no vertical jump between levels of different lengths).
    val configuration = LocalConfiguration.current
    val pageHeight = configuration.screenHeightDp.dp * 0.60f

    // One seekable transition drives BOTH the title and the page body, so the header text tracks
    // the back-gesture scrub in lockstep with the content.
    val levelSeek = remember { SeekableTransitionState<CatalogLevel?>(null) }
    val levelTransition = rememberTransition(levelSeek, label = "catalogLevel")

    LaunchedEffect(currentLevel) {
        if (levelSeek.targetState != currentLevel) levelSeek.animateTo(currentLevel)
    }

    // Search overlay vs. browse, also seekable so the back gesture scrubs the results away and
    // reveals the page underneath.
    val searchSeek = remember { SeekableTransitionState(false) }
    val searchTransition = rememberTransition(searchSeek, label = "searchVsBrowse")
    LaunchedEffect(searchActive) {
        if (searchSeek.targetState != searchActive) searchSeek.animateTo(searchActive)
    }

    val rootGridState = remember { LazyGridState() }
    val levelListStates = remember { mutableMapOf<String, LazyListState>() }

    // Back precedence: while searching, clear the search first; otherwise pop one level when
    // deep. At the root with no search, neither is enabled, so the host PredictiveModalBottomSheet
    // takes the gesture and scrubs the whole sheet closed. The two handlers are gated mutually
    // exclusively, so registration order never matters.
    PredictiveBackHandler(enabled = searchActive) { progress ->
        try {
            progress.collect { event -> searchSeek.seekTo(event.progress, targetState = false) }
            searchSeek.animateTo(false)
            onSetSearch("")
        } catch (_: CancellationException) {
            searchSeek.animateTo(true)
        }
    }

    val levelsState = rememberUpdatedState(levels)
    PredictiveBackHandler(enabled = depth > 0 && !searchActive) { progress ->
        val chain = levelsState.value
        val parent = chain.getOrNull(chain.size - 2)
        val current = chain.lastOrNull()
        try {
            progress.collect { event -> levelSeek.seekTo(event.progress, targetState = parent) }
            levelSeek.animateTo(parent)
            onBack()
        } catch (_: CancellationException) {
            levelSeek.animateTo(current)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Header(
            levelTransition = levelTransition,
            showBack = depth > 0,
            onBack = onBack,
        )
        AddCourseSearchField(
            query = searchQuery,
            placeholder = searchPlaceholder(depth),
            onQueryChange = onSetSearch,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pageHeight)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            if (catalogValue == null) {
                CatalogLoading(modifier = Modifier.fillMaxSize())
            } else {
                searchTransition.AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
                    contentKey = { it },
                ) { searching ->
                    if (searching) {
                        SearchResults(
                            hits = searchResults,
                            enrolment = enrolment,
                            onEnrol = onEnrol,
                            onUserScroll = onUserScroll,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        levelTransition.AnimatedContent(
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = { pageTransition(forward = isForward()) },
                            contentKey = { it?.key ?: "loading" },
                        ) { level ->
                            when (level) {
                                null -> Box(Modifier.fillMaxSize())
                                is CatalogLevel.Root -> RootGrid(
                                    sections = level.sections,
                                    onOpen = onOpen,
                                    onUserScroll = onUserScroll,
                                    gridState = rootGridState,
                                    modifier = Modifier.fillMaxSize(),
                                )

                                is CatalogLevel.Inside -> InsideLevel(
                                    level = level,
                                    enrolment = enrolment,
                                    onOpen = onOpen,
                                    onEnrol = onEnrol,
                                    onUserScroll = onUserScroll,
                                    listState = levelListStates.getOrPut(level.key) { LazyListState() },
                                    modifier = Modifier.fillMaxSize(),
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
private fun Header(
    levelTransition: androidx.compose.animation.core.Transition<CatalogLevel?>,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = showBack,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Indietro",
                    tint = scheme.onSurface,
                )
            }
        }
        levelTransition.AnimatedContent(
            modifier = Modifier.weight(1f),
            transitionSpec = { titleTransition(forward = isForward()) },
            contentKey = { it?.key ?: "loading" },
        ) { level ->
            Column(modifier = Modifier.padding(start = if (showBack) 4.dp else 12.dp)) {
                Text(
                    text = level?.title ?: "Aggiungi corso",
                    color = scheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val ancestors = (level as? CatalogLevel.Inside)?.ancestors.orEmpty()
                if (ancestors.isNotEmpty()) {
                    Text(
                        text = ancestors.joinToString(separator = "  ›  "),
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RootGrid(
    sections: List<CatalogSection>,
    onOpen: (CatalogStackEntry) -> Unit,
    onUserScroll: () -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    DismissOnScroll(gridState, onUserScroll)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = "section-${section.name}",
            ) {
                SectionLabel(
                    title = section.name,
                    modifier = Modifier.padding(
                        start = 6.dp,
                        top = if (sectionIndex == 0) 4.dp else 16.dp,
                        bottom = 6.dp,
                    ),
                )
            }
            items(items = section.nodes, key = { it.id }) { node ->
                val nameLower = node.name.lowercase()
                val visual = when {
                    nameLower.contains("economico-statistica") || nameLower.contains("economia e statistica") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFFF3C513))
                    nameLower.contains("giuridica") || nameLower.contains("giurisprudenza") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFF0B4E91))
                    nameLower.contains("medica") || nameLower.contains("medicina") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFFE53728))
                    nameLower.contains("psicologica") || nameLower.contains("psicologia") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFF982069))
                    nameLower.contains("scienze della formazione") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFFCE2992))
                    nameLower.contains("di scienze") || nameLower == "area scienze" || nameLower == "scienze" -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFF0D733C))
                    nameLower.contains("sociologica") || nameLower.contains("sociologia") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFFF29420))
                    nameLower.contains("bicocca academy") || nameLower.contains("bicoccaacademy") -> AreaTileVisual.CustomImage(
                        R.drawable.elearning_bicoccaaccademy
                    )

                    nameLower.contains("scuola di dottorato") -> AreaTileVisual.CustomImage(R.drawable.elearning_scuoladottorato)
                    nameLower.contains("formazione insegnanti") -> AreaTileVisual.CustomImage(R.drawable.elearning_formazioneinsegnanti)
                    nameLower.contains("scuole di specializzazione") || nameLower.contains("scuola di specializzazione") -> AreaTileVisual.CustomImage(
                        R.drawable.elearning_scuolespecializzazione
                    )

                    nameLower.contains("bbetween") -> AreaTileVisual.CustomImage(R.drawable.elearning_bbetween)
                    nameLower.contains("formazione tutor") -> AreaTileVisual.CustomImage(R.drawable.elearning_formazionetutor)
                    nameLower.contains("idoneità linguistica") || nameLower.contains("idoneita' linguistica") -> AreaTileVisual.CustomImage(
                        R.drawable.elearning_idoling
                    )
                    nameLower.contains("altre attività formative") || nameLower.contains("altre attivita") -> AreaTileVisual.CustomColor(androidx.compose.ui.graphics.Color(0xFFB9BBBD))
                    else -> AreaTileVisual.Default(palette.accentFor(node.id))
                }
                
                val resolvedAccent = when (visual) {
                    is AreaTileVisual.CustomColor -> visual.color
                    else -> palette.accentFor(node.id)
                }
                
                AreaTile(
                    label = node.name,
                    visual = visual,
                    onClick = { onOpen(CatalogStackEntry(node = node, areaTileId = node.id, accent = resolvedAccent)) },
                )
            }
        }
    }
}

@Composable
private fun InsideLevel(
    level: CatalogLevel.Inside,
    enrolment: Map<CourseId, EnrolmentStatus>,
    onOpen: (CatalogStackEntry) -> Unit,
    onEnrol: (CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val accent = level.accent
    val children = level.children
    val courses = level.courses
    val showLabels = children.isNotEmpty() && courses.isNotEmpty()

    DismissOnScroll(listState, onUserScroll)
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (children.isEmpty() && courses.isEmpty()) {
            item(key = "empty") {
                EmptyHint(text = "Nessun contenuto in questa categoria.")
            }
        }

        if (children.isNotEmpty()) {
            if (showLabels) {
                item(key = "label-categorie") {
                    SectionLabel(title = "Categorie", modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 6.dp))
                }
            }
            itemsIndexed(items = children, key = { _, it -> "c-${it.id}" }) { index, child ->
                CategoryRow(
                    label = child.name,
                    accent = accent,
                    isFirst = index == 0,
                    isLast = index == children.lastIndex,
                    onClick = { onOpen(CatalogStackEntry(node = child, areaTileId = level.areaTileId, accent = accent)) },
                )
            }
        }

        if (courses.isNotEmpty()) {
            if (showLabels) {
                item(key = "label-insegnamenti") {
                    SectionLabel(title = "Insegnamenti", modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 6.dp))
                }
            }
            itemsIndexed(items = courses, key = { _, it -> "x-${it.id.value}" }) { index, course ->
                CatalogCourseRow(
                    name = course.name,
                    code = course.code,
                    accent = accent,
                    status = enrolment[course.id] ?: EnrolmentStatus.Idle,
                    isFirst = index == 0,
                    isLast = index == courses.lastIndex,
                    onEnrol = { onEnrol(course) },
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    hits: List<CatalogSearchHit>,
    enrolment: Map<CourseId, EnrolmentStatus>,
    onEnrol: (CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    if (hits.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            EmptyHint(text = "Nessun corso trovato.")
        }
        return
    }
    val listState = rememberLazyListState()
    DismissOnScroll(listState, onUserScroll)
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        itemsIndexed(
            // The same course is intentionally emitted once per breadcrumb (cross-listings), so the
            // course id + first crumb is not unique; the list index is, and the order is stable
            // within a query result.
            items = hits,
            key = { index, hit -> "s-$index-${hit.course.id.value}" },
        ) { index, hit ->
            val areaKey = hit.path.firstOrNull().orEmpty()
            val parsed = remember(hit.course.code) { CourseCode.parse(hit.course.code) }
            // Keep the lecture stream (turno) visible so two streams of the same course in the same
            // year don't render identically.
            val codeLabel = parsed.code?.let { base ->
                parsed.stream?.let { "$base · T$it" } ?: base
            } ?: hit.course.code
            SearchResultRow(
                name = hit.course.name,
                code = codeLabel,
                year = parsed.academicYear?.toString(),
                breadcrumb = hit.path.joinToString(separator = "  ›  "),
                accent = palette.accentFor(areaKey),
                status = enrolment[hit.course.id] ?: EnrolmentStatus.Idle,
                isFirst = index == 0,
                isLast = index == hits.lastIndex,
                onEnrol = { onEnrol(hit.course) },
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = modifier,
    )
}

@Composable
private fun DismissOnScroll(state: LazyListState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }.filter { it }.collect { onScroll() }
    }
}

@Composable
private fun DismissOnScroll(state: LazyGridState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }.filter { it }.collect { onScroll() }
    }
}

@Composable
private fun CatalogLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = modifier.padding(32.dp),
    )
}

private fun searchPlaceholder(depth: Int): String = when (depth) {
    0 -> "Cerca area, insegnamento, codice…"
    1 -> "Cerca tipo, insegnamento, codice…"
    2 -> "Cerca programma, insegnamento, codice…"
    else -> "Cerca insegnamento, codice…"
}

// Forward = navigating deeper (open); reverse = popping back. Null (initial load) is treated as
// "below the root" so the first page arrives from the right like an open.
private fun androidx.compose.animation.AnimatedContentTransitionScope<CatalogLevel?>.isForward(): Boolean =
    (targetState?.depth ?: -1) >= (initialState?.depth ?: -1)

private fun pageTransition(forward: Boolean): ContentTransform {
    val dur = 320
    val enter = slideInHorizontally(tween(dur)) { w -> if (forward) w else -w } + fadeIn(tween(dur))
    val exit = slideOutHorizontally(tween(dur)) { w -> if (forward) -w else w } + fadeOut(tween(220))
    return enter togetherWith exit
}

private fun titleTransition(forward: Boolean): ContentTransform {
    val dur = 300
    val enter = slideInHorizontally(tween(dur)) { w -> (if (forward) w else -w) / 5 } + fadeIn(tween(dur))
    val exit = slideOutHorizontally(tween(dur)) { w -> (if (forward) -w else w) / 5 } + fadeOut(tween(180))
    return enter togetherWith exit
}
