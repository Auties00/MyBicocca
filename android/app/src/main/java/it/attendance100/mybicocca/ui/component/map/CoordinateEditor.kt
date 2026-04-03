package it.attendance100.mybicocca.ui.component.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

data class EditableBuilding(
    val label: String,
    val points: SnapshotStateList<Pair<Double, Double>>, // lat, lng
)

/**
 * Creates an [EditableBuilding] from a list of lat/lng pairs.
 * The closing point (duplicate of first) should NOT be included — it is added automatically when exporting.
 */
fun editableBuilding(label: String, points: List<Pair<Double, Double>>): EditableBuilding {
    return EditableBuilding(label, mutableStateListOf(*points.toTypedArray()))
}

@Composable
fun CoordinateEditor(
    buildings: List<EditableBuilding>,
    selectedBuildingIndex: Int,
    onBuildingIndexChange: (Int) -> Unit,
    selectedPointIndex: Int,
    onPointIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var stepExponent by remember { mutableFloatStateOf(-4f) } // 10^-4 = 0.0001 degrees ~11m
    var expanded by remember { mutableStateOf(true) }

    val step = 10.0.pow(stepExponent.toDouble())
    val currentBuilding = buildings.getOrNull(selectedBuildingIndex) ?: return
    val clipboardManager = LocalClipboardManager.current

    // Clamp point index when switching buildings
    if (selectedPointIndex >= currentBuilding.points.size) {
        onPointIndexChange(0)
    }

    val currentPoint = currentBuilding.points.getOrNull(selectedPointIndex)

    val panelShape = RoundedCornerShape(16.dp)
    val panelBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

    Box(modifier = modifier.fillMaxSize()) {
        // Toggle button when collapsed
        if (!expanded) {
            Text(
                text = "Edit",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(panelBg)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            return@Box
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .width(300.dp)
                .clip(panelShape)
                .background(panelBg)
                .padding(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Coord Editor",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "X",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { expanded = false }
                        .padding(4.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Building selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                buildings.forEachIndexed { index, building ->
                    FilterChip(
                        selected = index == selectedBuildingIndex,
                        onClick = {
                            onBuildingIndexChange(index)
                            onPointIndexChange(0)
                        },
                        label = { Text(building.label, fontSize = 12.sp) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Point list
            Text("Points:", style = MaterialTheme.typography.labelMedium)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ),
            ) {
                itemsIndexed(currentBuilding.points) { index, (lat, lng) ->
                    val isSelected = index == selectedPointIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onPointIndexChange(index) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "#$index",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.width(24.dp),
                        )
                        Text(
                            "%.8f, %.8f".format(lat, lng),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Current point display
            if (currentPoint != null) {
                val (lat, lng) = currentPoint

                Spacer(Modifier.height(8.dp))

                // D-pad controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Up (increase latitude)
                    IconButton(
                        onClick = {
                            currentBuilding.points[selectedPointIndex] = Pair(lat + step, lng)
                        },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, "North")
                    }

                    // Left / Right
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                currentBuilding.points[selectedPointIndex] =
                                    Pair(lat, lng - step)
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "West")
                        }

                        // Copy all button
                        IconButton(
                            onClick = {
                                val code = buildString {
                                    appendLine("val ${currentBuilding.label} = listOf(")
                                    currentBuilding.points.forEach { (pLat, pLng) ->
                                        appendLine("    Point($pLat, $pLng),")
                                    }
                                    // Closing point
//                                    currentBuilding.points.firstOrNull()?.let { (pLat, pLng) ->
//                                        appendLine("    Point($pLat, $pLng),")
//                                    }
                                    appendLine(")")
                                }
                                clipboardManager.setText(AnnotatedString(code))
                            },
                            modifier = Modifier.align(Alignment.CenterVertically),
                        ) {
                            Icon(Icons.Filled.ContentCopy, "Copy code")
                        }

                        IconButton(
                            onClick = {
                                currentBuilding.points[selectedPointIndex] =
                                    Pair(lat, lng + step)
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "East")
                        }
                    }

                    // Down (decrease latitude)
                    IconButton(
                        onClick = {
                            currentBuilding.points[selectedPointIndex] = Pair(lat - step, lng)
                        },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, "South")
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Step size slider
                Text(
                    "Step: ~${"%.3f".format(step * 111_000)}m",
                    fontSize = 11.sp,
                )
                Slider(
                    value = stepExponent,
                    onValueChange = { stepExponent = it },
                    valueRange = -8f..-4f,
                    steps = 3, // -8, -7, -6, -5, -4
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
