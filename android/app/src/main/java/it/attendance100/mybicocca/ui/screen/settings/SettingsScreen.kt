package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.settings.component.preference.StandardSettingTile

@Composable
fun SettingsScreen(
    onOpenAppearance: () -> Unit = {},
    onOpenGeneral: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenAppInfo: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        StandardSettingTile(
            title = "Aspetto",
            subtitle = "Tema e colori dell'app",
            iconAsset = Icons.Outlined.Palette,
            onItemClick = onOpenAppearance,
        )
        StandardSettingTile(
            title = "Generale",
            subtitle = "Lingua dell'app",
            iconAsset = Icons.Outlined.Translate,
            onItemClick = onOpenGeneral,
        )
        StandardSettingTile(
            title = "Sicurezza",
            subtitle = "Blocco app, sblocco con impronta o volto",
            iconAsset = Icons.Outlined.Lock,
            onItemClick = onOpenSecurity,
        )
        StandardSettingTile(
            title = "Info App",
            subtitle = "Versione e dettagli build",
            iconAsset = Icons.Outlined.Info,
            onItemClick = onOpenAppInfo,
        )
    }
}
