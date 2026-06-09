package it.attendance100.mybicocca.ui.component.modal

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Pinned header for in-sheet pagers: it never leaves the composition when the page below
// changes. The back button slides in (pushing the text right), while the old title slides
// toward the right and crossfades into the new one; everything mirrors when going back.
//
// Title and subtitle animate INDEPENDENTLY. The title carries the directional page morph; its
// height is constant (one line) so it only slides + crossfades. The subtitle has its OWN
// height-aware crossfade, so it can appear, disappear, or update live on the SAME page (e.g. a
// selection counter that loads in a moment later) by growing / shrinking the sheet smoothly —
// the ModalBottomSheet re-measures to the animating height each frame — instead of snapping the
// header taller. The subtitle accepts an AnnotatedString too, for inline colored status text.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SheetPagerHeader(
    depth: Int,
    title: String,
    subtitle: CharSequence?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onSubtitleClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    // Keep the last back action alive so the button stays functional while sliding out.
    val lastBack = remember { mutableStateOf(onBack) }
    if (onBack != null) lastBack.value = onBack

    val normalizedSubtitle: AnnotatedString? = when (subtitle) {
        null -> null
        is AnnotatedString -> subtitle
        else -> AnnotatedString(subtitle.toString())
    }

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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = onBack != null,
            enter = expandHorizontally(tween(350)) + fadeIn(tween(280, delayMillis = 40)),
            exit = shrinkHorizontally(tween(350)) + fadeOut(tween(180)),
        ) {
            // The 6dp button/text gap lives INSIDE the animated block (not a Row spacedBy):
            // a between-children spacing would survive until the node is removed at the end
            // of the exit animation, snapping the text left by the gap on the last frame.
            IconButton(
                onClick = { lastBack.value?.invoke() },
                modifier = Modifier.padding(end = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Indietro",
                )
            }
        }
        Column {
            // Title carries the directional page morph. Keyed on depth + title: a live subtitle
            // update on the same page must NOT replay this slide.
            AnimatedContent(
                targetState = HeaderText(depth = depth, title = title),
                transitionSpec = {
                    val forward = targetState.depth >= initialState.depth
                    (fadeIn(tween(280, delayMillis = 40)) + slideInHorizontally(tween(350)) { if (forward) -it / 6 else it / 6 })
                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(350)) { if (forward) it / 6 else -it / 6 })
                        .using(SizeTransform(clip = false) { _, _ -> tween(350) })
                },
                contentKey = { it.depth to it.title },
                label = "sheet_header_title",
            ) { text ->
                Text(
                    text = text.title,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // Subtitle animates its OWN height so appearing / disappearing / changing grows or
            // shrinks the sheet smoothly instead of snapping. Keyed on depth + text so it both
            // updates in place on the same page and re-animates across a page change.
            AnimatedContent(
                targetState = depth to normalizedSubtitle,
                transitionSpec = {
                    fadeIn(tween(240, delayMillis = 60))
                        .togetherWith(fadeOut(tween(140)))
                        .using(SizeTransform(clip = true) { _, _ -> tween(340) })
                },
                contentKey = { (d, sub) -> d to sub?.text },
                label = "sheet_header_subtitle",
            ) { (_, sub) ->
                if (sub != null) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = onSubtitleClick
                            ?.let { Modifier.clickable(onClick = it) }
                            ?: Modifier,
                    )
                } else {
                    Spacer(Modifier.height(0.dp))
                }
            }
        }
    }
}

private data class HeaderText(val depth: Int, val title: String)
