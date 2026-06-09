package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component.AttendanceCourseCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.AttendanceEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.MarkUiState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.courseAttendanceDetail.CourseOverviewPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.RilevaChooserPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.RilevaCodePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.PresenceMarkingProgress
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.PresenceResultContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.QrScannerScreen

// Presenze as a modal sheet, same modal language as the percorso sheet: a pinned morphing
// header over a multi-level body pager. The root lists the pending courses; a course row
// pushes its overview (and from there the in-course marking), while the footer's "Rileva
// presenza" pushes the QR/code flow — the two flows that used to be separate stacked
// modals now page inside this one.
// Presenze as a single sheet entry: BottomSheetSceneStrategy owns the container; this keeps its
// own multi-level state machine (courses -> course detail / rileva flow) and morphing header.
@Composable
fun AttendancePage(
    viewModel: AttendanceViewModel,
) {
    val data by viewModel.courses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val markState by viewModel.markState.collectAsStateWithLifecycle()

    // The VM outlives the sheet (it is shell-scoped, not route-scoped): re-opening shows
    // the cached snapshot instantly while this kicks a background refresh.
    LaunchedEffect(viewModel) { viewModel.refresh() }

    var detailCourse by remember { mutableStateOf<CourseAttendance?>(null) }
    var rilevaOpen by remember { mutableStateOf(false) }
    var enteringCode by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var lastOutcome by remember { mutableStateOf<PresenceMarkOutcome?>(null) }

    LaunchedEffect(markState) {
        (markState as? MarkUiState.Done)?.let { lastOutcome = it.outcome }
    }
    // A mod_attendance QR scanned outside the app lands here mid-flight: jump straight
    // into the rileva flow, whose progress/result pages ride the in-flight markState.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AttendanceEvent.OpenRilevaSheet -> {
                    detailCourse = null
                    rilevaOpen = true
                }
            }
        }
    }

    fun closeRileva() {
        rilevaOpen = false
        enteringCode = false
        viewModel.resetMarkState()
    }

    fun closeCourse() {
        detailCourse = null
    }

    run {
        val loaded = data is Loadable.Loaded
        val courses = data.valueOrNull().orEmpty()
        val course = detailCourse

        val page = when {
            rilevaOpen -> when {
                markState is MarkUiState.Done -> AttendancePage.RilevaResult
                markState == MarkUiState.Submitting -> AttendancePage.RilevaProgress
                enteringCode -> AttendancePage.RilevaCode
                else -> AttendancePage.Rileva
            }

            course != null -> AttendancePage.Course(course)

            else -> AttendancePage.Root
        }

        // System back walks the pager up one level before dismissing the sheet.
        BackHandler(enabled = page != AttendancePage.Root) {
            when (page) {
                is AttendancePage.Course -> closeCourse()
                AttendancePage.RilevaCode -> enteringCode = false
                AttendancePage.Rileva, AttendancePage.RilevaResult -> closeRileva()
                // Mid-submit there is nothing sensible to go back to: swallow.
                AttendancePage.RilevaProgress, AttendancePage.Root -> Unit
            }
        }

        Column {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    AttendancePage.Root -> "Presenze"
                    is AttendancePage.Course -> page.course.name
                    AttendancePage.Rileva -> "Rileva presenza"
                    AttendancePage.RilevaCode -> "Lezione"
                    AttendancePage.RilevaProgress -> "Registrazione"
                    AttendancePage.RilevaResult -> "Esito"
                },
                subtitle = when (page) {
                    AttendancePage.Root -> if (loaded) coursesSummary(courses.size) else null
                    is AttendancePage.Course -> page.course.teacherName
                    AttendancePage.Rileva -> "Registra la tua presenza"
                    AttendancePage.RilevaCode -> "Inserisci il codice della lezione"
                    AttendancePage.RilevaProgress, AttendancePage.RilevaResult -> null
                },
                onBack = when (page) {
                    AttendancePage.Root, AttendancePage.RilevaProgress, AttendancePage.RilevaResult -> null
                    is AttendancePage.Course -> ({ closeCourse() })
                    AttendancePage.RilevaCode -> ({ enteringCode = false })
                    AttendancePage.Rileva -> ({ closeRileva() })
                },
            )
            AnimatedContent(
                targetState = page,
                // Swipes on the pages scroll their content, never the sheet: the header
                // above (and the drag handle) is the only swipe-to-dismiss surface.
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { target ->
                    when (target) {
                        AttendancePage.Root -> "root"
                        is AttendancePage.Course -> "course"
                        AttendancePage.Rileva -> "rileva"
                        AttendancePage.RilevaCode -> "rileva_code"
                        AttendancePage.RilevaProgress -> "rileva_progress"
                        AttendancePage.RilevaResult -> "rileva_result"
                    }
                },
                label = "attendance_pages",
            ) { target ->
                when (target) {
                    AttendancePage.Root -> SheetBody(
                        loaded = loaded,
                        courses = courses,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onOpenCourse = { detailCourse = it },
                        onStartRileva = {
                            viewModel.resetMarkState()
                            rilevaOpen = true
                        },
                    )

                    is AttendancePage.Course -> CourseOverviewPage(course = target.course)

                    AttendancePage.Rileva -> RilevaChooserPage(
                        onLessonCode = { enteringCode = true },
                        onScanActivities = { showScanner = true },
                    )

                    AttendancePage.RilevaCode -> RilevaCodePage(onSubmit = viewModel::submitScan)

                    AttendancePage.RilevaProgress -> PresenceMarkingProgress(
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    AttendancePage.RilevaResult -> lastOutcome?.let { outcome ->
                        PresenceResultContent(
                            outcome = outcome,
                            onDone = { closeRileva() },
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }
            }
        }

        if (showScanner) {
            QrScannerScreen(
                onResult = { raw ->
                    showScanner = false
                    viewModel.submitScan(raw)
                },
                onClose = { showScanner = false },
            )
        }
    }
}

private sealed interface AttendancePage {
    val depth: Int

    data object Root : AttendancePage {
        override val depth = 0
    }

    // The course detail; its open mod_attendance sessions register inline in its tabs.
    data class Course(val course: CourseAttendance) : AttendancePage {
        override val depth = 1
    }

    // The QR/code flow, reachable from the root footer (or a deep-linked scan).
    data object Rileva : AttendancePage {
        override val depth = 1
    }

    data object RilevaCode : AttendancePage {
        override val depth = 2
    }

    data object RilevaProgress : AttendancePage {
        override val depth = 2
    }

    data object RilevaResult : AttendancePage {
        override val depth = 2
    }
}

private fun coursesSummary(count: Int): String? = when (count) {
    0 -> null
    1 -> "1 corso da frequentare"
    else -> "$count corsi da frequentare"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetBody(
    loaded: Boolean,
    courses: List<CourseAttendance>,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenCourse: (CourseAttendance) -> Unit,
    onStartRileva: () -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading
    // Rileva works even with no pending course (a typed lesson code needs no list).
    val showFooter = settled && failure == null

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet only grows/shrinks vertically as content lands — animate the height
    // change here instead of letting the modal snap to the new size.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && courses.isEmpty() -> SheetError(cause = failure.cause, onRetry = onRetry)

            !settled -> SheetLoadingIndicator(label = "Caricamento presenze…")

            courses.isEmpty() -> SheetMessage(
                icon = Icons.Outlined.CoPresent,
                title = "Nessun corso da frequentare",
                body = "Hai superato tutti i corsi previsti dal tuo piano fino a questo semestre.",
            )

            else -> CourseList(
                courses = courses,
                onOpenCourse = onOpenCourse,
                bottomPadding = if (showFooter) 12.dp else 24.dp,
            )
        }

        if (showFooter) {
            RilevaFooterButton(
                onClick = onStartRileva,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}

// The old FAB as the sheet's pinned action, same brand language as the percorso footer.
@Composable
private fun RilevaFooterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text("Rileva presenza", fontWeight = FontWeight.SemiBold)
    }
}

// Flat connected segment group of pending courses — the header carries the count, the
// height cap keeps the footer reachable.
@Composable
private fun CourseList(
    courses: List<CourseAttendance>,
    onOpenCourse: (CourseAttendance) -> Unit,
    bottomPadding: Dp,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(
            items = courses,
            key = { _, course -> course.code ?: course.name },
        ) { index, course ->
            AttendanceCourseCard(
                course = course,
                shape = segmentedShape(isFirst = index == 0, isLast = index == courses.lastIndex),
                onClick = { onOpenCourse(course) },
            )
        }
    }
}

@Composable
private fun SheetError(cause: Throwable, onRetry: () -> Unit) {
    SheetMessage(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { RetryButton(onClick = onRetry) },
    )
}
