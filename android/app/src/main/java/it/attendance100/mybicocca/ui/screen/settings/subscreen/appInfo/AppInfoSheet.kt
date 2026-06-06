package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.component.directory.SegmentedIconChip
import it.attendance100.mybicocca.ui.component.directory.SegmentedTile
import it.attendance100.mybicocca.ui.component.directory.segmentedShape
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Year

private const val COPYRIGHT_START_YEAR = 2025
private const val GITHUB_URL = "https://github.com/Auties00/MyBicocca"

private val versionText: String = buildString {
    append("Versione ${BuildConfig.VERSION_NAME}")
    if (BuildConfig.DEBUG) append(" [Debug]")
}

private val copyrightText: String
    get() {
        val current = Year.now().value
        val span =
            if (current > COPYRIGHT_START_YEAR) "$COPYRIGHT_START_YEAR–$current" else "$COPYRIGHT_START_YEAR"
        return "© $span 100% Attendance"
    }

private data class Credit(val name: String, val icon: @Composable (modifier: Modifier) -> Unit)

private val CREDITS = listOf(
    Credit("Alessandro Autiero", { }),
    Credit("Lorenzo Angelo Lupi", { }),
    Credit("Alessandro Ferrari Pagini", { }),
    Credit("Federico Giarrusso", { }),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppInfoSheet(onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val secondary = scheme.onSurfaceVariant
    val haptic = rememberHapticManager()
    val context = LocalContext.current
    val githubIcon = ImageVector.vectorResource(R.drawable.ic_github)
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo MyBicocca",
                    modifier = Modifier.size(168.dp),
                )
                // Pulled up so the wordmark tucks closer under the logo graphic.
                MyBicoccaWordmark(
                    modifier = Modifier.offset(y = (-10).dp),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = versionText, color = secondary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(text = copyrightText, color = secondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SegmentedTile(
                    isFirst = true,
                    isLast = false,
                    title = "Check for Updates",
                    subtitle = "Cerca nuove versioni",
                    onClick = {
                        haptic.tap()
                        // Guard the work, not the tap: stacking taps mustn't launch overlapping checks.
                        if (!isLoading) {
                            isLoading = true
                            scope.launch {
                                delay(2000L) // TODO implement actual update checking logic
                                isLoading = false
                            }
                        }
                    },
                    leading = {
                        SegmentedIconChip(
                            Icons.Outlined.Update,
                            scheme.secondaryContainer,
                            scheme.onSecondaryContainer
                        )
                    },
                    trailing = {
                        Spacer(Modifier.width(6.dp))
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = scheme.primary,
                            )
                        }
                    },
                )
                SegmentedTile(
                    isFirst = false,
                    isLast = false,
                    title = "What's New",
                    subtitle = "Novità dell'ultima versione",
                    onClick = { haptic.tap() },
                    leading = {
                        SegmentedIconChip(
                            Icons.Outlined.NewReleases,
                            scheme.secondaryContainer,
                            scheme.onSecondaryContainer
                        )
                    },
                    trailing = { TrailingGlyph(Icons.Rounded.ChevronRight) },
                )
                SegmentedTile(
                    isFirst = false,
                    isLast = true,
                    title = "GitHub",
                    subtitle = "Codice sorgente del progetto",
                    onClick = {
                        haptic.tap()
                        // In-app browser (Custom Tab) instead of kicking the user to the system browser.
                        CustomTabsIntent.Builder().setShowTitle(true).build()
                            .launchUrl(context, GITHUB_URL.toUri())
                    },
                    leading = {
                        SegmentedIconChip(
                            githubIcon,
                            scheme.secondaryContainer,
                            scheme.onSecondaryContainer
                        )
                    },
                    trailing = { TrailingGlyph(Icons.Rounded.Link) },
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Credits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CREDITS.forEachIndexed { index, credit ->
                    CreditTile(
                        credit = credit,
                        isFirst = index == 0,
                        isLast = index == CREDITS.lastIndex,
                    )
                }
            }
        }
    }
}

// The standard trailing chevron/link glyph for the action tiles.
@Composable
private fun TrailingGlyph(icon: ImageVector) {
    Spacer(Modifier.width(6.dp))
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.size(22.dp),
    )
}

// Compact credit row: a circular avatar placeholder + the member's name. Kept separate from the
// directory tiles because it is intentionally denser (smaller avatar/text, no trailing); it still
// shares the connected-card [segmentedShape].
@Composable
private fun CreditTile(
    credit: Credit,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = segmentedShape(isFirst, isLast),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TEMP placeholder avatar — replace each credit's icon lambda with the member's photo.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                credit.icon(Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = credit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
