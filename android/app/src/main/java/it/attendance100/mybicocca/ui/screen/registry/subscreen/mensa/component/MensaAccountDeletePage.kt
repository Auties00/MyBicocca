package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaDeletionCheck
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags

/**
 * Asks the operator to delete the account. The check runs first because some accounts cannot be
 * deleted yet; the request itself is completed by a person, who confirms by mail.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaAccountDeletePage(
    check: MensaDeletionCheck,
    busy: Boolean,
    onRequest: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .testTag(MensaTestTags.ACCOUNT_DELETE_PAGE),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                    Text(
                        text = stringResource(R.string.mensa_delete_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.mensa_delete_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            when (check) {
                MensaDeletionCheck.Checking -> Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center).size(40.dp))
                }

                is MensaDeletionCheck.NotAllowed -> Surface(shape = RoundedCornerShape(20.dp), color = scheme.errorContainer) {
                    Text(
                        text = check.reason ?: stringResource(R.string.mensa_delete_not_allowed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                MensaDeletionCheck.Failed -> SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.mensa_delete_check_failed),
                    body = null,
                    action = { RetryButton(onClick = onRetry) },
                )

                MensaDeletionCheck.Allowed -> Unit
            }
        }

        Button(
            onClick = { haptic.tap(); onRequest() },
            enabled = check == MensaDeletionCheck.Allowed && !busy,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
                .heightIn(min = ButtonDefaults.MediumContainerHeight),
            colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError),
        ) {
            if (busy) {
                LoadingIndicator(modifier = Modifier.size(30.dp), color = scheme.onError)
            } else {
                Text(
                    text = stringResource(R.string.mensa_delete_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(name = "Delete account", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 520)
@Composable
private fun AccountDeletePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaAccountDeletePage(
            check = MensaDeletionCheck.Allowed,
            busy = false,
            onRequest = {},
            onRetry = {},
        )
    }
}
