package it.attendance100.mybicocca.ui.screen.elearning

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.domain.model.elearning.quiz.QuizId
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.elearning.component.CardEdition
import it.attendance100.mybicocca.ui.screen.elearning.component.HomeFilterBar
import it.attendance100.mybicocca.ui.screen.elearning.component.NotebookCard
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.ui.screen.elearning.state.ElearningOneShotEvent
import it.attendance100.mybicocca.ui.screen.elearning.state.InitialFetchState
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.AddCourseSheet
import it.attendance100.mybicocca.ui.screen.elearning.theme.LocalCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.ProvideCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.accentFor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElearningScreen(
    modifier: Modifier = Modifier,
    // True only while this is the visible tab — see CalendarScreen for the pager-cache rationale.
    isActive: Boolean = true,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onOpenCourse: (CourseId) -> Unit = {},
    onOpenAssignment: (CourseId, AssignmentId) -> Unit = { _, _ -> },
    onOpenQuiz: (CourseId, QuizId) -> Unit = { _, _ -> },
    onRequireSignIn: () -> Unit = {},
    viewModel: ElearningViewModel = hiltViewModel(),
) {
    val visibleCourses by viewModel.visibleCourses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val studyYears by viewModel.availableStudyYears.collectAsStateWithLifecycle()
    val initialFetch by viewModel.initialFetch.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val coroutineScope = rememberCoroutineScope()
    var addSheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is ElearningOneShotEvent.OpenCourse -> onOpenCourse(event.courseId)
                is ElearningOneShotEvent.OpenAssignment -> onOpenAssignment(event.courseId, event.assignmentId)
                is ElearningOneShotEvent.OpenQuiz -> onOpenQuiz(event.courseId, event.quizId)
                ElearningOneShotEvent.RequireSignIn -> onRequireSignIn()
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    ProvideCourseAccentPalette {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter bar appears only once there is cached data to filter; during the
                // first load or a cold-cache error the whole tab is the loading/error state.
                if (initialFetch is InitialFetchState.Settled) {
                    HomeFilterBar(
                        selected = filter,
                        studyYears = studyYears,
                        onSelect = viewModel::setFilter,
                    )
                }

                PullToRefreshBox(
                    isRefreshing = syncStatus is SyncStatus.Refreshing,
                    onRefresh = viewModel::pullToRefresh,
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val initial = initialFetch) {
                        InitialFetchState.InProgress -> RefreshableEmpty {
                            ElearningLoadingState(modifier = Modifier.fillMaxSize())
                        }
                        is InitialFetchState.Failed -> RefreshableEmpty {
                            ErrorEmptyState(
                                cause = initial.cause,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        InitialFetchState.Settled -> {
                            val data = visibleCourses
                            when {
                                data is Loadable.Loaded && data.value.isNotEmpty() -> CourseList(
                                    groups = data.value,
                                    listState = listState,
                                    onOpenCourse = viewModel::openCourse,
                                    onOpenDeadline = viewModel::openDeadline,
                                    onToggleGroupFavourite = viewModel::toggleGroupFavourite,
                                )
                                data is Loadable.Loaded -> RefreshableEmpty {
                                    EmptyStateForCurrentFilter(
                                        filterActive = filter !is CourseFilter.All,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                else -> RefreshableEmpty {
                                    ElearningLoadingState(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }

            AddCourseFab(
                onClick = { addSheetVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }

    if (addSheetVisible) {
        AddCourseSheet(
            onDismiss = { addSheetVisible = false },
            onEnrolFailed = { cause ->
                coroutineScope.launch {
                    snackbar.showError("Iscrizione non riuscita", cause)
                }
            },
            onEnrolSucceeded = { name ->
                coroutineScope.launch {
                    snackbar.showInfo("Iscritto a $name")
                }
            },
            onRequireSignIn = {
                addSheetVisible = false
                onRequireSignIn()
            },
        )
    }
}

@Composable
private fun AddCourseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
            )
        },
        text = { Text("Aggiungi corso") },
    )
}

@Composable
private fun CourseList(
    groups: List<EnrolledCourseGroup>,
    listState: LazyListState,
    onOpenCourse: (CourseId) -> Unit,
    onOpenDeadline: (Deadline) -> Unit,
    onToggleGroupFavourite: (EnrolledCourseGroup, Boolean) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = groups, key = { it.editions.first().id.value }) { group ->
            val accentPalette = LocalCourseAccentPalette.current
            val groupKey = group.sharedCode ?: group.latest.id.value
            var activeEditionId by rememberSaveable(groupKey) {
                mutableStateOf(group.latest.id.value)
            }
            // If the active id is no longer in the editions list (a year just
            // dropped out of the filter), fall back to the latest edition.
            val resolvedActiveId = group.editions
                .firstOrNull { it.id.value == activeEditionId }
                ?.id
                ?: group.latest.id
            val active = group.editions.first { it.id == resolvedActiveId }
            val cardEditions = group.editions.map { e ->
                val code = e.courseCode()
                CardEdition(
                    id = e.id,
                    yearLabel = code.periodLabel ?: "Trasversale",
                    accent = accentPalette.accentFor(e.id),
                    academicYear = code.academicYear,
                )
            }
            NotebookCard(
                title = active.fullName,
                code = active.idNumber?.takeIf { it.isNotBlank() },
                editions = cardEditions,
                activeEditionId = active.id,
                isFavourite = group.isFavourite,
                deadlines = active.deadlines,
                onClick = { onOpenCourse(active.id) },
                onSelectEdition = { id -> activeEditionId = id.value },
                onToggleFavourite = { fav -> onToggleGroupFavourite(group, fav) },
                onDeadlineClick = onOpenDeadline,
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = tween(durationMillis = 260),
                        fadeOutSpec = tween(durationMillis = 220),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    )
                    .padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun EmptyStateForCurrentFilter(
    filterActive: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        filterActive -> EmptyState(
            icon = Icons.Outlined.FilterAltOff,
            title = "Nessun corso per questo filtro",
            body = "Cambia filtro per vedere altri corsi.",
            modifier = modifier,
        )
        else -> EmptyState(
            icon = Icons.Outlined.School,
            title = "Nessun corso",
            body = "Non risulti iscritto a nessun corso e-learning.",
            modifier = modifier,
        )
    }
}

@Composable
private fun ElearningLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Wraps a non-scrolling empty/error/loading view in a LazyColumn so the
// surrounding PullToRefreshBox still receives nested-scroll gestures and the
// user can pull to retry from these states.
@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) {
                content()
            }
        }
    }
}

// No explicit retry action: the state sits inside the PullToRefreshBox, so the pull
// gesture is the retry.
@Composable
private fun ErrorEmptyState(
    cause: Throwable,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Sincronizzazione non riuscita",
        body = cause.friendlyMessage(),
        modifier = modifier,
    )
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto"
}
