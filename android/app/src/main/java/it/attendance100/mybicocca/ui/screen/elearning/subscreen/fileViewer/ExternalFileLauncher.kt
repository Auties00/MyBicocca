package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.route.AppRoute

/**
 * Opens a file in the device's default app, without any in-app viewer page: the file-viewer
 * ViewModel downloads the (tokenized) file on init through the same path the in-app viewers use,
 * and the moment the local copy is ready it is fired at the system via ACTION_VIEW + FileProvider.
 * Backs the "external" half of the in-app/external open choice.
 *
 * The only chrome is a small progress dialog (ignoring outside taps) while the download runs;
 * the launcher dismisses itself once the system app is launched, when no app can open the file,
 * or when the download fails (with an error snackbar).
 */
@Composable
fun ExternalFileLauncher(
    route: AppRoute.FileViewer,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val snackbar = LocalAppSnackbarController.current
    @Suppress("DEPRECATION") val viewModel =
        hiltViewModel<FileViewerViewModel, FileViewerViewModel.Factory>(
            key = route.fileUrl ?: route.localPath ?: route.fileName,
            creationCallback = { it.create(route) },
        )
    val localPath by viewModel.localPath.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()

    val noAppMessage = stringResource(R.string.elearning_file_no_app)
    val downloadFailedMessage = stringResource(R.string.elearning_file_download_failed)

    LaunchedEffect(localPath) {
        val path = (localPath as? Loadable.Loaded)?.value ?: return@LaunchedEffect
        if (!launchExternalViewer(context, path, route.mimeType)) {
            snackbar.showError(noAppMessage)
        }
        onFinished()
    }
    LaunchedEffect(downloadStatus) {
        if (downloadStatus is SyncStatus.Failed) {
            snackbar.showError(downloadFailedMessage)
            onFinished()
        }
    }

    if (localPath is Loadable.NotYetLoaded && downloadStatus !is SyncStatus.Failed) {
        Dialog(
            onDismissRequest = onFinished,
            properties = DialogProperties(dismissOnClickOutside = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.elearning_file_opening_external),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
