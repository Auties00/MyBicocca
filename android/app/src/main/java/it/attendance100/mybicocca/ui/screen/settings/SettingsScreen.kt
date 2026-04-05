package it.attendance100.mybicocca.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.settings.SimpleCategorySettingItem

@OptIn(ExperimentalMaterial3Api::class)
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
    val textColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(60.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings),
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
}
