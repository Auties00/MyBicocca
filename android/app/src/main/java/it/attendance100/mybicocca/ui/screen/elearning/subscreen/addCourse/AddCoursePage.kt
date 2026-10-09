package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.navigation.DisposableEffectOnPop
import it.attendance100.mybicocca.ui.component.modal.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AddCourseSearchField
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AreaTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AreaTileVisual
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CatalogCourseRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CategoryRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.SearchTreeCategoryRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.SearchTreeCourseRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogLevel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.SearchRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.buildCatalogLevels
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.LocalAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.ProvideAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.accentFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

/**
 * Add-course sheet page: browses the university's public course catalog and self-enrols into a
 * course. The catalog stack is a [SheetPager]: each level is a page with its title and
 * breadcrumb in the pinned header, and the system back gesture pops one level (scrubbed) when
 * deep and closes the sheet when at the root. There is no explicit close button — the drag
 * handle and the back gesture are the two ways out. The root page hosts its own
 * loading/error/content states in place, so the catalog landing never reads as a page change.
 *
 * Enrol outcomes and missing-session events reset the browse stack, dismiss the sheet, and are
 * reported to the host through [onEnrolSucceeded] and [onEnrolFailed].
 */
@Composable
fun AddCoursePage(
    onEnrolFailed: (Throwable) -> Unit,
    onEnrolSucceeded: (CourseId, String) -> Unit,
    viewModel: AddCourseViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val catalogFailed by viewModel.catalogFailed.collectAsStateWithLifecycle()
    val stack by viewModel.stack.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val enrolment by viewModel.enrolment.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.ensureCatalogLoaded() }

    val dismissControl = LocalSheetDismissControl.current
    val focusManager = LocalFocusManager.current
    val dismissInput: () -> Unit = { focusManager.clearFocus(force = true) }
    val closeSheet: () -> Unit = {
        dismissInput()
        viewModel.resetStack()
        dismissControl?.dismiss()
    }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is AddCourseOneShotEvent.EnrolFailed -> {
                    closeSheet()
                    onEnrolFailed(event.cause)
                }

                is AddCourseOneShotEvent.EnrolSucceeded -> {
                    closeSheet()
                    onEnrolSucceeded(event.courseId, event.courseName)
                }

            }
        }
    }

    DisposableEffectOnPop { viewModel.resetStack() }

    ProvideAreaAccentPalette {
        AddCourseContent(
            catalog = catalog,
            catalogFailed = catalogFailed,
            stack = stack,
            searchQuery = searchQuery,
            searchResults = searchResults,
            enrolment = enrolment,
            onRetryCatalog = viewModel::retryCatalog,
            onSetSearch = viewModel::setSearch,
            onOpen = { entry -> dismissInput(); viewModel.open(entry) },
            onOpenPath = { entries -> dismissInput(); viewModel.openPath(entries) },
            onBack = { dismissInput(); viewModel.back() },
            onEnrol = { course -> dismissInput(); viewModel.enrol(course.id, course.name) },
            onUserScroll = dismissInput,
        )
    }
}

/**
 * Page body: a [SheetPager] over the catalog levels, each page a scoped search field above a
 * stable-height panel, so the push between levels of different lengths never jumps vertically.
 * The pager's header carries the level title and a subtitle on every level (the root explains
 * the page, deeper levels show the breadcrumb home-first), keeping a stable two-line height.
 *
 * The search-overlay/browse swap is its own seekable transition inside the current level's
 * panel, so the back gesture scrubs the results away and reveals the page underneath. Back
 * precedence: while searching, clear the search first; otherwise pop one level when deep. At the
 * root with no search, neither the overlay's handler nor the pager's is enabled, so the host
 * sheet takes the gesture and scrubs itself closed; the two handlers are gated mutually
 * exclusively, so registration order never matters.
 *
 * While the catalog loads, the level chain is a placeholder Root and the pager is SEEDED with it:
 * level identity is by key ("root"), so when the real catalog lands there is no page transition —
 * the root page just crossfades its own loading state into content. The root page still reads its
 * sections from LIVE state, never from the level object.
 */
@Composable
private fun AddCourseContent(
    catalog: Loadable<ElearningCatalog>,
    catalogFailed: Boolean,
    stack: List<CatalogStackEntry>,
    searchQuery: String,
    searchResults: List<SearchRow>,
    enrolment: Map<CourseId, EnrolmentStatus>,
    onRetryCatalog: () -> Unit,
    onSetSearch: (String) -> Unit,
    onOpen: (CatalogStackEntry) -> Unit,
    onOpenPath: (List<CatalogStackEntry>) -> Unit,
    onBack: () -> Unit,
    onEnrol: (CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
) {
    val catalogValue = (catalog as? Loadable.Loaded)?.value

    val placeholderRoot = remember { CatalogLevel.Root(emptyList()) }
    val levels = remember(catalogValue, stack) {
        catalogValue?.let { buildCatalogLevels(it, stack) } ?: listOf(placeholderRoot)
    }
    val currentLevel = levels.last()
    val parentLevel = levels.getOrNull(levels.size - 2)
    val searchActive = searchQuery.isNotBlank()

    val configuration = LocalConfiguration.current
    val pageHeight = configuration.screenHeightDp.dp * 0.60f

    val searchSeek = remember { SeekableTransitionState(false) }
    val searchTransition = rememberTransition(searchSeek, label = "searchVsBrowse")
    LaunchedEffect(searchActive) {
        if (searchSeek.targetState != searchActive) searchSeek.animateTo(searchActive)
    }

    val rootGridState = remember { LazyGridState() }
    val levelListStates = remember { mutableMapOf<String, LazyListState>() }

    PredictiveBackHandler(enabled = searchActive) { progress ->
        try {
            progress.collect { event -> searchSeek.seekTo(event.progress, targetState = false) }
            searchSeek.animateTo(false)
            onSetSearch("")
        } catch (_: CancellationException) {
            searchSeek.animateTo(true)
        }
    }

    val addCourseTitle = stringResource(R.string.elearning_add_course)
    SheetPager(
        page = currentLevel,
        depth = { it.depth },
        backTo = if (searchActive) null else parentLevel,
        onBack = onBack,
        key = { it.key },
        header = { level ->
            when (level) {
                is CatalogLevel.Root -> SheetHeaderSpec(
                    title = stringResource(R.string.catalog_add_course),
                    subtitle = stringResource(R.string.elearning_select_area),
                )

                is CatalogLevel.Inside -> SheetHeaderSpec(
                    title = level.title,
                    subtitle = (listOf(addCourseTitle) + level.ancestors).joinToString(separator = "  ›  "),
                )
            }
        },
    ) { level ->
        Column(modifier = Modifier.fillMaxWidth()) {
            AddCourseSearchField(
                query = searchQuery,
                placeholder = searchPlaceholder(level.depth),
                onQueryChange = onSetSearch,
                modifier = Modifier
                    .testTag(AddCourseTestTags.SEARCH_FIELD)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pageHeight)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                searchTransition.AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
                    contentKey = { it },
                ) { searching ->
                    val haptic = rememberHapticManager()
                    // Only the level on screen hosts the results; a level sliding in or out of a
                    // page change keeps showing its own content.
                    if (searching && level.key == currentLevel.key) {
                        SearchResults(
                            rows = searchResults,
                            enrolment = enrolment,
                            insideAccent = (level as? CatalogLevel.Inside)?.accent,
                            onOpenCategory = { row, accent ->
                                haptic.tap()
                                onOpenPath(
                                    row.path.map { node ->
                                        CatalogStackEntry(
                                            node = node,
                                            areaTileId = row.areaTileId,
                                            accent = accent,
                                        )
                                    },
                                )
                            },
                            onEnrol = { haptic.tap(); onEnrol(it) },
                            onUserScroll = onUserScroll,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        when (level) {
                            is CatalogLevel.Root -> RootPage(
                                sections = catalogValue?.sections,
                                failed = catalogFailed,
                                onRetry = { haptic.tap(); onRetryCatalog() },
                                onOpen = { haptic.tap(); onOpen(it) },
                                onUserScroll = onUserScroll,
                                gridState = rootGridState,
                                modifier = Modifier.fillMaxSize(),
                            )

                            is CatalogLevel.Inside -> InsideLevel(
                                level = level,
                                enrolment = enrolment,
                                onOpen = { haptic.tap(); onOpen(it) },
                                onEnrol = { haptic.tap(); onEnrol(it) },
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

private enum class RootPageState { Loading, Error, Content }

/**
 * Catalog landing page. Loading, error and the area grid swap IN PLACE (no level transition),
 * so a slow or failed catalog load never looks like the modal changed page; the error state
 * carries an explicit retry button.
 */
@Composable
private fun RootPage(
    sections: List<CatalogSection>?,
    failed: Boolean,
    onRetry: () -> Unit,
    onOpen: (CatalogStackEntry) -> Unit,
    onUserScroll: () -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    val state = when {
        sections != null -> RootPageState.Content
        failed -> RootPageState.Error
        else -> RootPageState.Loading
    }
    Crossfade(
        targetState = state,
        animationSpec = tween(220),
        label = "rootPageState",
        modifier = modifier,
    ) { current ->
        when (current) {
            RootPageState.Loading -> Box(
                modifier = Modifier
                    .testTag(AddCourseTestTags.ROOT_STATE_LOADING)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SheetLoadingIndicator(label = stringResource(R.string.elearning_catalog_loading))
            }

            RootPageState.Error -> Box(
                modifier = Modifier
                    .testTag(AddCourseTestTags.ROOT_STATE_ERROR)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.elearning_catalog_load_failed),
                    body = stringResource(R.string.elearning_catalog_load_error),
                    action = {
                        RetryButton(
                            onClick = onRetry,
                            modifier = Modifier.testTag(AddCourseTestTags.RETRY_BUTTON),
                        )
                    },
                )
            }

            RootPageState.Content -> RootGrid(
                sections = sections.orEmpty(),
                onOpen = onOpen,
                onUserScroll = onUserScroll,
                gridState = gridState,
                modifier = Modifier
                    .testTag(AddCourseTestTags.ROOT_STATE_CONTENT)
                    .fillMaxSize(),
            )
        }
    }
}

/**
 * Two-column area-tile grid with sticky section labels. Areas with an official brand colour or
 * artwork get a custom tile visual; the rest fall back to the hashed palette accent. Opening a
 * tile pins its identity and accent into the pushed [CatalogStackEntry].
 *
 * Layout constraints: the grid's top contentPadding must stay 0 — pinned headers pin BELOW the
 * top padding, and tiles would scroll visibly through that strip — so breathing space lives
 * inside the pinned band, which is an opaque band in the sheet's own container colour that
 * fully occludes tiles scrolling under the label.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RootGrid(
    sections: List<CatalogSection>,
    onOpen: (CatalogStackEntry) -> Unit,
    onUserScroll: () -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    val scheme = MaterialTheme.colorScheme
    DismissOnScroll(gridState, onUserScroll)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sections.forEach { section ->
            stickyHeader(key = "section-${section.name}") { _ ->
                SectionLabel(
                    title = section.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.surfaceContainerLow)
                        .padding(start = 6.dp, top = 8.dp, bottom = 8.dp),
                )
            }
            items(items = section.nodes, key = { it.id }) { node ->
                val nameLower = node.name.lowercase()
                val customColor = customAreaColor(nameLower)
                val customImage = customAreaImage(nameLower)
                val visual = when {
                    customColor != null -> AreaTileVisual.CustomColor(customColor)
                    customImage != null -> AreaTileVisual.CustomImage(customImage)
                    else -> AreaTileVisual.Default(palette.accentFor(node.id))
                }
                val resolvedAccent = customColor ?: palette.accentFor(node.id)

                AreaTile(
                    label = node.name,
                    visual = visual,
                    onClick = { onOpen(CatalogStackEntry(node = node, areaTileId = node.id, accent = resolvedAccent)) },
                )
            }
        }
    }
}

/**
 * One catalog level below the root: drill-down category rows above enrollable course rows, in
 * the area's accent, with "Categorie"/"Insegnamenti" labels only when both kinds are present
 * and an empty hint when the category has no content.
 */
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
                EmptyHint(text = stringResource(R.string.elearning_category_empty))
            }
        }

        if (children.isNotEmpty()) {
            if (showLabels) {
                item(key = "label-categorie") {
                    SectionLabel(
                        title = stringResource(R.string.elearning_categories),
                        modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 6.dp)
                    )
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
                    SectionLabel(
                        title = stringResource(R.string.elearning_courses),
                        modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 6.dp)
                    )
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
                    modifier = Modifier.testTag(AddCourseTestTags.courseRow(course.id.value)),
                    enrolButtonModifier = Modifier.testTag(AddCourseTestTags.enrolButton(course.id.value)),
                )
            }
        }
    }
}

/**
 * Scoped results as a pruned tree: matching categories and courses under their waypoint
 * ancestors, rails drawn per row. The accent is the live level's when the search is scoped
 * inside one, the (custom or hashed) area accent at the root. Tapping a category pushes its
 * whole path; an empty query result shows a centered hint.
 */
@Composable
private fun SearchResults(
    rows: List<SearchRow>,
    enrolment: Map<CourseId, EnrolmentStatus>,
    insideAccent: Color?,
    onOpenCategory: (SearchRow.Category, Color) -> Unit,
    onEnrol: (CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    if (rows.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            EmptyHint(text = stringResource(R.string.common_no_results))
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
        itemsIndexed(items = rows, key = { _, row -> row.key }) { _, row ->
            val accent = insideAccent
                ?: customAreaColor(row.areaName.lowercase())
                ?: palette.accentFor(row.areaTileId)
            when (row) {
                is SearchRow.Category -> SearchTreeCategoryRow(
                    row = row,
                    accent = accent,
                    onOpen = { onOpenCategory(row, accent) },
                )

                is SearchRow.Course -> SearchTreeCourseRow(
                    row = row,
                    accent = accent,
                    status = enrolment[row.course.id] ?: EnrolmentStatus.Idle,
                    onEnrol = { onEnrol(row.course) },
                )
            }
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

/** Fires [onScroll] as soon as the user starts dragging, letting the host hide the keyboard. */
@Composable
private fun DismissOnScroll(state: LazyListState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }.filter { it }.collect { onScroll() }
    }
}

/** Grid counterpart of the list-based [DismissOnScroll]. */
@Composable
private fun DismissOnScroll(state: LazyGridState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }.filter { it }.collect { onScroll() }
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

@Composable
private fun searchPlaceholder(depth: Int): String = when (depth) {
    0 -> stringResource(R.string.elearning_search_placeholder_area)
    1 -> stringResource(R.string.elearning_search_placeholder_category)
    2 -> stringResource(R.string.elearning_search_placeholder_subcategory)
    else -> stringResource(R.string.elearning_search_placeholder_course)
}

/** The official-brand area colours; areas without one fall back to the hashed palette accent. */
private fun customAreaColor(nameLower: String): Color? = when {
    nameLower.contains("economico-statistica") || nameLower.contains("economia e statistica") -> Color(0xFFF3C513)
    nameLower.contains("giuridica") || nameLower.contains("giurisprudenza") -> Color(0xFF0B4E91)
    nameLower.contains("medica") || nameLower.contains("medicina") -> Color(0xFFE53728)
    nameLower.contains("psicologica") || nameLower.contains("psicologia") -> Color(0xFF982069)
    nameLower.contains("scienze della formazione") -> Color(0xFFCE2992)
    nameLower.contains("di scienze") || nameLower == "area scienze" || nameLower == "scienze" -> Color(0xFF0D733C)
    nameLower.contains("sociologica") || nameLower.contains("sociologia") -> Color(0xFFF29420)
    nameLower.contains("altre attività formative") || nameLower.contains("altre attivita") -> Color(0xFFB9BBBD)
    else -> null
}

/** Artwork tiles for the non-area catalog roots (academies, doctoral school, ...). */
private fun customAreaImage(nameLower: String): Int? = when {
    nameLower.contains("bicocca academy") || nameLower.contains("bicoccaacademy") -> R.drawable.elearning_bicoccaaccademy
    nameLower.contains("scuola di dottorato") -> R.drawable.elearning_scuoladottorato
    nameLower.contains("formazione insegnanti") -> R.drawable.elearning_formazioneinsegnanti
    nameLower.contains("scuole di specializzazione") || nameLower.contains("scuola di specializzazione") -> R.drawable.elearning_scuolespecializzazione
    nameLower.contains("bbetween") -> R.drawable.elearning_bbetween
    nameLower.contains("formazione tutor") -> R.drawable.elearning_formazionetutor
    nameLower.contains("idoneità linguistica") || nameLower.contains("idoneita' linguistica") -> R.drawable.elearning_idoling
    else -> null
}
