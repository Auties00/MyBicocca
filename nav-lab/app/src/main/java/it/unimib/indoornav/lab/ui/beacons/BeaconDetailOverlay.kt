package it.unimib.indoornav.lab.ui.beacons

import it.unimib.indoornav.lab.ui.components.PacketRateText
import it.unimib.indoornav.lab.ui.components.SlotBadge
import it.unimib.indoornav.lab.inventory.SlotPills
import androidx.compose.material.icons.filled.Map
import it.unimib.indoornav.lab.inventory.BeaconPhotos
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import it.unimib.indoornav.lab.ui.components.rememberReduceMotion
import it.unimib.indoornav.lab.ui.components.BatteryBadge
import it.unimib.indoornav.lab.inventory.SavedBeaconEntry
import it.unimib.indoornav.lab.inventory.BeaconBattery
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unimib.indoornav.lab.format.BeaconFrameType
import it.unimib.indoornav.lab.format.CanonicalBeaconId
import it.unimib.indoornav.lab.inventory.ResolvedBeaconTile
import it.unimib.indoornav.lab.ui.components.BeaconModelView
import it.unimib.indoornav.lab.ui.components.OrganicBeaconHalo
import it.unimib.indoornav.lab.ui.components.OrganicShapes
import it.unimib.indoornav.lab.ui.components.SignalTypeBadge
import it.unimib.indoornav.lab.ui.theme.LabTheme
import it.unimib.indoornav.lab.ui.theme.SignalBadgeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val STAGE_HEIGHT = 285.dp
private val HERO_SIZE = 240.dp
private val HERO_TOP = 16.dp
private val TITLE_FADE = 48.dp
private const val HALO_DEGREES_PER_DP = 0.25f

private enum class DetailTab(val title: String, val dotColor: Color?) {
    GENERAL("General", null),
    ED_UID("Eddystone UID", SignalBadgeTokens.EdUid.dotColor),
    IBEACON("iBeacon", SignalBadgeTokens.IBeacon.dotColor),
    TELEMETRY("Telemetry", SignalBadgeTokens.EdTlm.dotColor),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeaconDetailOverlay(
    tile: ResolvedBeaconTile,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    var photoOpen by remember { mutableStateOf(false) }

    val uidCanonical = remember(tile) { findCanonical(tile, "ed:", BeaconFrameType.ED_UID) }
    val ibeaconCanonical = remember(tile) { findCanonical(tile, "ib:", BeaconFrameType.IBEACON) }
    val hasUid = BeaconFrameType.ED_UID in tile.frameTypes || uidCanonical != null
    val hasIBeacon = BeaconFrameType.IBEACON in tile.frameTypes || ibeaconCanonical != null
    val hasTelemetry = BeaconFrameType.ED_TLM in tile.frameTypes || tile.tlmBattMv != null

    val visibleTabs = remember(hasUid, hasIBeacon, hasTelemetry) {
        buildList {
            add(DetailTab.GENERAL)
            if (hasUid) add(DetailTab.ED_UID)
            if (hasIBeacon) add(DetailTab.IBEACON)
            if (hasTelemetry) add(DetailTab.TELEMETRY)
        }
    }

    val pagerState = rememberPagerState(pageCount = { visibleTabs.size })
    val selectedTab = visibleTabs.getOrElse(pagerState.currentPage) { DetailTab.GENERAL }

    val tabScrollState = rememberScrollState()
    val subLabel = listOf(tile.building, tile.floor).filter { it.isNotBlank() }.joinToString(" - ")
    val topTitle = tile.alphanumericCode.ifBlank { tile.displayLabel }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    // The label joins the title once the top half of the model has scrolled under the bar.
    val titleFadeStartPx = with(density) { (HERO_TOP + HERO_SIZE / 2).toPx() }
    val titleFadePx = with(density) { TITLE_FADE.toPx() }
    val reduceMotion = rememberReduceMotion()
    val haloRotation = if (reduceMotion) 0f else scrollState.value / density.density * HALO_DEGREES_PER_DP

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val hero = LocalBeaconHero.current
            var barHeightPx by remember { mutableIntStateOf(0) }
            TopAppBar(
                modifier = Modifier
                    .heroCeiling()
                    .onSizeChanged { barHeightPx = it.height }
                    .clipToBounds(),
                // The screen around it already sits below the status bar.
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        // The flying halo is cut at an edge that rises from the bar's bottom to its top
                        // while the detail closes; the title rises with it so the halo never covers it.
                        modifier = Modifier.graphicsLayer {
                            val p = hero?.takeIf { it.key == tile.tileKey }?.progress?.value ?: 1f
                            translationY = -(1f - p) * barHeightPx
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = topTitle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (topTitle != tile.displayLabel) {
                            Text(
                                text = tile.displayLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.graphicsLayer {
                                    alpha = ((scrollState.value - titleFadeStartPx) / titleFadePx).coerceIn(0f, 1f)
                                },
                            )
                        }
                    }
                },
                actions = {
                    if (onShowOnMap != null && tile.savedEntry?.x != null && tile.savedEntry.y != null) {
                        IconButton(onClick = onShowOnMap) {
                            Icon(Icons.Default.Map, contentDescription = "Show on map")
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (tile.isSaved) "Edit" else "Save to inventory") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                onClick = {
                                    menuOpen = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Copy MAC") },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                },
                                enabled = tile.macAddress.isNotBlank(),
                                onClick = {
                                    menuOpen = false
                                    copyMacToClipboard(context, tile.macAddress)
                                },
                            )
                            if (tile.isSaved) {
                                DropdownMenuItem(
                                    text = { Text("Remove beacon", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        confirmRemove = true
                                    },
                                )
                            }
                        }
                    }
                },
            )

            Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(STAGE_HEIGHT)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    MaterialTheme.colorScheme.surface,
                                ),
                            ),
                        ),
                ) {
                    BeaconHero(
                        tileKey = tile.tileKey,
                        label = tile.displayLabel,
                        subLabel = subLabel,
                        online = tile.isOnline,
                        stale = tile.isStale,
                        cardShape = OrganicShapes.cardShapeFor(tile.tileKey),
                        inDetail = true,
                        size = HERO_SIZE,
                        haloRotation = haloRotation,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = HERO_TOP),
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        FlowRow(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            SlotPills.of(tile).forEach { (slot, state) -> SlotBadge(slot, state) }
                        }

                        if (tile.isOnline && tile.currentRssi != null) {
                            CornerPill(highlighted = true, modifier = Modifier.padding(start = 6.dp)) {
                                PacketRateText(
                                    packetsPerSec = tile.packetsPerSec ?: 0.0,
                                    trackerKeys = tile.trackerKeys(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    mono = true,
                                    suffix = " · Tx ${tile.txPowerAdv ?: "-"} · ${tile.currentRssi} dBm",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        } else {
                            CornerPill(
                                text = "Offline",
                                highlighted = false,
                                mono = true,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tabScrollState)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    visibleTabs.forEachIndexed { index, tab ->
                        val active = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (tab.dotColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(tab.dotColor),
                                    )
                                }
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        when (visibleTabs.getOrElse(page) { DetailTab.GENERAL }) {
                            DetailTab.GENERAL -> GeneralTabContent(tile)
                            DetailTab.ED_UID -> EddystoneUidTabContent(tile, uidCanonical)
                            DetailTab.IBEACON -> IBeaconTabContent(tile, ibeaconCanonical)
                            DetailTab.TELEMETRY -> TelemetryTabContent(tile)
                        }
                    }
                }
            }

            // Outside the scrolling column, so they stay right under the bar while the page scrolls. With a
            // reference photo, its preview takes the top-right corner and the floor moves under the building.
            val photo = tile.savedEntry?.photoFile
            val photoBitmap = remember(photo) {
                photo?.let { runCatching { BitmapFactory.decodeFile(BeaconPhotos.file(context, it).path)?.asImageBitmap() }.getOrNull() }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CornerPill(text = tile.building.ifBlank { "—" }, highlighted = tile.building.isNotBlank())
                if (photoBitmap != null) {
                    CornerPill(text = tile.floor.ifBlank { "—" }, highlighted = false)
                }
            }
            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = "Reference photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 10.dp)
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { photoOpen = true },
                )
            } else {
                CornerPill(
                    text = tile.floor.ifBlank { if (tile.isSaved) "—" else "Unsaved" },
                    highlighted = false,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 10.dp),
                )
            }
            if (photoOpen && photoBitmap != null) {
                PhotoViewer(photoBitmap, onDismiss = { photoOpen = false })
            }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove this beacon?") },
            text = { Text("${tile.displayLabel} is removed from the inventory. It still shows in Survey while it transmits.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    onRemove()
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } },
        )
    }
}

/** The reference photo full screen; pinch to zoom, tap outside or back to close. */
@Composable
private fun PhotoViewer(bitmap: ImageBitmap, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset = if (scale == 1f) Offset.Zero else offset + pan
                    }
                }
                .pointerInput(Unit) { detectTapGestures { if (scale == 1f) onDismiss() } },
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Reference photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
    }
}

@Composable
private fun BatteryCard(entry: SavedBeaconEntry, liveBattMv: Int?) {
    val estimate = BeaconBattery.estimate(entry, System.currentTimeMillis())
    val lifeDays = BeaconBattery.lifeDays(entry.advIntervalMs, BeaconBattery.slotsOf(entry))
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val slots = BeaconBattery.slotsOf(entry).joinToString(" + ") { it.label }.ifBlank { "no slots" }
    val voltage = (liveBattMv ?: entry.lastBattMv)?.let { " · %.2f V".format(Locale.US, it / 1000.0) } ?: ""
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BATTERY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                estimate?.let { BatteryBadge(it.remainingFraction) }
            }
            Text(
                text = if (estimate != null) {
                    "Replace by about ${dateFormat.format(Date(estimate.replaceByMs))}"
                } else {
                    "Not tracked yet. Use ⋮ → Edit → Reset battery expectancy when a new battery goes in."
                },
                fontSize = 13.sp,
            )
            Text(
                text = "CR2477 · $slots every ${entry.advIntervalMs} ms · about ${formatLife(lifeDays)}$voltage",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CornerPill(
    text: String,
    highlighted: Boolean,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
) {
    CornerPill(highlighted, modifier) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            color = if (highlighted) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun CornerPill(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content,
    )
}

@Composable
private fun GeneralTabContent(tile: ResolvedBeaconTile) {
    val frameList = tile.frameTypes.joinToString(", ") {
        SignalBadgeTokens.forFrameType(it).fullLabel
    }.ifBlank { "None" }

    val rssiLine = if (tile.isOnline && tile.currentRssi != null) {
        "${tile.currentRssi} dBm (median ${tile.medianRssi ?: "-"} dBm)"
    } else if (tile.medianRssi != null) {
        "Offline (last median ${tile.medianRssi} dBm)"
    } else {
        "Offline"
    }

    val ppsLine = if (tile.isOnline && tile.packetsPerSec != null) {
        "%.2f packets/s".format(Locale.US, tile.packetsPerSec)
    } else {
        "0.00 packets/s (Offline)"
    }

    val intervalLine = if (tile.estimatedIntervalByFrame.isEmpty()) {
        if (tile.isOnline) "Collecting samples..." else "Unavailable while offline"
    } else {
        tile.estimatedIntervalByFrame.entries.joinToString(" · ") { (ft, ms) ->
            "${SignalBadgeTokens.forFrameType(ft).shortLabel}: ≈ $ms ms"
        }
    }

    val lastSeenLine = formatLastSeen(tile.lastSeenMs)

    FactCard(
        fields = listOf(
            FactRow("MAC Address", tile.macAddress.ifBlank { "Unavailable" }, mono = true),
            FactRow("Frame Types Seen", frameList),
            FactRow("Current & Median RSSI", rssiLine, mono = true),
            FactRow("Packets/s", ppsLine, mono = true),
            FactRow("Estimated Adv. Interval", intervalLine, mono = true),
            FactRow("Last Seen", lastSeenLine),
        ),
    )

    tile.savedEntry?.let { BatteryCard(it, tile.tlmBattMv) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "POSITION / DESCRIPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = tile.positionDescription.ifBlank { "No description saved. Use ⋮ → Edit to add one." },
                fontSize = 13.sp,
                color = if (tile.positionDescription.isNotBlank()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "RAW ADVERTISEMENT HEX",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (tile.rawAdvHexByFrame.isEmpty()) {
                Text(
                    text = "No advertisement bytes captured in current scan.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                tile.rawAdvHexByFrame.forEach { (ft, hex) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = SignalBadgeTokens.forFrameType(ft).fullLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = hex,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EddystoneUidTabContent(
    tile: ResolvedBeaconTile,
    canonical: String?,
) {
    val parsed = canonical?.let { CanonicalBeaconId.parse(it) as? CanonicalBeaconId.EddystoneUid }
    val txPower = tile.liveBeacon?.framesByType?.get(BeaconFrameType.ED_UID)?.txPowerAdv ?: tile.txPowerAdv

    FactCard(
        fields = listOf(
            FactRow("Namespace", parsed?.namespaceHex ?: "Unavailable", mono = true),
            FactRow("Instance", parsed?.instanceHex ?: "Unavailable", mono = true),
            FactRow("Calibrated Tx Power (0 m)", txPower?.let { "$it dBm" } ?: "Unavailable", mono = true),
        ),
    )
}

@Composable
private fun IBeaconTabContent(
    tile: ResolvedBeaconTile,
    canonical: String?,
) {
    val parsed = canonical?.let { CanonicalBeaconId.parse(it) as? CanonicalBeaconId.IBeacon }
    val txPower = tile.liveBeacon?.framesByType?.get(BeaconFrameType.IBEACON)?.txPowerAdv ?: tile.txPowerAdv

    FactCard(
        fields = listOf(
            FactRow("UUID", parsed?.uuid ?: "Unavailable", mono = true),
            FactRow("Major", parsed?.major?.toString() ?: "Unavailable", mono = true),
            FactRow("Minor", parsed?.minor?.toString() ?: "Unavailable", mono = true),
            FactRow("Measured Power (1 m)", txPower?.let { "$it dBm" } ?: "Unavailable", mono = true),
        ),
    )
}

@Composable
private fun TelemetryTabContent(tile: ResolvedBeaconTile) {
    val rows = buildList {
        add(FactRow("Battery", tile.tlmBattMv?.let { "$it mV" } ?: "Unavailable", mono = true))
        tile.tlmAdvCount?.let { count ->
            add(FactRow("Advertising Count", "%,d".format(Locale.US, count), mono = true))
        }
        tile.tlmUptimeSec?.let { uptimeSec ->
            add(FactRow("Uptime", formatUptime(uptimeSec), mono = true))
        }
    }
    FactCard(fields = rows)
}

private data class FactRow(
    val label: String,
    val value: String,
    val mono: Boolean = false,
)

@Composable
private fun FactCard(fields: List<FactRow>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            fields.forEachIndexed { idx, row ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = row.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = row.value,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (row.mono) FontFamily.Monospace else FontFamily.Default,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (idx < fields.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

private fun findCanonical(
    tile: ResolvedBeaconTile,
    prefix: String,
    frameType: BeaconFrameType,
): String? {
    tile.liveBeacon?.framesByType?.get(frameType)?.canonicalId?.takeIf { it.startsWith(prefix) }?.let { return it }
    if (tile.canonicalKey.startsWith(prefix)) return tile.canonicalKey
    return tile.savedEntry?.linkedKeys?.firstOrNull { it.startsWith(prefix) }
}

private fun formatLastSeen(lastSeenMs: Long): String {
    if (lastSeenMs <= 0L) return "Never"
    val now = System.currentTimeMillis()
    val ageSec = ((now - lastSeenMs) / 1_000L).coerceAtLeast(0L)
    val rel = when {
        ageSec < 60L -> "${ageSec}s ago"
        ageSec < 3600L -> "${ageSec / 60}m ${ageSec % 60}s ago"
        else -> "${ageSec / 3600}h ${(ageSec % 3600) / 60}m ago"
    }
    val clock = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(lastSeenMs))
    return "$clock ($rel)"
}

private fun formatUptime(uptimeSec: Double): String {
    val total = uptimeSec.toLong().coerceAtLeast(0L)
    val days = total / 86_400L
    val hours = (total % 86_400L) / 3_600L
    val minutes = (total % 3_600L) / 60L
    val seconds = total % 60L
    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "%.1f s".format(Locale.US, uptimeSec)
    }
}

private fun copyMacToClipboard(context: Context, mac: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("Beacon MAC", mac))
    Toast.makeText(context, "Copied $mac", Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BeaconDetailOverlayPreview() {
    LabTheme {
        BeaconDetailOverlay(
            tile = ResolvedBeaconTile(
                tileKey = "1",
                savedEntry = null,
                liveBeacon = null,
                isOnline = true,
                displayLabel = "B07",
                alphanumericCode = "K4M2",
                building = "U14",
                floor = "Floor 1",
                positionDescription = "Near Lab 102 door (2.2 m pillar)",
                macAddress = "D9:84:CE:34:5C:5C",
                canonicalKey = "ed:515b86185cea0ec01b22:0000000e0007",
                frameTypes = listOf(BeaconFrameType.ED_UID, BeaconFrameType.ED_TLM),
                currentRssi = -54,
                medianRssi = -56,
                packetsPerSec = 4.2,
                txPowerAdv = -40,
                tlmBattMv = 3120,
                tlmTempC = 22.4,
                tlmAdvCount = 482_910L,
                tlmUptimeSec = 94_215.0,
                estimatedIntervalByFrame = mapOf(
                    BeaconFrameType.ED_UID to 200,
                    BeaconFrameType.ED_TLM to 1000,
                ),
                rawAdvHexByFrame = mapOf(
                    BeaconFrameType.ED_UID to "0201060303aafe1716aafe00d8515b86185cea0ec01b220000000e0007",
                    BeaconFrameType.ED_TLM to "0201060303aafe1116aafe20000c30166600075e4e000e5e46",
                ),
                lastSeenMs = System.currentTimeMillis() - 2_000L,
            ),
            onClose = {},
            onEdit = {},
            onRemove = {},
        )
    }
}
