package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * Pinned header for in-sheet pagers: it never leaves the composition when the page below
 * changes. The back button slides in (pushing the text right), while the old title slides
 * toward the right and crossfades into the new one; everything mirrors when going back. The
 * leading inset animates from 24dp at root depth to 10dp with the back button (48dp button +
 * 6dp gap landing the text at 64dp), so the title visibly slides right as the button enters.
 * The button/text gap lives inside the button's animated block rather than as row spacing:
 * between-children spacing would outlive the exiting node and snap the text left by the gap on
 * the exit's last frame.
 *
 * Title and subtitle animate independently. The title carries the directional page morph; its
 * height is constant (one line) so it only slides and crossfades, and it is keyed on depth plus
 * text so a live subtitle update on the same page never replays the slide. The subtitle has its
 * own height-aware crossfade, so it can appear, disappear, or update live on the same page
 * (e.g. a selection counter that loads in a moment later) by growing or shrinking the sheet
 * smoothly — the sheet re-measures to the animating height each frame — instead of snapping the
 * header taller. The subtitle accepts an AnnotatedString too, for inline colored status text,
 * and becomes tappable when [onSubtitleClick] is provided.
 *
 * On a page with nothing to show (blank title, no subtitle, no back — i.e. a terminal
 * result/outcome page that renders its own centered message) the whole header collapses, so the
 * result content isn't pushed down by an empty header band.
 *
 * @param depth position in the pager's back stack; page changes animate forward when it grows
 * and mirror when it shrinks.
 * @param onBack null at root depth hides the back button; the last non-null action is retained
 * so the button stays functional while sliding out.
 */
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
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme

    val lastBack = remember { mutableStateOf(onBack) }
    if (onBack != null) lastBack.value = onBack

    val normalizedSubtitle: AnnotatedString? = when (subtitle) {
        null -> null
        is AnnotatedString -> subtitle
        else -> AnnotatedString(subtitle.toString())
    }

    val startPadding by animateDpAsState(
        targetValue = if (onBack != null) 10.dp else 24.dp,
        animationSpec = tween(350),
        label = "header_start_padding",
    )

    val blank = title.isBlank() && subtitle.isNullOrBlank() && onBack == null

    AnimatedVisibility(
        visible = !blank,
        enter = expandVertically(tween(350)) + fadeIn(tween(280, delayMillis = 40)),
        exit = shrinkVertically(tween(300)) + fadeOut(tween(180)),
    ) {
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
                IconButton(
                    onClick = {
                        haptic.tap()
                        lastBack.value?.invoke()
                    },
                    modifier = Modifier.padding(end = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            }
            Column {
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
                                ?.let { Modifier.clickable(onClick = { haptic.tap(); it() }) }
                                ?: Modifier,
                        )
                    } else {
                        Spacer(Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

private data class HeaderText(val depth: Int, val title: String)
