package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.DeviceType
import it.attendance100.mybicocca.core.os.LocalDeviceType
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.lock.BiometricCapability
import it.attendance100.mybicocca.ui.screen.lock.errorMessageRes
import it.attendance100.mybicocca.ui.screen.lock.findFragmentActivity
import it.attendance100.mybicocca.ui.screen.lock.promptBiometric
import it.attendance100.mybicocca.ui.screen.lock.rememberBiometricCapability
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity.component.UnlockPreview
import kotlin.math.roundToInt

private val TIMEOUT_STEPS = listOf(0, 1, 5, 10, 15, 30, 60, 240)

/**
 * The "Sicurezza" settings page, shown as a modal bottom sheet. The app-lock master toggle is a
 * pair of side-by-side radio cells, each playing a looping [UnlockPreview] of what that state
 * looks like (a mock phone launching the app into the lock gate vs straight into the calendar).
 * Flipping the toggle requires proving identity first: a biometric prompt where available, with
 * a password [AlertDialog] as fallback (and as the prompt's "Usa password" escape); only a
 * successful authorization commits the change. While the lock is on, an inactivity-timeout
 * slider and the "Schermo privato" switch (hide previews / block screenshots) reveal themselves
 * beneath the cells. The crash-reporting toggle (Crashlytics opt-out) sits below regardless of
 * the lock state. A caption explains when biometrics are unenrolled or unavailable and the
 * password will be used.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSecuritySheet(
    onDismiss: () -> Unit,
    viewModel: SettingsSecurityViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val capability = rememberBiometricCapability()

    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val timeoutMinutes by viewModel.timeoutMinutes.collectAsStateWithLifecycle()
    val secureScreen by viewModel.secureScreen.collectAsStateWithLifecycle()
    val crashReporting by viewModel.crashReporting.collectAsStateWithLifecycle()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingTarget by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authorizing by remember { mutableStateOf(false) }

    val confirmIdentityStr = stringResource(R.string.settings_security_confirm_identity)
    val enableLockStr = stringResource(R.string.settings_security_enable_lock)
    val disableLockStr = stringResource(R.string.settings_security_disable_lock)
    val usePasswordStr = stringResource(R.string.settings_security_use_password)

    val startToggle: () -> Unit = {
        val target = !enabled
        if (capability == BiometricCapability.Available && activity != null) {
            promptBiometric(
                activity = activity,
                title = confirmIdentityStr,
                subtitle = if (target) enableLockStr else disableLockStr,
                negativeButton = usePasswordStr,
                onSuccess = { viewModel.setEnabled(target) },
                onError = { _, _ ->
                    pendingTarget = target
                    password = ""
                    passwordError = null
                    showPasswordDialog = true
                },
            )
        } else {
            pendingTarget = target
            password = ""
            passwordError = null
            showPasswordDialog = true
        }
    }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(R.string.settings_security_sheet_title),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.settings_security_sheet_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val device = LocalDeviceType.current

                FingerprintModeCell(
                    targetEnabled = false,
                    selected = !enabled,
                    deviceType = device,
                    onClick = { if (enabled) startToggle() },
                    modifier = Modifier.weight(1f),
                )
                FingerprintModeCell(
                    targetEnabled = true,
                    selected = enabled,
                    deviceType = device,
                    onClick = { if (!enabled) startToggle() },
                    modifier = Modifier.weight(1f),
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(20.dp))
                    TimeoutSlider(
                        timeoutMinutes = timeoutMinutes,
                        onTimeoutChange = { viewModel.setTimeout(it) },
                    )
                    Spacer(Modifier.height(24.dp))
                    SettingToggleRow(
                        title = stringResource(R.string.settings_security_private_screen_title),
                        subtitle = stringResource(R.string.settings_security_private_screen_subtitle),
                        checked = secureScreen,
                        onCheckedChange = { viewModel.setSecureScreen(it) },
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                SettingToggleRow(
                    title = stringResource(R.string.settings_security_crash_reporting_title),
                    subtitle = stringResource(R.string.settings_security_crash_reporting_subtitle),
                    checked = crashReporting,
                    onCheckedChange = { viewModel.setCrashReporting(it) },
                )
                Spacer(Modifier.height(4.dp))
            }

            if (capability == BiometricCapability.NoneEnrolled) {
                Text(
                    text = stringResource(R.string.settings_security_biometric_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            } else if (capability == BiometricCapability.Unavailable) {
                Text(
                    text = stringResource(R.string.settings_security_biometric_not_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (showPasswordDialog) {
        val authorize: () -> Unit = {
            if (!authorizing && password.isNotEmpty()) {
                authorizing = true
                passwordError = null
                viewModel.verifyPassword(password) { result ->
                    authorizing = false
                    if (result == UnlockResult.Success) {
                        viewModel.setEnabled(pendingTarget)
                        showPasswordDialog = false
                        password = ""
                    } else {
                        passwordError = result.errorMessageRes()?.let(context::getString)
                    }
                }
            }
        }
        AlertDialog(
            onDismissRequest = { if (!authorizing) showPasswordDialog = false },
            title = {
                Text(
                    if (pendingTarget) stringResource(R.string.settings_security_enable_lock_dialog) else stringResource(
                        R.string.settings_security_disable_lock_dialog
                    )
                )
            },
            text = {
                Column {
                    Text(stringResource(R.string.settings_security_password_confirm_prompt))
                    Spacer(Modifier.height(12.dp))
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        enabled = !authorizing,
                        errorText = passwordError,
                        onImeAction = authorize,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = authorize,
                    enabled = !authorizing && password.isNotEmpty(),
                ) { Text(stringResource(R.string.common_confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !authorizing,
                ) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

/**
 * The "Blocca quando inattivo" control: a discrete slider over [TIMEOUT_STEPS] with the active
 * step's label trailing the title. Haptics tick once per discrete step crossing, not on every
 * drag frame — [Slider] emits onValueChange continuously while dragging, which would be a
 * haptic storm. The persisted value commits when the drag settles.
 */
@Composable
private fun TimeoutSlider(
    timeoutMinutes: Int,
    onTimeoutChange: (Int) -> Unit,
) {
    val haptic = rememberHapticManager()

    var sliderPos by remember(timeoutMinutes) {
        mutableFloatStateOf(TIMEOUT_STEPS.indexOf(timeoutMinutes).coerceAtLeast(0).toFloat())
    }
    val index = sliderPos.roundToInt().coerceIn(0, TIMEOUT_STEPS.lastIndex)
    val colors = SliderDefaults.colors(
        activeTickColor = MaterialTheme.colorScheme.onSurface,
        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        inactiveTickColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
    )

    val timeoutImmediately = stringResource(R.string.settings_security_timeout_immediately)
    val timeout1Hour = stringResource(R.string.settings_security_timeout_1hour)
    val timeout4Hours = stringResource(R.string.settings_security_timeout_4hours)
    val timeoutLabelFunc: (Int) -> String = { minutes ->
        when (minutes) {
            0 -> timeoutImmediately
            60 -> timeout1Hour
            240 -> timeout4Hours
            else -> "$minutes min"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_security_lock_inactive_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = timeoutLabelFunc(TIMEOUT_STEPS[index]),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = sliderPos,
            onValueChange = { newPos ->
                if (newPos.roundToInt()
                        .coerceIn(0, TIMEOUT_STEPS.lastIndex) != index
                ) haptic.feather()
                sliderPos = newPos
            },
            onValueChangeFinished = { onTimeoutChange(TIMEOUT_STEPS[index]) },
            valueRange = 0f..TIMEOUT_STEPS.lastIndex.toFloat(),
            steps = TIMEOUT_STEPS.size - 2,
            colors = colors,
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                    drawStopIndicator = null,
                )
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptic = rememberHapticManager()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = { b ->
            onCheckedChange(b)
            haptic.tap()
        })
    }
}

/**
 * One of the two lock-state picks: the looping [UnlockPreview] for [targetEnabled] above a
 * radio button and its label. Tapping the cell only acts when it is not already the active
 * state, kicking off the authorization flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FingerprintModeCell(
    targetEnabled: Boolean,
    selected: Boolean,
    deviceType: DeviceType,
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
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UnlockPreview(
                enabled = targetEnabled,
                deviceType = deviceType,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = if (targetEnabled) stringResource(R.string.settings_security_lock_active) else stringResource(
                        R.string.settings_security_lock_inactive
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
