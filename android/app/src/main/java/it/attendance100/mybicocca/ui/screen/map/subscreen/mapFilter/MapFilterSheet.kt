package it.attendance100.mybicocca.ui.screen.map.subscreen.mapFilter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.map.BuildingCategory
import it.attendance100.mybicocca.ui.screen.map.component.icon
import it.attendance100.mybicocca.ui.screen.map.component.labelRes

/**
 * Multi-select category filter for the map pins, opened from the shell app bar's filter
 * affordance: one toggleable chip per [BuildingCategory], plus an "Azzera filtri" action that
 * appears only while at least one category is selected. An empty selection means no filtering.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapFilterSheet(
    selected: Set<BuildingCategory>,
    onToggle: (BuildingCategory) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet(onDismiss = onDismiss) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.map_filter_title),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BuildingCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category in selected,
                        onClick = { onToggle(category) },
                        label = { Text(stringResource(category.labelRes)) },
                        leadingIcon = { Icon(category.icon, contentDescription = null) },
                    )
                }
            }
            if (selected.isNotEmpty()) {
                TextButton(onClick = onClear) { Text(stringResource(R.string.map_filter_clear)) }
            }
        }
    }
}
