package it.unimib.indoornav.lab.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.ui.theme.LabTheme

/** "All" plus one chip per building, several at once; an empty [selected] means all. */
@Composable
fun BuildingChips(
    buildings: List<String>,
    selected: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChip(selected = selected.isEmpty(), onClick = { onSelectedChange(emptySet()) }, label = { Text("All") })
        buildings.forEach { building ->
            FilterChip(
                selected = building in selected,
                onClick = { onSelectedChange(if (building in selected) selected - building else selected + building) },
                label = { Text(building) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuildingChipsPreview() {
    LabTheme {
        Surface { BuildingChips(buildings = listOf("Home", "U14"), selected = setOf("U14"), onSelectedChange = {}) }
    }
}
