package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Panorama
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.domain.model.map.MapRoom
import it.attendance100.mybicocca.domain.model.map.MapRoomDetail
import it.attendance100.mybicocca.ui.screen.map.component.icon
import it.attendance100.mybicocca.ui.screen.map.component.label

@Composable
fun BuildingDetailSheet(
    building: MapBuilding,
    rooms: Loadable<List<MapRoom>>,
    syncStatus: SyncStatus,
    selectedRoom: MapRoom?,
    roomDetail: Loadable<MapRoomDetail?>,
    onRoomClick: (MapRoom) -> Unit,
    onOpen360: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = building.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(building.category.label) },
                    leadingIcon = { Icon(building.category.icon, contentDescription = null) },
                )
                building.address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Aule",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        val roomList = (rooms as? Loadable.Loaded)?.value
        when {
            rooms is Loadable.NotYetLoaded || (roomList.isNullOrEmpty() && syncStatus is SyncStatus.Refreshing) -> {
                item { LoadingRow() }
            }

            roomList.isNullOrEmpty() -> {
                item {
                    Text(
                        text = "Nessuna aula disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                items(roomList, key = { it.code.value }) { room ->
                    RoomRow(
                        room = room,
                        expanded = room == selectedRoom,
                        detail = if (room == selectedRoom) roomDetail else Loadable.Loaded(null),
                        onClick = { onRoomClick(room) },
                        onOpen360 = onOpen360,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun RoomRow(
    room: MapRoom,
    expanded: Boolean,
    detail: Loadable<MapRoomDetail?>,
    onClick: () -> Unit,
    onOpen360: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = room.name, style = MaterialTheme.typography.bodyLarge)
            room.capacity?.let { capacity ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$capacity",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (expanded) {
            RoomDetailPanel(roomName = room.name, detail = detail, onOpen360 = onOpen360)
        }
    }
}

@Composable
private fun RoomDetailPanel(
    roomName: String,
    detail: Loadable<MapRoomDetail?>,
    onOpen360: (String, String) -> Unit,
) {
    when (detail) {
        Loadable.NotYetLoaded -> Box(Modifier.padding(vertical = 8.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }

        is Loadable.Loaded -> {
            val value = detail.value
            if (value == null) {
                Text(
                    text = "Dettagli non disponibili",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                value.floor?.let { floor ->
                    InfoLine(
                        icon = { Icon(Icons.Outlined.Layers, null, Modifier.size(18.dp)) },
                        text = if (floor == 0) "Piano terra" else "Piano $floor",
                    )
                }
                if (value.isAccessible) {
                    InfoLine(
                        icon = { Icon(Icons.Outlined.Accessible, null, Modifier.size(18.dp)) },
                        text = "Accessibile",
                    )
                }
                value.roomType?.let { type ->
                    Text(text = type, style = MaterialTheme.typography.bodySmall)
                }
                if (value.equipment.isNotEmpty()) {
                    Text(
                        text = value.equipment.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                value.interactive360Url?.let { url ->
                    Button(onClick = { onOpen360(url, roomName) }) {
                        Icon(Icons.Outlined.Panorama, contentDescription = null)
                        Text(text = "Vista 360°", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        icon()
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
