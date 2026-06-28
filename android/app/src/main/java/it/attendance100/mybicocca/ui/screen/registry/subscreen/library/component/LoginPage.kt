package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginFeedback
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import kotlinx.coroutines.delay

/**
 * Email-validation login page. The email is pinned to the institutional address (Esse3/Elearning)
 * and shown read-only: enter is implicit, the server sends a link, the user opens it from their
 * inbox, then confirms with "Ho aperto il link". After a verify attempt the form cross-fades to an
 * in-content feedback panel — a warning that reverts to the form, or a success that proceeds —
 * rather than a separate result-page modal.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginPage(
    email: String,
    phase: LibraryLoginPhase,
    feedback: LibraryLoginFeedback?,
    onSendEmail: () -> Unit,
    onVerify: () -> Unit,
    onFeedbackDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = feedback,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(240)) + scaleIn(tween(360), initialScale = 0.9f)) togetherWith
                (fadeOut(tween(160)) + scaleOut(tween(240), targetScale = 0.94f))
        },
        label = "login_feedback",
    ) { current ->
        if (current != null) {
            LoginFeedbackPanel(feedback = current, onDismiss = onFeedbackDismiss)
        } else {
            LoginForm(
                email = email,
                phase = phase,
                onSendEmail = onSendEmail,
                onVerify = onVerify,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginForm(
    email: String,
    phase: LibraryLoginPhase,
    onSendEmail: () -> Unit,
    onVerify: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val awaiting = phase == LibraryLoginPhase.AwaitingClick || phase == LibraryLoginPhase.Verifying
    val busy = phase == LibraryLoginPhase.Sending || phase == LibraryLoginPhase.Verifying

    val brandContainer = if (dark) scheme.primaryContainer else scheme.primary
    val brandContent = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Column(
        modifier = Modifier
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
            val context = LocalContext.current
            Text(
                text = stringResource(R.string.library_login_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (awaiting) {
                    context.getString(R.string.library_link_sent)
                } else {
                    context.getString(R.string.library_will_send_link)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

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

        val haptic = rememberHapticManager()
        Button(
            onClick = {
                haptic.tap()
                if (awaiting) onVerify() else onSendEmail()
            },
            enabled = !busy && email.isNotBlank(),
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(containerColor = brandContainer, contentColor = brandContent),
        ) {
            if (busy) {
                LoadingIndicator(modifier = Modifier.size(32.dp), color = brandContent)
            } else if (awaiting) {
                Icon(Icons.Outlined.TaskAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.library_link_opened),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    stringResource(R.string.library_send_link),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * In-content outcome of a verify tap. A warning auto-reverts to the form; a success plays, then
 * the host pops the login page. Mirrors the booking-confirmation hero (Cookie shape + spring pop).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginFeedbackPanel(
    feedback: LibraryLoginFeedback,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val success = feedback == LibraryLoginFeedback.LoggedIn

    val icon: ImageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.MarkEmailUnread
    val container = if (success) scheme.primaryContainer else scheme.tertiaryContainer
    val onContainer = if (success) scheme.onPrimaryContainer else scheme.onTertiaryContainer
    val title =
        if (success) context.getString(R.string.library_login_success) else context.getString(R.string.library_link_not_opened)
    val body = if (success) {
        context.getString(R.string.library_bookings_synced)
    } else {
        context.getString(R.string.library_open_link_again)
    }

    val pop by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "login_feedback_pop",
    )

    LaunchedEffect(feedback) {
        delay(if (success) 1500 else 2200)
        onDismiss()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(120.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = onContainer, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.size(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
