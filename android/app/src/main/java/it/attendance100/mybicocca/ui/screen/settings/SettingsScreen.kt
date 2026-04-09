package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.settings.SimpleCategorySettingItem

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToGeneral: () -> Unit,
    onNavigateToBehaviour: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToLoginManager: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        SimpleCategorySettingItem(
            title = stringResource(R.string.settings_appearance),
            subtitle = stringResource(R.string.settings_appearance_subtitle),
            icon = Icons.Default.Palette,
            onClick = onNavigateToAppearance,
        )
        SimpleCategorySettingItem(
            title = stringResource(R.string.settings_general),
            subtitle = stringResource(R.string.settings_general_subtitle),
            icon = Icons.Default.Settings,
            onClick = onNavigateToGeneral,
        )
        SimpleCategorySettingItem(
            title = stringResource(R.string.settings_behaviour),
            subtitle = stringResource(R.string.settings_behaviour_subtitle),
            icon = Icons.Default.Psychology,
            onClick = onNavigateToBehaviour,
        )
        SimpleCategorySettingItem(
            title = stringResource(R.string.settings_security),
            subtitle = stringResource(R.string.settings_security_subtitle),
            icon = Icons.Default.Fingerprint,
            onClick = onNavigateToSecurity,
        )
        SimpleCategorySettingItem(
            title = stringResource(R.string.settings_developer),
            subtitle = stringResource(R.string.settings_developer_subtitle),
            icon = Icons.Default.BugReport,
            onClick = onNavigateToDeveloper,
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        SimpleCategorySettingItem(
            title = stringResource(R.string.login_manager),
            subtitle = stringResource(R.string.login_manager),
            icon = Icons.Default.Key,
            onClick = onNavigateToLoginManager,
        )
        SimpleCategorySettingItem(
            title = stringResource(R.string.app_info),
            subtitle = stringResource(R.string.app_info),
            icon = Icons.Default.Info,
            onClick = onNavigateToAppInfo,
        )
    }
}
