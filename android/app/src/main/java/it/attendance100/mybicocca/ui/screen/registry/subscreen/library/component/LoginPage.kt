package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase

// Email-validation login. The email is pinned to the institutional address (Esse3/Elearning) and
// not editable: enter is implicit, the server sends a link, the user opens it, then verifies.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginPage(
    email: String,
    phase: LibraryLoginPhase,
    onSendEmail: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val awaiting = phase == LibraryLoginPhase.AwaitingClick || phase == LibraryLoginPhase.Verifying
    val busy = phase == LibraryLoginPhase.Sending || phase == LibraryLoginPhase.Verifying

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (awaiting) Icons.Outlined.MarkEmailRead else Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp),
                )
            }
            Text(
                text = "Accedi alle tue prenotazioni",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (awaiting) {
                    "Ti abbiamo inviato un link. Aprilo dalla tua casella, poi torna qui e tocca \"Ho aperto il link\"."
                } else {
                    "Ti invieremo un link di accesso alla tua email istituzionale per sincronizzare le tue prenotazioni."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // The address is fixed to the institutional email — shown, not editable.
            Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Outlined.AlternateEmail, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(
                        text = email.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.size(4.dp))
        }

        val brandContainer = if (dark) scheme.primaryContainer else scheme.primary
        val brandContent = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

        if (awaiting) {
            // Connected pair: resend (tonal) + verify (wide, brand) — primary trails.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilledTonalButton(
                    onClick = onSendEmail,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Text("Invia di nuovo", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onVerify,
                    enabled = !busy && email.isNotBlank(),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = brandContainer, contentColor = brandContent),
                ) {
                    if (phase == LibraryLoginPhase.Verifying) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = brandContent)
                    } else {
                        Text("Ho aperto il link", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            Button(
                onClick = onSendEmail,
                enabled = !busy && email.isNotBlank(),
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ButtonDefaults.MediumContainerHeight)
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                contentPadding = ButtonDefaults.MediumContentPadding,
                colors = ButtonDefaults.buttonColors(containerColor = brandContainer, contentColor = brandContent),
            ) {
                if (phase == LibraryLoginPhase.Sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = brandContent)
                } else {
                    Text("Invia link di accesso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
