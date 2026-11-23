package it.unimib.indoornav.lab.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.unimib.indoornav.lab.ui.theme.LabTheme

/** Filters beacons by text; the ".*" chip switches to a regular expression. */
@Composable
fun BeaconFilterField(
    query: String,
    onQueryChange: (String) -> Unit,
    regex: Boolean,
    onRegexChange: (Boolean) -> Unit,
    invalid: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        isError = invalid,
        placeholder = { Text(if (regex) "Regex, e.g. ^B(1|2)$" else "Filter: label, code, place, ID") },
        supportingText = if (invalid) ({ Text("Not a valid regular expression") }) else null,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = regex,
                    onClick = { onRegexChange(!regex) },
                    label = { Text(".*", fontFamily = FontFamily.Monospace) },
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear filter")
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun BeaconFilterFieldPreview() {
    LabTheme {
        Surface {
            BeaconFilterField(query = "B1", onQueryChange = {}, regex = false, onRegexChange = {}, invalid = false)
        }
    }
}
