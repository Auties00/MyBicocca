package it.attendance100.mybicocca.ui.screen.map.subscreen.buildingsList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.map.BuildingCode
import it.attendance100.mybicocca.domain.model.map.MapBuilding
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.map.component.icon
import it.attendance100.mybicocca.ui.screen.map.component.label

@Composable
fun BuildingsListSheet(
    buildings: List<MapBuilding>,
    onShowOnMap: (BuildingCode) -> Unit,
    onDismiss: () -> Unit,
) {
    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column {
            var expandedCode by remember { mutableStateOf<String?>(null) }

            Text(
                text = "Edifici",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(buildings, key = { it.code.value }) { building ->
                    BuildingRow(
                        building = building,
                        expanded = expandedCode == building.code.value,
                        onToggle = {
                            expandedCode = if (expandedCode == building.code.value) null else building.code.value
                        },
                        onShowOnMap = { onShowOnMap(building.code) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildingRow(
    building: MapBuilding,
    expanded: Boolean,
    onToggle: () -> Unit,
    onShowOnMap: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "building_row_arrow",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = scheme.primaryContainer) {
                        Icon(
                            imageVector = building.category.icon,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp).size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = building.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation),
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    building.address?.let { address ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Outlined.Place,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = scheme.onSurfaceVariant,
                            )
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                    FilledTonalButton(onClick = onShowOnMap) {
                        Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Box(Modifier.size(8.dp))
                        Text("Mostra sulla mappa")
                    }
                }
            }
        }
    }
}
