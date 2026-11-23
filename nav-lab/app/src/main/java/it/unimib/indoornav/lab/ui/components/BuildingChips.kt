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

/** "All" plus one chip per building; [selected] null means all. */
@Composable
fun BuildingChips(
    buildings: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        buildings.forEach { building ->
            FilterChip(
                selected = selected == building,
                onClick = { onSelect(if (selected == building) null else building) },
                label = { Text(building) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuildingChipsPreview() {
    LabTheme {
        Surface { BuildingChips(buildings = listOf("Home", "U14"), selected = "U14", onSelect = {}) }
    }
}
