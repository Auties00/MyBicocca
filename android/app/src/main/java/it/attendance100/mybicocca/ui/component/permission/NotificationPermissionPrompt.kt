package it.attendance100.mybicocca.ui.component.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R

/**
 * Asks for `POST_NOTIFICATIONS` once, behind a rationale.
 *
 * The system dialog on its own says nothing about *why*, and this app's notifications all concern
 * things that happen while it is closed, so a cold prompt is the one most likely to be dismissed
 * out of hand. Explaining first costs a tap and is the difference between the feature working and
 * it being permanently off.
 *
 * Declining is not a dead end: nothing here gates work on the permission, and the user can turn
 * notifications on later from system settings.
 */
@Composable
fun NotificationPermissionPrompt(
    viewModel: NotificationPermissionViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null
    ),
) {
    // Nothing to ask for below Android 13 — the permission doesn't exist there.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val shouldAsk by viewModel.shouldAsk.collectAsStateWithLifecycle()

    // Closes the dialog on the same frame as the tap, rather than waiting for the DataStore write
    // to come back around through shouldAsk.
    var handled by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Granted or not, the outcome is read live via NotificationPermissions when posting. */ }

    if (!shouldAsk || handled) return

    AlertDialog(
        onDismissRequest = {
            handled = true
            viewModel.markAsked()
        },
        title = { Text(stringResource(R.string.notification_permission_title)) },
        text = { Text(stringResource(R.string.notification_permission_message)) },
        confirmButton = {
            TextButton(onClick = {
                handled = true
                viewModel.markAsked()
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }) {
                Text(stringResource(R.string.notification_permission_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                handled = true
                viewModel.markAsked()
            }) {
                Text(stringResource(R.string.notification_permission_dismiss))
            }
        },
    )
}
