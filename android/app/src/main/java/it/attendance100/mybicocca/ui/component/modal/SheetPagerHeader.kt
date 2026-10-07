package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
 * What a sheet page shows in the pinned [SheetPager] header. Resolved in composition, so the
 * title/subtitle can read live ViewModel state (e.g. a "3 prenotazioni" counter) and update in
 * place on the same page.
 *
 * @param showBack whether this page offers the header back arrow. Pages at depth 0 never do;
 * deeper pages hide it when back is not a meaningful action (e.g. an in-flight submission or a
 * terminal result page).
 * @param onBack overrides the pager's own back step for the arrow, for pages whose back must be
 * routed through a nested handler first (e.g. a wizard that asks before leaving).
 */
@Immutable
data class SheetHeaderSpec(
    val title: String,
    val subtitle: CharSequence? = null,
    val onSubtitleClick: (() -> Unit)? = null,
    val showBack: Boolean = true,
    val onBack: (() -> Unit)? = null,
)

/**
 * The pinned header of a [SheetPager], driven by the SAME [transition] as the page body. Because
 * every part of the morph — the back arrow sliding in, the leading inset easing from 24dp to
 * 10dp, the outgoing title sliding toward the back edge while the incoming one slides in — is a
 * child animation of that transition, a predictive-back gesture that seeks the body also seeks
 * the header frame for frame, and a cancelled gesture rewinds both together (issue #9). The
 * header previously animated on its own clocks from the settled page, so it only started moving
 * once the gesture committed.
 *
 * Title and subtitle of one page are keyed by the page, so live text changes on the same page
 * (a counter loading in) crossfade in place with a height-aware animation instead of replaying
 * the directional slide. A page with nothing to show (no title, no subtitle, no back arrow — a
 * terminal page that renders its own centered message) collapses the header entirely.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun <P : Any> SheetPagerHeader(
    transition: Transition<P>,
    specs: Map<Any, SheetHeaderSpec?>,
    keyOf: (P) -> Any,
    depthOf: (P) -> Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme

    fun spec(page: P): SheetHeaderSpec? = specs[keyOf(page)]
    fun hasBack(page: P): Boolean = depthOf(page) > 0 && spec(page)?.showBack == true
    fun isBlank(page: P): Boolean {
        val s = spec(page) ?: return true
        return s.title.isBlank() && s.subtitle.isNullOrBlank() && !hasBack(page)
    }

    // The arrow keeps the action of the last page that offered one, so it stays functional while
    // it slides out.
    val lastBack = remember { mutableStateOf<(() -> Unit)?>(null) }
    val target = transition.targetState
    if (hasBack(target)) lastBack.value = spec(target)?.onBack ?: onBack

    val startPadding by transition.animateDp(
        transitionSpec = { tween(SheetMotion.PAGE_MS) },
        label = "header_start_padding",
    ) { page -> if (hasBack(page)) 10.dp else 24.dp }

    transition.AnimatedVisibility(
        visible = { !isBlank(it) },
        enter = expandVertically(tween(SheetMotion.PAGE_MS)) + fadeIn(tween(280, delayMillis = 40)),
        exit = shrinkVertically(tween(300)) + fadeOut(tween(180)),
    ) {
        Row(
            modifier = modifier
                .padding(end = 24.dp, bottom = 12.dp)
                .padding(start = startPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            transition.AnimatedVisibility(
                visible = { hasBack(it) },
                enter = expandHorizontally(tween(SheetMotion.PAGE_MS)) + fadeIn(tween(280, delayMillis = 40)),
                exit = shrinkHorizontally(tween(SheetMotion.PAGE_MS)) + fadeOut(tween(180)),
            ) {
                IconButton(
                    onClick = {
                        haptic.tap()
                        lastBack.value?.invoke()
                    },
                    // The button/text gap lives inside the animated block rather than as row
                    // spacing: between-children spacing would outlive the exiting node and snap
                    // the text left by the gap on the exit's last frame.
                    modifier = Modifier.padding(end = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            }
            transition.AnimatedContent(
                transitionSpec = {
                    val forward = depthOf(targetState) >= depthOf(initialState)
                    (fadeIn(tween(280, delayMillis = 40)) + slideInHorizontally(tween(SheetMotion.PAGE_MS)) { if (forward) -it / 6 else it / 6 })
                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(SheetMotion.PAGE_MS)) { if (forward) it / 6 else -it / 6 })
                        .using(SizeTransform(clip = false) { _, _ -> tween(SheetMotion.PAGE_MS) })
                },
                contentKey = keyOf,
            ) { page ->
                val pageSpec = spec(page)
                Column {
                    AnimatedContent(
                        targetState = pageSpec?.title.orEmpty(),
                        transitionSpec = { fadeIn(tween(240, delayMillis = 60)) togetherWith fadeOut(tween(140)) },
                        label = "sheet_header_title",
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val subtitle: AnnotatedString? = when (val sub = pageSpec?.subtitle) {
                        null -> null
                        is AnnotatedString -> sub
                        else -> AnnotatedString(sub.toString())
                    }
                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = {
                            fadeIn(tween(240, delayMillis = 60))
                                .togetherWith(fadeOut(tween(140)))
                                .using(SizeTransform(clip = true) { _, _ -> tween(340) })
                        },
                        contentKey = { it?.text },
                        label = "sheet_header_subtitle",
                    ) { sub ->
                        if (sub != null) {
                            val onSubtitleClick = pageSpec?.onSubtitleClick
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
}

/**
 * Resolves the header spec of every page the [transition] is currently showing (the settled page,
 * plus the incoming/outgoing one mid-transition) so the header can render both sides of a seek.
 */
@Composable
internal fun <P : Any> rememberHeaderSpecs(
    transition: Transition<P>,
    keyOf: (P) -> Any,
    header: @Composable (P) -> SheetHeaderSpec?,
): Map<Any, SheetHeaderSpec?> {
    val pages = listOf(transition.currentState, transition.targetState).distinctBy(keyOf)
    return pages.associate { page ->
        val pageKey = keyOf(page)
        pageKey to key(pageKey) { header(page) }
    }
}

/** Shared timings of the in-sheet page morph (header and body move as one). */
internal object SheetMotion {
    const val PAGE_MS: Int = 350
}
