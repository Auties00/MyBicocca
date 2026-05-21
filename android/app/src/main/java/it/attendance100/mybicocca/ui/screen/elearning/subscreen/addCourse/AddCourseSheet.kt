package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AddCourseSearchField
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.AreaTile
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CatalogCourseRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.CategoryRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component.SearchResultRow
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseLevel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.AddCourseOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CatalogStackEntry
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.LocalAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.ProvideAreaAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.theme.accentFor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseSheet(
    onDismiss: () -> Unit,
    onEnrolFailed: (Throwable) -> Unit,
    onEnrolSucceeded: (String) -> Unit,
    onRequireSignIn: () -> Unit,
    viewModel: AddCourseViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val stack by viewModel.stack.collectAsStateWithLifecycle()
    val level by viewModel.level.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val enrolment by viewModel.enrolment.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.ensureCatalogLoaded() }

    val scheme = MaterialTheme.colorScheme

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
                    onEnrolSucceeded(event.courseName)
                    onDismiss()
                }
                AddCourseOneShotEvent.RequireSignIn -> onRequireSignIn()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            dismissInput()
            viewModel.resetStack()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = scheme.surface,
    ) {
        ProvideAreaAccentPalette {
            AddCourseSheetContent(
                catalog = catalog,
                level = level,
                stack = stack,
                searchQuery = searchQuery,
                searchResults = searchResults,
                enrolment = enrolment,
                onSetSearch = viewModel::setSearch,
                onOpen = { entry -> dismissInput(); viewModel.open(entry) },
                onBack = { dismissInput(); viewModel.back() },
                onClose = {
                    dismissInput()
                    viewModel.resetStack()
                    onDismiss()
                },
                onEnrol = { course -> dismissInput(); viewModel.enrol(course.id, course.name) },
                onUserScroll = dismissInput,
            )
        }
    }
}

@Composable
private fun AddCourseSheetContent(
    catalog: Loadable<*>,
    level: AddCourseLevel?,
    stack: List<CatalogStackEntry>,
    searchQuery: String,
    searchResults: List<it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit>,
    enrolment: Map<it.attendance100.mybicocca.domain.model.elearning.course.CourseId, EnrolmentStatus>,
    onSetSearch: (String) -> Unit,
    onOpen: (CatalogStackEntry) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onEnrol: (it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
) {
    val depth = stack.size
    val title = remember(stack) {
        if (stack.isEmpty()) "Aggiungi corso"
        else stack.last().node.name
    }
    val breadcrumb = remember(stack) {
        if (stack.size <= 1) ""
        else stack.dropLast(1).joinToString(separator = "  ›  ") { it.node.name }
    }
    val searchPlaceholder = when (depth) {
        0 -> "Cerca area, insegnamento, codice…"
        1 -> "Cerca tipo, insegnamento, codice…"
        2 -> "Cerca programma, insegnamento, codice…"
        else -> "Cerca insegnamento, codice…"
    }
    val searchActive = searchQuery.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 320.dp),
    ) {
        Header(
            depth = depth,
            title = title,
            onBack = onBack,
            onClose = onClose,
        )
        AnimatedVisibility(visible = breadcrumb.isNotBlank()) {
            Text(
                text = breadcrumb,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                letterSpacing = 0.1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 8.dp),
            )
        }
        AddCourseSearchField(
            query = searchQuery,
            placeholder = searchPlaceholder,
            onQueryChange = onSetSearch,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        )
        Spacer(Modifier.size(8.dp))

        when {
            catalog is Loadable.NotYetLoaded -> CatalogLoading(modifier = Modifier.fillMaxSize())
            searchActive -> SearchResults(
                hits = searchResults,
                enrolment = enrolment,
                onEnrol = onEnrol,
                onUserScroll = onUserScroll,
                modifier = Modifier.fillMaxSize(),
            )
            level != null -> {
                val previousDepth = remember { mutableStateOf(stack.size) }
                val forward = stack.size >= previousDepth.value
                previousDepth.value = stack.size
                AnimatedContent(
                    targetState = level,
                    transitionSpec = {
                        val offset = if (forward) 60 else -60
                        val outOffset = if (forward) -60 else 60
                        (slideInHorizontally(animationSpec = tween(280)) { offset } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { outOffset } + fadeOut(tween(160)))
                    },
                    contentKey = { state ->
                        when (state) {
                            is AddCourseLevel.Root -> "root"
                            is AddCourseLevel.Inside -> state.node.id
                        }
                    },
                    label = "addCourseLevel",
                    modifier = Modifier.fillMaxSize(),
                ) { current ->
                    when (current) {
                        is AddCourseLevel.Root -> RootGrid(
                            sections = current.sections,
                            onOpen = onOpen,
                            onUserScroll = onUserScroll,
                            modifier = Modifier.fillMaxSize(),
                        )
                        is AddCourseLevel.Inside -> InsideLevel(
                            children = current.children,
                            courses = current.courses,
                            areaTileId = stack.firstOrNull()?.areaTileId ?: current.node.id,
                            enrolment = enrolment,
                            onOpen = onOpen,
                            onEnrol = onEnrol,
                            onUserScroll = onUserScroll,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    depth: Int,
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = depth > 0) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Indietro",
                    tint = scheme.onSurface,
                )
            }
        }
        Text(
            text = title,
            color = scheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (depth > 0) 0.dp else 12.dp),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(scheme.surfaceContainerHigh)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Chiudi",
                tint = scheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun RootGrid(
    sections: List<it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection>,
    onOpen: (CatalogStackEntry) -> Unit,
    onUserScroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    val gridState = rememberLazyGridState()
    DismissOnScroll(gridState, onUserScroll)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sections.forEachIndexed { sIndex, section ->
            item(
                span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) },
                key = "section-${section.name}",
            ) {
                SectionHeader(
                    title = section.name,
                    modifier = Modifier.padding(start = 6.dp, top = if (sIndex == 0) 4.dp else 14.dp, bottom = 4.dp),
                )
            }
            items(items = section.nodes, key = { it.id }) { node ->
                AreaTile(
                    label = node.name,
                    accent = palette.accentFor(node.id),
                    onClick = {
                        onOpen(CatalogStackEntry(node = node, areaTileId = node.id))
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
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
private fun InsideLevel(
    children: List<CatalogNode>,
    courses: List<it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.CourseItem>,
    areaTileId: String,
    enrolment: Map<it.attendance100.mybicocca.domain.model.elearning.course.CourseId, EnrolmentStatus>,
    onOpen: (CatalogStackEntry) -> Unit,
    onEnrol: (it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse) -> Unit,
    onUserScroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAreaAccentPalette.current
    val accent = palette.accentFor(areaTileId)
    val listState = rememberLazyListState()
    DismissOnScroll(listState, onUserScroll)
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (children.isEmpty() && courses.isEmpty()) {
            item(key = "empty") {
                EmptyHint(text = "Nessun contenuto in questa categoria.")
            }
        }
        items(items = children, key = { "c-${it.id}" }) { child ->
            CategoryRow(
                label = child.name,
                accent = accent,
                onClick = {
                    onOpen(CatalogStackEntry(node = child, areaTileId = areaTileId))
                },
            )
        }
        items(items = courses, key = { "x-${it.course.id.value}" }) { item ->
            CatalogCourseRow(
                name = item.course.name,
                code = item.course.code,
                accent = accent,
                status = enrolment[item.course.id] ?: EnrolmentStatus.Idle,
                onEnrol = { onEnrol(item.course) },
            )
        }
    }
}

@Composable
private fun SearchResults(
    hits: List<it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit>,
    enrolment: Map<it.attendance100.mybicocca.domain.model.elearning.course.CourseId, EnrolmentStatus>,
    onEnrol: (it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse) -> Unit,
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = hits, key = { "s-${it.course.id.value}-${it.path.firstOrNull().orEmpty()}" }) { hit ->
            val areaKey = hit.path.firstOrNull().orEmpty()
            SearchResultRow(
                name = hit.course.name,
                code = hit.course.code,
                breadcrumb = hit.path.joinToString(separator = "  ›  "),
                accent = palette.accentFor(areaKey),
                status = enrolment[hit.course.id] ?: EnrolmentStatus.Idle,
                onEnrol = { onEnrol(hit.course) },
            )
        }
    }
}

@Composable
private fun DismissOnScroll(state: LazyListState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }
            .filter { it }
            .collect { onScroll() }
    }
}

@Composable
private fun DismissOnScroll(state: LazyGridState, onScroll: () -> Unit) {
    LaunchedEffect(state, onScroll) {
        snapshotFlow { state.isScrollInProgress }
            .filter { it }
            .collect { onScroll() }
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