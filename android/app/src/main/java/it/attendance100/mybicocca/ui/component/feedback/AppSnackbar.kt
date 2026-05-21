package it.attendance100.mybicocca.ui.component.feedback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.BuildConfig
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class AppSnackbarSeverity { Info, Error }

@Immutable
internal data class AppSnackbarVisuals(
    override val message: String,
    val severity: AppSnackbarSeverity,
    val detail: String? = null,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
) : SnackbarVisuals

@Stable
class AppSnackbarController internal constructor() {
    val hostState: SnackbarHostState = SnackbarHostState()

    suspend fun showInfo(message: String) {
        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(
            AppSnackbarVisuals(message = message, severity = AppSnackbarSeverity.Info),
        )
    }

    suspend fun showError(message: String, cause: Throwable? = null) {
        val friendly = cause?.friendlyShortReason()
        val title = if (friendly != null) "$message · $friendly" else message
        val detail = if (BuildConfig.DEBUG) cause?.debugDetail() else null
        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(
            AppSnackbarVisuals(
                message = title,
                severity = AppSnackbarSeverity.Error,
                detail = detail,
            ),
        )
    }
}

@Composable
fun rememberAppSnackbarController(): AppSnackbarController =
    remember { AppSnackbarController() }

val LocalAppSnackbarController = staticCompositionLocalOf<AppSnackbarController> {
    error("AppSnackbarController not installed. Wrap the screen tree with a controller provider.")
}

@Composable
fun AppSnackbarHost(
    controller: AppSnackbarController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    val hostState = controller.hostState
    val current = hostState.currentSnackbarData
    val accessibility = LocalAccessibilityManager.current

    // Replicates SnackbarHost's built-in auto-dismiss using the visuals' duration,
    // since we render the snackbar ourselves (custom layout + slide animation).
    LaunchedEffect(current) {
        if (current != null) {
            val durationMs = current.visuals.duration.toMillisCompat(
                hasAction = current.visuals.actionLabel != null,
                accessibility = accessibility,
            )
            delay(durationMs)
            current.dismiss()
        }
    }

    Box(
        modifier = modifier.padding(contentPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (slideInVertically(initialOffsetY = { it + 32 }) + fadeIn()) togetherWith
                    (slideOutVertically(targetOffsetY = { it + 32 }) + fadeOut())
            },
            label = "app_snackbar_animation",
        ) { data ->
            if (data != null) {
                AppSnackbarSurface(data = data)
            } else {
                // Empty placeholder during exit; AnimatedContent retains the leaving child.
                Box(Modifier.size(0.dp))
            }
        }
    }
}

@Composable
private fun AppSnackbarSurface(data: SnackbarData) {
    val scheme = MaterialTheme.colorScheme
    val visuals = data.visuals as? AppSnackbarVisuals
    val severity = visuals?.severity ?: AppSnackbarSeverity.Info

    val container = when (severity) {
        AppSnackbarSeverity.Info -> scheme.surfaceContainerHigh
        AppSnackbarSeverity.Error -> scheme.errorContainer
    }
    val onContainer = when (severity) {
        AppSnackbarSeverity.Info -> scheme.onSurface
        AppSnackbarSeverity.Error -> scheme.onErrorContainer
    }
    val accent = when (severity) {
        AppSnackbarSeverity.Info -> scheme.primary
        AppSnackbarSeverity.Error -> scheme.error
    }
    val icon = when (severity) {
        AppSnackbarSeverity.Info -> Icons.Outlined.Info
        AppSnackbarSeverity.Error -> Icons.Outlined.ErrorOutline
    }

    Surface(
        modifier = Modifier
            .widthIn(min = 240.dp, max = 560.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = onContainer,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer,
                    fontWeight = FontWeight.Medium,
                )
                val detail = visuals?.detail
                if (!detail.isNullOrBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

private fun SnackbarDuration.toMillisCompat(
    hasAction: Boolean,
    accessibility: androidx.compose.ui.platform.AccessibilityManager?,
): Long {
    val original = when (this) {
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
        SnackbarDuration.Long -> 10_000L
        SnackbarDuration.Short -> 4_000L
    }
    if (accessibility == null) return original
    return accessibility.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = original,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction,
    )
}

// Compact, user-readable category. Always shown in release builds — must not leak
// internals (no class names, no stack traces). In debug, the full debugDetail is
// shown on a second line so engineers can still diagnose.
private fun Throwable.friendlyShortReason(): String? = when (this) {
    is UnknownHostException, is ConnectException -> "rete non disponibile"
    is SocketTimeoutException -> "timeout di rete"
    is IOException -> "errore di rete"
    else -> null
}

// Engineer-facing detail line; emitted only when BuildConfig.DEBUG is true.
private fun Throwable.debugDetail(): String {
    val parts = buildList {
        val name = this@debugDetail::class.simpleName
        if (!name.isNullOrBlank()) add(name)
        message?.takeIf { it.isNotBlank() }?.let { add(it) }
        cause?.let { c ->
            val cn = c::class.simpleName
            val cm = c.message?.takeIf { it.isNotBlank() }
            when {
                cn != null && cm != null -> add("caused by $cn: $cm")
                cn != null -> add("caused by $cn")
                cm != null -> add("caused by: $cm")
            }
        }
    }
    return parts.joinToString(" · ")
}
