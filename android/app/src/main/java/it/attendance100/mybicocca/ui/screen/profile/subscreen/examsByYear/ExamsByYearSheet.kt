package it.attendance100.mybicocca.ui.screen.profile.subscreen.examsByYear

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.transcript.PrerequisiteStatus
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.courseDetail.CourseDetailSheet
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone
import it.attendance100.mybicocca.util.rememberHapticManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class ExamValueMode { Grade, Credits }

private val PassedGreen = Color(0xFF1FA84B)

@Composable
fun ExamsByYearSheet(
    rows: List<TranscriptRow>,
    initialMode: ExamValueMode,
    prerequisiteStatuses: Map<Long, PrerequisiteStatus>,
    onOpenAppelli: (courseKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by remember { mutableStateOf(initialMode) }
    // Which course's detail sheet is open (nested on top of this modal), or null.
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = if (mode == ExamValueMode.Grade) "Esami Sostenuti" else "CFU Acquisiti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    byYear.forEach { (year, exams) ->
                        item(key = "year-$year") {
                            Text(
                                text = if (year <= 0) "Prerequisiti" else "Anno $year",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(exams, key = { it.id }) { exam ->
                            ExamRow(
                                exam = exam,
                                mode = mode,
                                prerequisiteStatus = prerequisiteStatuses[exam.id],
                                onClick = { detailRow = exam },
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
                        RoundedCornerShape(100)
                    )
            ) {
                SegmentedSwitch(
                    selectedMode = mode,
                    onModeSelected = { mode = it }
                )
            }
        }
    }

    detailRow?.let { row ->
        CourseDetailSheet(
            row = row,
            onOpenAppelli = { courseKey ->
                // Close both this nested sheet and the libretto modal before driving the
                // cross-screen deep-link, so the back stack lands cleanly on the appelli.
                detailRow = null
                onDismiss()
                onOpenAppelli(courseKey)
            },
            onDismiss = { detailRow = null },
        )
    }
}

@Composable
private fun ExamRow(
    exam: TranscriptRow,
    mode: ExamValueMode,
    prerequisiteStatus: PrerequisiteStatus?,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val passed = exam.state == TranscriptRowState.Passed
    // Orange warning only when the activity is NOT passed AND its propedeuticità check came
    // back NotSatisfied (esito != 1). Pending-but-prereqs-OK keeps the neutral clock icon.
    val prereqMissing = !passed && prerequisiteStatus == PrerequisiteStatus.NotSatisfied
    val warningTone = registryBadgeTone(RegistryBadgeTone.Attention)

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
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

@Composable
fun SegmentedSwitch(
    selectedMode: ExamValueMode,
    onModeSelected: (ExamValueMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 100.dp
    val coroutineScope = rememberCoroutineScope()
    val haptic = rememberHapticManager()

    val currentMode by rememberUpdatedState(selectedMode)
    val currentOnModeSelected by rememberUpdatedState(onModeSelected)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(cornerRadius))
            .padding(5.dp)
    ) {
        val halfWidth = maxWidth / 2
        val halfWidthPx = with(LocalDensity.current) { halfWidth.toPx() }

        val offsetXAnimatable =
            remember { Animatable(if (selectedMode == ExamValueMode.Grade) 0f else halfWidthPx) }

        var rawDragX by remember { mutableFloatStateOf(0f) }
        var currentSide by remember { mutableIntStateOf(if (selectedMode == ExamValueMode.Grade) 0 else 1) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(selectedMode, halfWidthPx) {
            if (!isDragging) {
                val targetPx = if (selectedMode == ExamValueMode.Grade) 0f else halfWidthPx
                offsetXAnimatable.animateTo(targetPx, tween(300))
            }
        }

        val dragModifier = Modifier.pointerInput(halfWidthPx) {
            detectHorizontalDragGestures(
                onDragStart = {
                    isDragging = true
                    rawDragX = offsetXAnimatable.value
                    currentSide = if (rawDragX > halfWidthPx / 2f) 1 else 0
                },
                onDragEnd = {
                    isDragging = false
                    val targetMode =
                        if (currentSide == 1) ExamValueMode.Credits else ExamValueMode.Grade
                    coroutineScope.launch {
                        val targetPx = if (targetMode == ExamValueMode.Grade) 0f else halfWidthPx
                        offsetXAnimatable.animateTo(targetPx, tween(200))
                        if (targetMode != currentMode) currentOnModeSelected(targetMode)
                    }
                },
                onDragCancel = {
                    isDragging = false
                    coroutineScope.launch {
                        val targetPx = if (currentMode == ExamValueMode.Grade) 0f else halfWidthPx
                        offsetXAnimatable.animateTo(targetPx, tween(200))
                    }
                }
            ) { change, dragAmount ->
                change.consume()
                rawDragX = (rawDragX + dragAmount).coerceIn(0f, halfWidthPx)

                coroutineScope.launch { offsetXAnimatable.snapTo(rawDragX) }

                val thresholdPx = halfWidthPx / 2f
                val newSide = if (rawDragX > thresholdPx) 1 else 0

                if (newSide != currentSide) {
                    currentSide = newSide
                    haptic.tap()

                    val targetMode =
                        if (currentSide == 1) ExamValueMode.Credits else ExamValueMode.Grade
                    if (targetMode != currentMode) currentOnModeSelected(targetMode)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(dragModifier)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    .width(halfWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius),
                    onClick = {
                        currentOnModeSelected(ExamValueMode.Grade)
                        haptic.tap()
                    }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Voti",
                            color = if (selectedMode == ExamValueMode.Grade) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(cornerRadius),
                    onClick = {
                        currentOnModeSelected(ExamValueMode.Credits)
                        haptic.tap()
                    }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Crediti",
                            color = if (selectedMode == ExamValueMode.Credits) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
