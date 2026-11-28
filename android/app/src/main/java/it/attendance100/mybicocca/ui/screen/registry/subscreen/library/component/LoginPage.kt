package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import android.content.res.Configuration
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreview
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryPreviewDefaults
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.LibraryTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginFeedback
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryLoginPhase
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import kotlinx.coroutines.delay

/**
 * Email-validation login page. The email is pinned to the institutional address (Esse3/Elearning)
 * and shown read-only: the server sends a link, the user opens it from their inbox, and either
 * returns to the app (triggering [onResume] silent validation on `ON_RESUME`) or confirms with
 * "Ho aperto il link".
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginPage(
    email: String,
    phase: LibraryLoginPhase,
    feedback: LibraryLoginFeedback?,
    onSendEmail: () -> Unit,
    onResendEmail: () -> Unit,
    onVerify: () -> Unit,
    onFeedbackDismiss: () -> Unit,
    onResume: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnResume by rememberUpdatedState(onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) latestOnResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AnimatedContent(
        targetState = feedback,
        modifier = modifier.testTag(LibraryTestTags.LOGIN_PAGE),
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
                onResendEmail = onResendEmail,
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
    onResendEmail: () -> Unit,
    onVerify: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
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
            Image(
                painter = painterResource(R.drawable.library_art_verify_mail),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 160.dp, height = 120.dp),
            )
            Text(
                text = stringResource(R.string.library_login_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (awaiting) {
                    stringResource(R.string.library_link_sent)
                } else {
                    stringResource(R.string.library_will_send_link)
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.size(4.dp))
        }

        val haptic = rememberHapticManager()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = if (awaiting) 12.dp else 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(
                onClick = {
                    haptic.tap()
                    if (awaiting) onVerify() else onSendEmail()
                },
                enabled = !busy && email.isNotBlank(),
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
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
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Text(
                        stringResource(R.string.library_send_link),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (awaiting) {
                TextButton(
                    onClick = {
                        haptic.tap()
                        onResendEmail()
                    },
                    enabled = !busy && email.isNotBlank(),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag(LibraryTestTags.LOGIN_RESEND),
                ) {
                    Text(
                        text = stringResource(R.string.library_resend_link),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * In-content outcome of a verify tap. A warning auto-reverts to the form; a success plays, then
 * the host pops the login page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginFeedbackPanel(
    feedback: LibraryLoginFeedback,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val success = feedback == LibraryLoginFeedback.LoggedIn

    val icon: ImageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.MarkEmailUnread
    val container = if (success) scheme.primaryContainer else scheme.tertiaryContainer
    val onContainer = if (success) scheme.onPrimaryContainer else scheme.onTertiaryContainer
    val title =
        if (success) stringResource(R.string.library_login_success) else stringResource(R.string.library_link_not_opened)
    val body = if (success) {
        stringResource(R.string.library_bookings_synced)
    } else {
        stringResource(R.string.library_open_link_again)
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

@Preview(name = "Login EnterEmail · Light", showBackground = true)
@Composable
private fun LoginEnterEmailLightPreview() {
    LibraryPreview(dark = false) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.EnterEmail,
            feedback = null,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}

@Preview(
    name = "Login EnterEmail · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LoginEnterEmailDarkPreview() {
    LibraryPreview(dark = true) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.EnterEmail,
            feedback = null,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}

@Preview(name = "Login AwaitingClick · Light", showBackground = true)
@Composable
private fun LoginAwaitingClickLightPreview() {
    LibraryPreview(dark = false) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.AwaitingClick,
            feedback = null,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}

@Preview(
    name = "Login AwaitingClick · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LoginAwaitingClickDarkPreview() {
    LibraryPreview(dark = true) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.AwaitingClick,
            feedback = null,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}

@Preview(name = "Login Success · Light", showBackground = true)
@Composable
private fun LoginSuccessLightPreview() {
    LibraryPreview(dark = false) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.AwaitingClick,
            feedback = LibraryLoginFeedback.LoggedIn,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}

@Preview(
    name = "Login LinkNotOpened · Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = PreviewBgDark,
)
@Composable
private fun LoginNotOpenedDarkPreview() {
    LibraryPreview(dark = true) {
        LoginPage(
            email = LibraryPreviewDefaults.Email,
            phase = LibraryLoginPhase.AwaitingClick,
            feedback = LibraryLoginFeedback.LinkNotOpened,
            onSendEmail = {},
            onResendEmail = {},
            onVerify = {},
            onFeedbackDismiss = {},
        )
    }
}
