package it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.profile.subscreen.courseDetail.CourseDetailPage
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone
import kotlinx.coroutines.launch

enum class ExamValueMode { Grade, Credits }

private val PassedGreen = Color(0xFF1FA84B)

// Libretto sheet: one modal hosting a pinned morphing header over a two-level body pager.
// The root page is a swipeable Voti/Crediti view (the segmented switch drives the same pager);
// tapping a course pushes its detail in place, the header morphing to the exam name/code.
@Composable
fun ExamsByYearSheet(
    rows: List<TranscriptRow>,
    initialMode: ExamValueMode,
    prerequisiteStatuses: Map<Long, PrerequisiteStatus>,
    onOpenAppelli: (courseKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    // Which course's detail page is open (depth 1), or null at the list root.
    var detailRow by remember { mutableStateOf<TranscriptRow?>(null) }

    // Grouped by year of the study plan (anno di corso), passed exams first within each year.
    val byYear = rows
        .sortedWith(
            compareBy(
                { it.courseYear },
                { it.state != TranscriptRowState.Passed },
                { it.activityName })
        )
        .groupBy { it.courseYear }
        .toSortedMap()

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        val pagerState = rememberPagerState(
            initialPage = if (initialMode == ExamValueMode.Grade) 0 else 1,
        ) { 2 }
        // targetPage (not currentPage) so the header title and switch track the slide as soon
        // as a swipe commits, instead of waiting for the page to settle.
        val mode = if (pagerState.targetPage == 0) ExamValueMode.Grade else ExamValueMode.Credits
        val current = detailRow

        // System back walks the pager up to the list before dismissing the sheet.
        BackHandler(enabled = current != null) { detailRow = null }

        Column {
            SheetPagerHeader(
                depth = if (current == null) 0 else 1,
                title = when {
                    current != null -> current.activityName
                    mode == ExamValueMode.Grade -> "Esami Sostenuti"
                    else -> "CFU Acquisiti"
                },
                subtitle = current?.activityCode?.takeIf { it.isNotBlank() },
                onBack = if (current != null) ({ detailRow = null }) else null,
            )
            AnimatedContent(
                targetState = current,
                transitionSpec = { sheetPageTransform(forward = targetState != null) },
                contentKey = { it?.id ?: -1L },
                label = "exams_sheet_pages",
            ) { row ->
                if (row == null) {
                    ExamsListPage(
                        byYear = byYear,
                        mode = mode,
                        pagerState = pagerState,
                        prerequisiteStatuses = prerequisiteStatuses,
                        onExamClick = { detailRow = it },
                    )
                } else {
                    CourseDetailPage(
                        row = row,
                        onOpenAppelli = { courseKey ->
                            // Close both the detail page and the libretto modal before driving
                            // the cross-screen deep-link, so the back stack lands on the appelli.
                            detailRow = null
                            onDismiss()
                            onOpenAppelli(courseKey)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamsListPage(
    byYear: Map<Int, List<TranscriptRow>>,
    mode: ExamValueMode,
    pagerState: PagerState,
    prerequisiteStatuses: Map<Long, PrerequisiteStatus>,
    onExamClick: (TranscriptRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val pageMode = if (page == 0) ExamValueMode.Grade else ExamValueMode.Credits
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                byYear.forEach { (year, exams) ->
                    item(key = "year-$page-$year") {
                        YearLabel(year = year)
                    }
                    itemsIndexed(exams, key = { _, exam -> "$page-${exam.id}" }) { index, exam ->
                        ExamCard(
                            exam = exam,
                            mode = pageMode,
                            prerequisiteStatus = prerequisiteStatuses[exam.id],
                            isFirst = index == 0,
                            isLast = index == exams.lastIndex,
                            onClick = { onExamClick(exam) },
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(100),
                ),
        ) {
            SegmentedSwitch(
                options = ExamValueMode.entries,
                selected = mode,
                onSelected = { selected ->
                    scope.launch {
                        pagerState.animateScrollToPage(if (selected == ExamValueMode.Grade) 0 else 1)
                    }
                },
                label = { if (it == ExamValueMode.Grade) "Voti" else "Crediti" },
                borderColor = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun YearLabel(year: Int) {
    Text(
        text = if (year <= 0) "Prerequisiti" else "Anno $year",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

// Segmented M3E group: 28dp corners cap each year group's ends, 6dp where cards touch.
@Composable
private fun ExamCard(
    exam: TranscriptRow,
    mode: ExamValueMode,
    prerequisiteStatus: PrerequisiteStatus?,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val passed = exam.state == TranscriptRowState.Passed
    // Orange warning only when the activity is NOT passed AND its propedeuticità check came
    // back NotSatisfied (esito != 1). Pending-but-prereqs-OK keeps the neutral clock icon.
    val prereqMissing = !passed && prerequisiteStatus == PrerequisiteStatus.NotSatisfied
    val warningTone = registryBadgeTone(RegistryBadgeTone.Attention)
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 28.dp else 6.dp,
        topEnd = if (isFirst) 28.dp else 6.dp,
        bottomStart = if (isLast) 28.dp else 6.dp,
        bottomEnd = if (isLast) 28.dp else 6.dp,
    )

    Surface(
        onClick = onClick,
        color = scheme.surfaceContainer,
        shape = shape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (prereqMissing) {
                // Reuse the registry Attention token as a compact badge: the soft-orange
                // container is the warning fill, the glyph rides the matching onContainer.
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(warningTone.container),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = "Propedeuticità non soddisfatte",
                        tint = warningTone.onContainer,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Outlined.Schedule,
                    contentDescription = if (passed) "Superato" else "In sospeso",
                    tint = if (passed) PassedGreen else scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = exam.activityName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueLabel(exam, mode),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (passed) scheme.onSurface else scheme.onSurfaceVariant,
            )
        }
    }
}

private fun valueLabel(exam: TranscriptRow, mode: ExamValueMode): String = when (mode) {
    ExamValueMode.Grade -> when {
        exam.grade == null -> "—"
        exam.cumLaude -> "30L"
        else -> exam.grade.toString()
    }

    ExamValueMode.Credits -> "${formatCredits(exam.credits)} CFU"
}

private fun formatCredits(value: Float): String {
    val asInt = value.toInt()
    return if (value == asInt.toFloat()) asInt.toString() else "%.1f".format(value)
}
