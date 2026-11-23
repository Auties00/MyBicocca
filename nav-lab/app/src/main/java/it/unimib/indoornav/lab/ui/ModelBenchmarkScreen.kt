package it.unimib.indoornav.lab.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.ui.components.BeaconModelView
import it.unimib.indoornav.lab.ui.components.BeaconPose
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.components.SegmentedSwitch
import it.unimib.indoornav.lab.ui.model.BeaconRenderSpec
import it.unimib.indoornav.lab.ui.model.BenchmarkStatsCalculator
import it.unimib.indoornav.lab.ui.model.CpuBeaconRenderer
import it.unimib.indoornav.lab.ui.model.GlBeaconOffscreenRenderer
import it.unimib.indoornav.lab.ui.model.ModelRenderer
import it.unimib.indoornav.lab.ui.model.PoseAngles
import it.unimib.indoornav.lab.ui.model.RendererBenchStats
import it.unimib.indoornav.lab.ui.model.VikingMesh
import it.unimib.indoornav.lab.ui.theme.LabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private const val BENCH_TAG = "LabModelBench"
private const val ROLLING_WINDOW = 120
private const val BENCH_FRAMES = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelBenchmarkScreen(
    selectedRenderer: ModelRenderer,
    onSelectRenderer: (ModelRenderer) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val inPreview = LocalInspectionMode.current
    val scope = rememberCoroutineScope()

    val detailWidthPx = with(density) { 96.dp.toPx().roundToInt().coerceAtLeast(1) }
    val detailHeightPx = with(density) { 176.dp.toPx().roundToInt().coerceAtLeast(1) }
    val cardWidthPx = with(density) { 44.dp.toPx().roundToInt().coerceAtLeast(1) }
    val cardHeightPx = with(density) { 88.dp.toPx().roundToInt().coerceAtLeast(1) }

    val cpuRolling = remember { ArrayDeque<Long>(ROLLING_WINDOW) }
    val glRolling = remember { ArrayDeque<Long>(ROLLING_WINDOW) }

    val cpuStatsFlow = remember { MutableStateFlow(RendererBenchStats()) }
    val glStatsFlow = remember { MutableStateFlow(RendererBenchStats()) }
    val cpuStats by cpuStatsFlow.collectAsState()
    val glStats by glStatsFlow.collectAsState()
    var benchRunning by remember { mutableStateOf(false) }
    var benchProgressText by remember { mutableStateOf<String?>(null) }
    var benchProgress by remember { mutableFloatStateOf(0f) }

    var spinYaw by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(inPreview) {
        if (inPreview) return@LaunchedEffect
        var lastNs = 0L
        while (true) {
            withFrameNanos { frameNs ->
                if (lastNs != 0L) {
                    val dtSec = (frameNs - lastNs) / 1_000_000_000f
                    spinYaw = (spinYaw + dtSec * 45f) % 360f
                }
                lastNs = frameNs
            }
        }
    }

    // Measure first-render time for a card bitmap for both renderers once on open.
    LaunchedEffect(inPreview, cardWidthPx, cardHeightPx) {
        if (inPreview) return@LaunchedEffect
        val mesh = runCatching { withContext(Dispatchers.IO) { VikingMesh.load(context) } }.getOrNull()
            ?: return@LaunchedEffect
        val cpuFirstMs = withContext(Dispatchers.Default) {
            val t0 = System.nanoTime()
            CpuBeaconRenderer.renderBitmap(
                mesh = mesh,
                label = "B07",
                subLabel = "U14 - Floor 1",
                pose = BeaconRenderSpec.CARD_POSE,
                widthPx = cardWidthPx,
                heightPx = cardHeightPx,
            )
            (System.nanoTime() - t0) / 1_000_000.0
        }
        cpuStatsFlow.value = cpuStatsFlow.value.copy(firstCardMs = cpuFirstMs)

        val glFirstMs = withContext(Dispatchers.Default) {
            runCatching {
                val t0 = System.nanoTime()
                GlBeaconOffscreenRenderer.renderToBitmap(
                    mesh = mesh,
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconRenderSpec.CARD_POSE,
                    widthPx = cardWidthPx,
                    heightPx = cardHeightPx,
                )
                (System.nanoTime() - t0) / 1_000_000.0
            }.getOrNull()
        }
        glStatsFlow.value = glStatsFlow.value.copy(firstCardMs = glFirstMs)
    }

    fun recordLiveFrame(renderer: ModelRenderer, timeNs: Long) {
        if (benchRunning) return
        val queue = if (renderer == ModelRenderer.CPU) cpuRolling else glRolling
        val targetFlow = if (renderer == ModelRenderer.CPU) cpuStatsFlow else glStatsFlow
        val summary = synchronized(queue) {
            if (queue.size >= ROLLING_WINDOW) queue.removeFirst()
            queue.addLast(timeNs)
            BenchmarkStatsCalculator.summarizeNs(
                samplesNs = queue.toList(),
                firstCardMs = targetFlow.value.firstCardMs,
            )
        }
        targetFlow.value = summary
    }

    fun runFullBenchmark() {
        if (benchRunning) return
        benchRunning = true
        benchProgress = 0f
        benchProgressText = "Preparing mesh..."
        scope.launch {
            try {
                val mesh = withContext(Dispatchers.IO) { VikingMesh.load(context) }

                benchProgressText = "CPU card first render..."
                val cpuFirstMs = withContext(Dispatchers.Default) {
                    val t0 = System.nanoTime()
                    CpuBeaconRenderer.renderBitmap(
                        mesh = mesh,
                        label = "B07",
                        subLabel = "U14 - Floor 1",
                        pose = BeaconRenderSpec.CARD_POSE,
                        widthPx = cardWidthPx,
                        heightPx = cardHeightPx,
                    )
                    (System.nanoTime() - t0) / 1_000_000.0
                }

                val cpuTimesNs = withContext(Dispatchers.Default) {
                    val scratch = CpuBeaconRenderer.ScratchBuffers()
                    var bmpA: android.graphics.Bitmap? = null
                    var bmpB: android.graphics.Bitmap? = null
                    LongArray(BENCH_FRAMES) { i ->
                        val yaw = -12f + (i * 360f / BENCH_FRAMES)
                        val target = if (i % 2 == 0) bmpA else bmpB
                        val res = CpuBeaconRenderer.renderBitmap(
                            mesh = mesh,
                            label = "B07",
                            subLabel = "U14 - Floor 1",
                            pose = PoseAngles(-58f, yaw, 56f),
                            widthPx = detailWidthPx,
                            heightPx = detailHeightPx,
                            scratch = scratch,
                            recycleTarget = target,
                        )
                        if (i % 2 == 0) bmpA = res.bitmap else bmpB = res.bitmap
                        if (i % 15 == 14 || i == BENCH_FRAMES - 1) {
                            val done = i + 1
                            withContext(Dispatchers.Main) {
                                benchProgressText = "Running CPU $done/$BENCH_FRAMES..."
                                benchProgress = done / (BENCH_FRAMES * 2f)
                            }
                        }
                        res.renderTimeNs
                    }
                }
                val cpuSummary = BenchmarkStatsCalculator.summarizeNs(cpuTimesNs.toList(), cpuFirstMs)
                cpuStatsFlow.value = cpuSummary
                synchronized(cpuRolling) {
                    cpuRolling.clear()
                    cpuTimesNs.takeLast(ROLLING_WINDOW).forEach { cpuRolling.addLast(it) }
                }
                Log.i(
                    BENCH_TAG,
                    String.format(
                        Locale.US,
                        "renderer=CPU frames=%d size=%dx%d medianMs=%.2f p95Ms=%.2f maxMs=%.2f firstCardMs=%.2f",
                        BENCH_FRAMES,
                        detailWidthPx,
                        detailHeightPx,
                        cpuSummary.medianMs ?: 0.0,
                        cpuSummary.p95Ms ?: 0.0,
                        cpuSummary.maxMs ?: 0.0,
                        cpuSummary.firstCardMs ?: 0.0,
                    ),
                )

                benchProgressText = "GL card first render..."
                val glFirstMs = withContext(Dispatchers.Default) {
                    val t0 = System.nanoTime()
                    GlBeaconOffscreenRenderer.renderToBitmap(
                        mesh = mesh,
                        label = "B07",
                        subLabel = "U14 - Floor 1",
                        pose = BeaconRenderSpec.CARD_POSE,
                        widthPx = cardWidthPx,
                        heightPx = cardHeightPx,
                    )
                    (System.nanoTime() - t0) / 1_000_000.0
                }

                val glTimesNs = withContext(Dispatchers.Default) {
                    GlBeaconOffscreenRenderer.benchmarkFrames(
                        mesh = mesh,
                        frameCount = BENCH_FRAMES,
                        widthPx = detailWidthPx,
                        heightPx = detailHeightPx,
                    ) { done ->
                        scope.launch {
                            benchProgressText = "Running GL $done/$BENCH_FRAMES..."
                            benchProgress = 0.5f + done / (BENCH_FRAMES * 2f)
                        }
                    }
                }
                val glSummary = BenchmarkStatsCalculator.summarizeNs(glTimesNs.toList(), glFirstMs)
                glStatsFlow.value = glSummary
                synchronized(glRolling) {
                    glRolling.clear()
                    glTimesNs.takeLast(ROLLING_WINDOW).forEach { glRolling.addLast(it) }
                }
                Log.i(
                    BENCH_TAG,
                    String.format(
                        Locale.US,
                        "renderer=GL frames=%d size=%dx%d medianMs=%.2f p95Ms=%.2f maxMs=%.2f firstCardMs=%.2f",
                        BENCH_FRAMES,
                        detailWidthPx,
                        detailHeightPx,
                        glSummary.medianMs ?: 0.0,
                        glSummary.p95Ms ?: 0.0,
                        glSummary.maxMs ?: 0.0,
                        glSummary.firstCardMs ?: 0.0,
                    ),
                )
                benchProgressText = "Completed 300 frames each (logged to $BENCH_TAG)"
                benchProgress = 1f
            } catch (t: Throwable) {
                Log.e(BENCH_TAG, "Benchmark failed", t)
                benchProgressText = "Benchmark failed: ${t.message}"
            } finally {
                benchRunning = false
            }
        }
    }

    ModelBenchmarkContent(
        selectedRenderer = selectedRenderer,
        spinYaw = spinYaw,
        cpuStats = cpuStats,
        glStats = glStats,
        benchRunning = benchRunning,
        benchProgress = benchProgress,
        benchProgressText = benchProgressText,
        detailSizePx = "${detailWidthPx}x${detailHeightPx}",
        onSelectRenderer = onSelectRenderer,
        onRecordLiveFrame = ::recordLiveFrame,
        onRunBenchmark = ::runFullBenchmark,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelBenchmarkContent(
    selectedRenderer: ModelRenderer,
    spinYaw: Float,
    cpuStats: RendererBenchStats,
    glStats: RendererBenchStats,
    benchRunning: Boolean,
    benchProgress: Float,
    benchProgressText: String?,
    detailSizePx: String,
    onSelectRenderer: (ModelRenderer) -> Unit,
    onRecordLiveFrame: (ModelRenderer, Long) -> Unit,
    onRunBenchmark: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "3D Renderer & Benchmark",
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surface,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                OrganicBeaconHalo(
                    online = true,
                    shape = OrganicShapes.SmoothCookie12,
                    modifier = Modifier.size(190.dp),
                )
                BeaconModelView(
                    label = "B07",
                    subLabel = "U14 - Floor 1",
                    pose = BeaconPose.DETAIL,
                    online = true,
                    renderer = selectedRenderer,
                    interactive = false,
                    overrideAngles = PoseAngles(
                        pitchDeg = BeaconRenderSpec.DETAIL_POSE.pitchDeg,
                        yawDeg = BeaconRenderSpec.DETAIL_POSE.yawDeg + spinYaw,
                        rollDeg = BeaconRenderSpec.DETAIL_POSE.rollDeg,
                    ),
                    onFrameTimeNs = { ns -> onRecordLiveFrame(selectedRenderer, ns) },
                    modifier = Modifier.size(width = 96.dp, height = 176.dp),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Active 3D Renderer (persisted)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                SegmentedSwitch(
                    options = ModelRenderer.entries,
                    selected = selectedRenderer,
                    onSelected = onSelectRenderer,
                    label = { it.label },
                )
                Text(
                    text = "Stage resolution: $detailSizePx px · Rolling window: last $ROLLING_WINDOW live frames",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RendererStatsCard(
                        title = "GL (ES 3.0)",
                        active = selectedRenderer == ModelRenderer.GL,
                        stats = glStats,
                        modifier = Modifier.weight(1f),
                    )
                    RendererStatsCard(
                        title = "CPU (2x SS)",
                        active = selectedRenderer == ModelRenderer.CPU,
                        stats = cpuStats,
                        modifier = Modifier.weight(1f),
                    )
                }

                Button(
                    onClick = onRunBenchmark,
                    enabled = !benchRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (benchRunning) "Running benchmark..." else "Run benchmark (300 frames each)")
                }

                if (benchRunning) {
                    LinearProgressIndicator(
                        progress = { benchProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                benchProgressText?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RendererStatsCard(
    title: String,
    active: Boolean,
    stats: RendererBenchStats,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (active) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            width = if (active) 1.5.dp else 1.dp,
            color = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            StatLine("Median", formatMs(stats.medianMs))
            StatLine("p95", formatMs(stats.p95Ms))
            StatLine("Max", formatMs(stats.maxMs))
            StatLine("1st card", formatMs(stats.firstCardMs))
            Text(
                text = "${stats.sampleCount} samples",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatMs(ms: Double?): String {
    return ms?.let { String.format(Locale.US, "%.2f ms", it) } ?: "—"
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun ModelBenchmarkScreenPreview() {
    LabTheme {
        ModelBenchmarkContent(
            selectedRenderer = ModelRenderer.GL,
            spinYaw = 25f,
            cpuStats = RendererBenchStats(
                medianMs = 11.42,
                p95Ms = 14.80,
                maxMs = 18.10,
                firstCardMs = 4.95,
                sampleCount = 120,
            ),
            glStats = RendererBenchStats(
                medianMs = 1.85,
                p95Ms = 2.41,
                maxMs = 4.12,
                firstCardMs = 9.20,
                sampleCount = 120,
            ),
            benchRunning = false,
            benchProgress = 0f,
            benchProgressText = "Completed 300 frames each (logged to LabModelBench)",
            detailSizePx = "288x528",
            onSelectRenderer = {},
            onRecordLiveFrame = { _, _ -> },
            onRunBenchmark = {},
            onBack = {},
        )
    }
}
