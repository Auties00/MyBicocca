package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Accessible
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.domain.model.map.RoomScheduleEntry
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Headerless room page hosted inside a sheet pager (the hosting sheet provides the morphing
 * header): everything EasyStaff knows about the room — floor, capacity, type, description,
 * accessibility (incl. notes and B.Inclusion validation), equipment, today's slots.
 *
 * The loading state is held for a beat so quick fetches don't flash it; the detail is never
 * cached, so reopening a room re-fetches, and everything (buttons included) stays hidden
 * until the data lands. The page may only grow/shrink vertically as the detail lands: the
 * height change is animated here instead of letting the hosting modal snap to the new size.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RoomDetailPage(
    room: MapRoom,
    detail: Loadable<MapRoomDetail?>,
    todayEntries: List<RoomScheduleEntry>?,
    modifier: Modifier = Modifier,
) {
    val detailValue = (detail as? Loadable.Loaded)?.value

    val showLoading = rememberMinDurationLoading(loading = detail is Loadable.NotYetLoaded)

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec)
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (showLoading) {
            SheetLoadingIndicator(label = stringResource(R.string.map_room_loading))
        } else {
            RoomFeatures(room = room, detail = detailValue)

            detailValue?.description?.let { DescriptionSection(it) }

            detailValue?.let { AccessibilitySection(it) }

            detailValue?.equipment?.takeIf { it.isNotEmpty() }?.let { EquipmentSection(it) }

            TodaySchedule(entries = todayEntries)
        }
    }
}

/** The freshly scraped values win; the cached room's floor and capacity only fill in when the detail lacks them. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomFeatures(room: MapRoom, detail: MapRoomDetail?) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (detail?.floor ?: room.floor)?.let { floor ->
            FeaturePill(
                icon = Icons.Outlined.Layers,
                text = if (floor == 0) {
                    stringResource(R.string.map_floor_ground)
                } else {
                    stringResource(R.string.map_floor_n, floor)
                },
            )
        }
        (detail?.capacity ?: room.capacity)?.let { capacity ->
            FeaturePill(
                icon = Icons.Outlined.Groups,
                text = pluralStringResource(R.plurals.map_seat_count, capacity, capacity),
            )
        }
        detail?.roomType?.let { type ->
            FeaturePill(icon = Icons.Outlined.MeetingRoom, text = type)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

/** The description renders multi-line as-is: the scraper preserves the source line structure. */
@Composable
private fun DescriptionSection(description: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(R.string.map_room_description))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainer,
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            )
        }
    }
}

/**
 * Rendered only when there is something to say: the accessibility flag, the B.Inclusion
 * validation badge, or free-form notes (steps, amphitheater layout, accessible-table booking).
 */
@Composable
private fun AccessibilitySection(detail: MapRoomDetail) {
    val scheme = MaterialTheme.colorScheme
    val hasContent = detail.isAccessible || detail.isInclusionValidated || detail.accessibilityNotes != null
    if (!hasContent) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(R.string.map_room_accessibility))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (detail.isAccessible) {
                    IconLine(
                        icon = Icons.AutoMirrored.Outlined.Accessible,
                        text = stringResource(R.string.map_room_barrier_free),
                    )
                }
                if (detail.isInclusionValidated) {
                    IconLine(
                        icon = Icons.Outlined.Verified,
                        text = stringResource(R.string.map_room_validated_binclusion),
                    )
                }
                detail.accessibilityNotes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = scheme.tertiary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentSection(equipment: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(R.string.map_room_equipment))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            equipment.forEach { item ->
                FeaturePill(icon = Icons.Outlined.Check, text = item, iconTint = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

/**
 * Today's slots sorted by start time, the in-progress one flipped to the error container.
 * Idle entries sit on surfaceContainer to match the Descrizione/Accessibilità cards — bare
 * surface reads near-black against the sheet container in dark mode.
 */
@Composable
private fun TodaySchedule(entries: List<RoomScheduleEntry>?) {
    val scheme = MaterialTheme.colorScheme
    if (entries == null) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle(stringResource(R.string.map_room_today))
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.map_room_no_activities),
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
                color = if (inProgress) scheme.errorContainer else scheme.surfaceContainer,
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
private fun FeaturePill(
    icon: ImageVector?,
    text: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = scheme.surfaceContainer) {
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
                    tint = iconTint,
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
