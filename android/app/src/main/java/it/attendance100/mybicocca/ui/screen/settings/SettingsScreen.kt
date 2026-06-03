package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.settings.component.SettingsClickableRow

@Composable
fun SettingsScreen(
    onOpenSecurity: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        SettingsClickableRow(
            title = "Sicurezza",
            subtitle = "Blocco app, sblocco con impronta o volto",
            onClick = onOpenSecurity,
        )
    }
}
