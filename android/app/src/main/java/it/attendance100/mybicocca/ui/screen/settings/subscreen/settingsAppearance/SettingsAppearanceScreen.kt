package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.os.LocalDeviceType
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.data.local.settings.ThemeMode
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.screen.settings.component.preference.SettingsSectionTitle
import it.attendance100.mybicocca.ui.theme.AppTheme
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.isDynamicColorAvailable

private val THEME_MODES = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark)

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "Sistema"
    ThemeMode.Light -> "Chiaro"
    ThemeMode.Dark -> "Scuro"
}

@Composable
fun SettingsAppearanceScreen(
    viewModel: SettingsAppearanceViewModel = hiltViewModel(),
) {
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Previews render in the same light/dark mode the app is currently using
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val palettes = remember {
        AppTheme.entries.filter { it != AppTheme.MaterialYou || isDynamicColorAvailable }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        SettingsSectionTitle("Modalità")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(100)),
        ) {
            SegmentedSwitch(
                options = THEME_MODES,
                selected = themeMode,
                onSelected = viewModel::setThemeMode,
                label = ::themeModeLabel,
            )
        }

        SettingsSectionTitle("Tema")
        palettes.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { palette ->
                    PaletteCell(
                        appTheme = palette,
                        selected = palette == appTheme,
                        dark = dark,
                        onClick = { viewModel.setAppTheme(palette) },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Lone trailing card
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteCell(
    appTheme: AppTheme,
    selected: Boolean,
    dark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()

    Surface(
        selected = selected,
        onClick = {
            haptic.tap()
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val currentDevice = LocalDeviceType.current

            // Render the preview in its own palette
            BicoccaTheme(appTheme = appTheme, dark = dark) {
                AppThemePreviewItem(
                    selected = selected,
                    deviceType = currentDevice,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = appTheme.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
