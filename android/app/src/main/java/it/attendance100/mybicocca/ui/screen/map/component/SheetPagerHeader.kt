package it.attendance100.mybicocca.ui.screen.map.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Pinned header for in-sheet pagers: it never leaves the composition when the page below
// changes. The back button slides in (pushing the text right), while the old title/subtitle
// slide toward the right and crossfade into the new ones; everything mirrors when going back.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetPagerHeader(
    depth: Int,
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onSubtitleClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    // Keep the last back action alive so the button stays functional while sliding out.
    val lastBack = remember { mutableStateOf(onBack) }
    if (onBack != null) lastBack.value = onBack

    // 24dp leading at root depth; with the back button (48dp + 6dp gap at 10dp) the text
    // lands at 64dp, so the title visibly slides right as the button enters.
    val startPadding by animateDpAsState(
        targetValue = if (onBack != null) 10.dp else 24.dp,
        animationSpec = tween(350),
        label = "header_start_padding",
    )

    Row(
        modifier = modifier
            .padding(end = 24.dp, bottom = 12.dp)
            .padding(start = startPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = onBack != null,
            enter = expandHorizontally(tween(350)) + fadeIn(tween(280, delayMillis = 40)),
            exit = shrinkHorizontally(tween(350)) + fadeOut(tween(180)),
        ) {
            IconButton(onClick = { lastBack.value?.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Indietro",
                )
            }
        }
        AnimatedContent(
            targetState = HeaderText(depth, title, subtitle),
            transitionSpec = {
                val forward = targetState.depth >= initialState.depth
                (fadeIn(tween(280, delayMillis = 40)) + slideInHorizontally(tween(350)) { if (forward) -it / 6 else it / 6 })
                    .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(350)) { if (forward) it / 6 else -it / 6 })
                    .using(SizeTransform(clip = false) { _, _ -> tween(350) })
            },
            contentKey = { it.title to it.subtitle },
            label = "sheet_header_text",
        ) { text ->
            Column {
                Text(
                    text = text.title,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                text.subtitle?.let { subtitleText ->
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = onSubtitleClick
                            ?.let { Modifier.clickable(onClick = it) }
                            ?: Modifier,
                    )
                }
            }
        }
    }
}

private data class HeaderText(val depth: Int, val title: String, val subtitle: String?)
