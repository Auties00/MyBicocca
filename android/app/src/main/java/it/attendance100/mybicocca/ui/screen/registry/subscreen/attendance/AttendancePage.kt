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

/**
 * Body of the Presenze modal: per-course attendance as a single sheet entry whose container
 * is owned by the navigation-level BottomSheetSceneStrategy, in the same modal language as
 * the percorso sheet.
 *
 * A pinned morphing [SheetPagerHeader] sits over a multi-level body pager driven by a local
 * state machine: the root lists the courses still to attend, a course row pushes its
 * overview, and the footer's "Rileva presenza" pushes the QR/code marking flow, whose
 * progress and result pages ride the in-flight [MarkUiState]. Swipes on the pages scroll
 * their content, never the sheet — the header (and drag handle) is the only swipe-to-dismiss
 * surface. System back walks the pager up one level before dismissing the sheet and is
 * swallowed mid-submission, when there is nothing sensible to go back to.
 *
 * The ViewModel is shell-scoped and outlives the sheet, so re-opening shows the cached
 * snapshot instantly while entering kicks a background refresh. A mod_attendance QR scanned
 * outside the app lands mid-flight as a one-shot [AttendanceEvent.OpenRilevaSheet]: the pager
 * jumps straight into the rileva flow riding the already-running mark.
 */
@Composable
fun AttendancePage(
    viewModel: AttendanceViewModel,
) {
    val data by viewModel.courses.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val markState by viewModel.markState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    var detailCourse by remember { mutableStateOf<CourseAttendance?>(null) }
    var rilevaOpen by remember { mutableStateOf(false) }
    var enteringCode by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var lastOutcome by remember { mutableStateOf<PresenceMarkOutcome?>(null) }

    LaunchedEffect(markState) {
        (markState as? MarkUiState.Done)?.let { lastOutcome = it.outcome }
    }
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

        BackHandler(enabled = page != AttendancePage.Root) {
            when (page) {
                is AttendancePage.Course -> closeCourse()
                AttendancePage.RilevaCode -> enteringCode = false
                AttendancePage.Rileva, AttendancePage.RilevaResult -> closeRileva()
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

/** One level of the sheet's body pager; depth drives the header morph and the forward/back page transition. */
private sealed interface AttendancePage {
    val depth: Int

    data object Root : AttendancePage {
        override val depth = 0
    }

    /** The course detail, paging one attendance tab per source. */
    data class Course(val course: CourseAttendance) : AttendancePage {
        override val depth = 1
    }

    /** The QR/code marking flow, reachable from the root footer (or a deep-linked scan). */
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

/**
 * Root page body: error / loading / empty / course-list states over the optional "Rileva
 * presenza" footer. The loading state is held for a minimum beat so quick fetches do not
 * flash it, and the footer shows once content settles without failure — rileva works even
 * with no pending course, since a typed lesson code needs no list. Height changes are
 * animated so the modal grows and shrinks smoothly as content lands.
 */
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
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading
    val showFooter = settled && failure == null

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

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

/** The sheet's pinned brand-filled action opening the rileva flow, same brand language as the percorso footer. */
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

/**
 * Flat connected segment group of pending courses — the header carries the count, and the
 * height cap keeps the footer reachable.
 */
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
