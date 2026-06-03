package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Panorama
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// EasyStaff's "Prenotazione servizi" portal (AuleStudio) — the closest thing to a per-room
// booking flow the university exposes.
private const val BOOKING_URL =
    "https://gestioneorari.didattica.unimib.it/portaleplanning/unimib-aulestudio/index.php"

// Headerless room page hosted inside a sheet pager (the hosting sheet provides the morphing
// header): everything EasyStaff knows about the room — floor, capacity, type, accessibility,
// equipment, description, today's slots.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RoomDetailPage(
    room: MapRoom,
    detail: Loadable<MapRoomDetail?>,
    todayEntries: List<RoomScheduleEntry>?,
    onOpen360: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val detailValue = (detail as? Loadable.Loaded)?.value

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (detail is Loadable.NotYetLoaded) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            RoomFeatures(room = room, detail = detailValue)
            detailValue?.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        TodaySchedule(entries = todayEntries)

        RoomActionRow(
            roomName = room.name,
            interactive360Url = detailValue?.interactive360Url,
            onOpen360 = onOpen360,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomFeatures(room: MapRoom, detail: MapRoomDetail?) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // The cached floor wins over the freshly scraped one only when the latter is missing.
        (detail?.floor ?: room.floor)?.let { floor ->
            FeaturePill(
                icon = Icons.Outlined.Layers,
                text = if (floor == 0) "Piano terra" else "Piano $floor",
            )
        }
        (detail?.let { room.capacity } ?: room.capacity)?.let { capacity ->
            FeaturePill(
                icon = Icons.Outlined.Groups,
                text = if (capacity == 1) "1 posto" else "$capacity posti",
            )
        }
        detail?.roomType?.let { type ->
            FeaturePill(icon = null, text = type)
        }
        if (detail?.isAccessible == true) {
            FeaturePill(icon = Icons.Outlined.Accessible, text = "Accessibile")
        }
        detail?.equipment?.forEach { item ->
            FeaturePill(icon = null, text = item)
        }
    }
}

// Connected button pair, same scheme as the buildings list action row: VR tour + booking portal.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoomActionRow(
    roomName: String,
    interactive360Url: String?,
    onOpen360: (String, String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val vrBg = if (dark) scheme.primaryContainer else scheme.primary
    val vrFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = { interactive360Url?.let { onOpen360(it, roomName) } },
            enabled = interactive360Url != null,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = vrBg,
                contentColor = vrFg,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Panorama,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Vista VR",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        FilledTonalButton(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(BOOKING_URL))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Prenota",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun TodaySchedule(entries: List<RoomScheduleEntry>?) {
    val scheme = MaterialTheme.colorScheme
    if (entries == null) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Oggi in quest'aula",
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        if (entries.isEmpty()) {
            Text(
                text = "Nessuna attività programmata: libera tutto il giorno",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            return@Column
        }
        val now = remember(entries) { LocalDateTime.now() }
        entries.sortedBy { it.start }.forEach { entry ->
            val inProgress = !now.isBefore(entry.start) && now.isBefore(entry.end)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (inProgress) scheme.errorContainer else scheme.surface,
                contentColor = if (inProgress) scheme.onErrorContainer else scheme.onSurface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${entry.start.toLocalTime().format(TimeFormat)}\n${entry.end.toLocalTime().format(TimeFormat)}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(40.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val meta = listOfNotNull(entry.kind, entry.teacher).joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(
                                text = meta,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (inProgress) scheme.onErrorContainer else scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(icon: ImageVector?, text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = scheme.surface) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = scheme.onSurfaceVariant,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
