package it.attendance100.mybicocca.ui.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.util.rememberHapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class GradePoint(
    val name: String,
    val value: Float,
    val credits: Float,
    val date: String?,
    val cumLaude: Boolean,
)

private fun RecordBookRow.isPassed(): Boolean =
    status == RecordBookRow.Status.PASSED || (grade != null && grade >= 18)

private fun RecordBookRow.gradeDisplay(): String? {
    if (grade == null) return null
    return if (cumLaude) "30L" else grade.toString()
}

private fun List<RecordBookRow>.toGradePoints(): List<GradePoint> =
    filter { it.grade != null }
        .map { row ->
            GradePoint(
                name = row.activityName,
                value = if (row.cumLaude) 31f else row.grade!!.toFloat(),
                credits = row.credits,
                date = row.date,
                cumLaude = row.cumLaude,
            )
        }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsamiScreen(
    viewModel: ProfileViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val rows by viewModel.rows.collectAsState()

    val passedExams = remember(rows) { rows.filter { it.isPassed() } }
    val remainingExams = remember(rows) { rows.filter { !it.isPassed() } }
    val grades = remember(passedExams) { passedExams.toGradePoints() }

    val haptic = rememberHapticManager()

    var isPassedExpanded by remember { mutableStateOf(true) }
    var isPendingExpanded by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var highlightedExamName by remember { mutableStateOf<String?>(null) }
    var highlightTrigger by remember { mutableStateOf(0L) }


    if (passedExams.isEmpty() && remainingExams.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
        ) {
            // Chart
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(stringResource(R.string.profile_libretto_grafico_voti), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Box(modifier = Modifier.offset(x = 9.dp)) {
                            GradesChart(
                                grades = grades,
                                primaryColor = MaterialTheme.colorScheme.primary,
                                onGradeSelected = { gradePoint ->
                                    val index = passedExams.indexOfFirst { it.activityName == gradePoint.name }
                                    if (index != -1) {
                                        coroutineScope.launch {
                                            if (!isPassedExpanded) {
                                                isPassedExpanded = true
                                                delay(100)
                                            }
                                            listState.animateScrollToItem(index + 2, scrollOffset = -45)
                                            highlightedExamName = gradePoint.name
                                            highlightTrigger = System.currentTimeMillis()
                                        }
                                    }
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Passed
            if (passedExams.isNotEmpty()) {
                stickyHeader {
                    ExpandableHeader(
                        title = stringResource(R.string.profile_esami_passed),
                        count = passedExams.size,
                        isExpanded = isPassedExpanded,
                        titleColor = MaterialTheme.colorScheme.primary,
                        onToggle = {
                            isPassedExpanded = !isPassedExpanded
                            haptic.tap()
                        },
                    )
                }

                if (isPassedExpanded) {
                    items(passedExams) { row ->
                        ExamCard(
                            row = row,
                            shouldHighlight = row.activityName == highlightedExamName,
                            highlightTrigger = highlightTrigger,
                        )
                    }
                }
            }

            // Pending
            if (remainingExams.isNotEmpty()) {
                stickyHeader {
                    ExpandableHeader(
                        title = stringResource(R.string.profile_esami_pending),
                        count = remainingExams.size,
                        isExpanded = isPendingExpanded,
                        titleColor = MaterialTheme.colorScheme.primary,
                        onToggle = {
                            isPendingExpanded = !isPendingExpanded
                            haptic.tap()
                        },
                    )
                }

                if (isPendingExpanded) {
                    items(remainingExams) { row ->
                        ExamCard(row = row)
                    }
                }
            }
        }
    }
}


@Composable
fun GradesChart(
    grades: List<GradePoint>,
    primaryColor: Color,
    onGradeSelected: (GradePoint) -> Unit = {},
) {
    if (grades.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        return
    }

    val haptic = rememberHapticManager()

    @Suppress("AssignedValueIsNeverRead")
    var selectedGrade by remember { mutableStateOf<GradePoint?>(null) }

    val values = grades.map { it.value }
    val minGrade = values.minOrNull() ?: 18f
    val maxGrade = values.maxOrNull() ?: 30f

    val yAxisMin = minGrade.toDouble()
    val yAxisMax = maxGrade.toDouble()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(grades) {
        modelProducer.runTransaction {
            lineSeries { series(values) }
        }
    }

    val marker = rememberDefaultCartesianMarker(
        labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint,
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = rememberShapeComponent(
                shape = CorneredShape.Pill,
                fill = fill(MaterialTheme.colorScheme.surface),
            ),
            padding = insets(horizontal = 8.dp, vertical = 4.dp),
            margins = insets(bottom = 5.dp),
        ),
    )

    val markerVisibilityListener = remember(grades) {
        object : CartesianMarkerVisibilityListener {
            private fun updateSelectedGrade(targets: List<CartesianMarker.Target>) {
                val lineTarget = targets.filterIsInstance<LineCartesianLayerMarkerTarget>().firstOrNull()
                val point = lineTarget?.points?.firstOrNull()

                if (point != null) {
                    val index = point.entry.x.toInt()
                    if (index in grades.indices) {
                        selectedGrade = grades[index]
                    }
                }
            }

            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                updateSelectedGrade(targets)
                haptic.tap()
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                updateSelectedGrade(targets)
                haptic.feather()
            }

            override fun onHidden(marker: CartesianMarker) {
                selectedGrade?.let {
                    onGradeSelected(it)
                }
            }
        }
    }

    val textColor = MaterialTheme.colorScheme.onSurface
    val zoom = Zoom.fixed(0.1f)

    CartesianChartHost(
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = zoom,
        ),
        scrollState = rememberVicoScrollState(),
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                component = shapeComponent(shape = CorneredShape.Pill, fill = fill(primaryColor)),
                                sizeDp = 6f,
                            ),
                        ),
                    ),
                ),
                rangeProvider = CartesianLayerRangeProvider.fixed(
                    minY = yAxisMin,
                    maxY = yAxisMax,
                ),
                verticalAxisPosition = null,
            ),
            endAxis = VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(
                    color = textColor,
                    margins = Insets(horizontalDp = 8f, verticalDp = 4f),
                ),
                valueFormatter = { _, value, _ ->
                    val gradeValue = value.toInt()
                    if (gradeValue == 31) "30L" else gradeValue.toString()
                },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = null,
            ),
            marker = marker,
            markerVisibilityListener = markerVisibilityListener,
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    )
}


@Composable
fun ExpandableHeader(
    title: String,
    count: Int?,
    isExpanded: Boolean,
    titleColor: Color,
    onToggle: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                if (count != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CountBadge(count = count)
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Espandi",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
fun CountBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Text(
            text = count.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


@Composable
fun ExamCard(
    row: RecordBookRow,
    shouldHighlight: Boolean = false,
    highlightTrigger: Long = 0L,
) {
    val isPassed = row.isPassed()
    val gradeDisplay = row.gradeDisplay()

    val cardBackground = if (isPassed) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val statusColor = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val gradeColor = MaterialTheme.colorScheme.primary

    val haptic = rememberHapticManager()
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(shouldHighlight, highlightTrigger) {
        if (shouldHighlight && highlightTrigger > 0) {
            delay(300)
            repeat(2) {
                val press = PressInteraction.Press(Offset.Zero)
                interactionSource.emit(press)
                haptic.feather()
                delay(100)
                interactionSource.emit(PressInteraction.Release(press))
                delay(100)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground,
        ),
        onClick = {
            haptic.tap()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp),
                color = statusColor,
            ) {}

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = row.activityName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (row.date != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = row.date.split(" ").firstOrNull() ?: row.date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    if (isPassed && gradeDisplay != null) {
                        Surface(
                            color = gradeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.defaultMinSize(minWidth = 44.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = gradeDisplay,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = gradeColor,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = "${row.credits.toInt()} CFU",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
