package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager

/**
 * What a sheet page shows in the pinned [SheetPager] header. Every modal page has one, with a
 * title and a subtitle. Resolved in composition, so the title/subtitle can read live ViewModel
 * state (e.g. a "3 prenotazioni" counter) and update in place on the same page; [subtitle] is
 * null only while the data it summarises is still loading.
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
    val subtitle: CharSequence?,
    val onSubtitleClick: (() -> Unit)? = null,
    val showBack: Boolean = true,
    val onBack: (() -> Unit)? = null,
)

/**
 * The pinned header of a [SheetPager], moving in lockstep with its page transition.
 *
 * Everything that moves — the back arrow fading and sliding in, the leading inset easing from
 * 24dp to 10dp, the outgoing title sliding toward the back edge while the incoming one slides in,
 * the header height — is computed directly from the transition's two pages and its [progress]
 * (the same fraction a predictive-back gesture seeks), in a single layout pass. Nothing here keeps
 * animation state of its own, so the header can neither lag behind a seek nor jump when the
 * transition settles, and nothing is clipped: the texts are measured at their final widths and
 * only translated and faded.
 *
 * Within one page, live text changes (a counter loading in, a detail title resolving) crossfade
 * in place, growing or shrinking the header smoothly.
 */
@Composable
internal fun <P : Any> SheetPagerHeader(
    transition: Transition<P>,
    specs: Map<Any, SheetHeaderSpec?>,
    keyOf: (P) -> Any,
    depthOf: (P) -> Int,
    progress: () -> Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val current = transition.currentState
    val target = transition.targetState
    val settled = keyOf(current) == keyOf(target)
    val forward = depthOf(target) >= depthOf(current)

    fun spec(page: P): SheetHeaderSpec? = specs[keyOf(page)]
    fun hasBack(page: P): Boolean = depthOf(page) > 0 && spec(page)?.showBack == true

    // The arrow keeps the action of the last page that offered one, so it stays functional while
    // it fades out.
    val lastBack = remember { mutableStateOf<(() -> Unit)?>(null) }
    if (hasBack(target)) lastBack.value = spec(target)?.onBack ?: onBack

    val backFrom = if (hasBack(current)) 1f else 0f
    val backTo = if (hasBack(target)) 1f else 0f
    val currentSpec = spec(current)
    val targetSpec = spec(target)

    Layout(
        contents = listOf(
            {
                IconButton(
                    onClick = {
                        haptic.tap()
                        lastBack.value?.invoke()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            { if (currentSpec != null) key(keyOf(current)) { HeaderTexts(currentSpec) } },
            { if (!settled && targetSpec != null) key(keyOf(target)) { HeaderTexts(targetSpec) } },
        ),
        modifier = modifier.padding(end = 24.dp),
    ) { (backMeasurables, currentMeasurables, targetMeasurables), constraints ->
        // The page pair comes from composition, the progress is read live here. On the frame a
        // transition settles the live state has already moved on (progress resets) while the
        // composition still holds the in-flight pair; rest on the page it settled on instead of
        // re-reading the reset progress as "just started".
        val restingKey = keyOf(transition.targetState)
        val liveSettled = keyOf(transition.currentState) == restingKey
        val restsOnTarget = settled || keyOf(target) == restingKey

        val insetRoot = 24.dp.toPx()
        val insetWithBack = 10.dp.toPx()
        val backSlot = 54.dp.toPx() // 48dp button + 6dp gap: the text lands at 64dp
        fun insetX(presence: Float) = lerp(insetRoot, insetWithBack, presence)
        fun textX(presence: Float) = insetX(presence) + backSlot * presence

        val width = constraints.maxWidth
        val button = backMeasurables.first().measure(Constraints())
        fun measureTexts(measurables: List<Measurable>, presence: Float): Placeable? =
            measurables.firstOrNull()?.measure(
                Constraints(maxWidth = (width - textX(presence).roundToInt()).coerceAtLeast(0)),
            )
        val shown = measureTexts(currentMeasurables, if (settled) backTo else backFrom)
        val incoming = measureTexts(targetMeasurables, backTo)

        // A page whose own nested pager draws its header (null spec) takes no room here.
        val bottomGap = 12.dp.toPx()
        fun heightOf(texts: Placeable?, presence: Float): Float {
            val content = maxOf(texts?.height ?: 0, if (presence > 0f) button.height else 0)
            return if (content == 0) 0f else content + bottomGap
        }

        if (settled || liveSettled || (incoming == null && targetSpec != null)) {
            val texts = if (!settled && restsOnTarget) incoming else shown
            val presence = if (restsOnTarget) backTo else backFrom
            val height = heightOf(texts, presence).roundToInt()
            val body = (height - bottomGap).coerceAtLeast(0f)
            return@Layout layout(width, height) {
                if (presence > 0f) button.place(insetX(presence).roundToInt(), ((body - button.height) / 2).roundToInt())
                texts?.place(textX(presence).roundToInt(), ((body - texts.height) / 2).roundToInt())
            }
        }

        val t = FastOutSlowInEasing.transform(progress().coerceIn(0f, 1f))
        val back = lerp(backFrom, backTo, t)
        val heightFrom = heightOf(shown, backFrom)
        val heightTo = heightOf(incoming, backTo)
        val height = lerp(heightFrom, heightTo, t).roundToInt()
        // Vertical centre of each side within its own (resting) band, so a page growing a header
        // from nothing (or losing it) slides its texts with the band rather than jumping.
        val bodyFrom = (heightFrom - bottomGap).coerceAtLeast(0f)
        val bodyTo = (heightTo - bottomGap).coerceAtLeast(0f)
        val body = lerp(bodyFrom, bodyTo, t)
        layout(width, height) {
            if (back > 0f) {
                button.placeWithLayer(
                    x = insetX(back).roundToInt(),
                    y = ((body - button.height) / 2).roundToInt(),
                ) { alpha = back }
            }
            val x = textX(back)
            val direction = if (forward) 1 else -1
            shown?.placeWithLayer(
                x = (x + direction * shown.width / 6f * t).roundToInt(),
                y = ((body - shown.height) / 2).roundToInt(),
            ) { alpha = 1f - (t * PageMs / OutFadeMs).coerceIn(0f, 1f) }
            incoming?.placeWithLayer(
                x = (x - direction * incoming.width / 6f * (1f - t)).roundToInt(),
                y = ((body - incoming.height) / 2).roundToInt(),
            ) { alpha = ((t * PageMs - InFadeDelayMs) / InFadeMs).coerceIn(0f, 1f) }
        }
    }
}

private const val PageMs = SheetMotion.PAGE_MS.toFloat()
private const val OutFadeMs = 180f
private const val InFadeDelayMs = 40f
private const val InFadeMs = 280f

/** One page's title over its subtitle; live changes on the same page crossfade in place. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeaderTexts(spec: SheetHeaderSpec) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    Column {
        AnimatedContent(
            targetState = spec.title,
            transitionSpec = {
                fadeIn(tween(240, delayMillis = 60))
                    .togetherWith(fadeOut(tween(140)))
                    .using(SizeTransform(clip = false))
            },
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
        val subtitle: AnnotatedString? = when (val sub = spec.subtitle) {
            null -> null
            is AnnotatedString -> sub
            else -> AnnotatedString(sub.toString())
        }
        AnimatedContent(
            targetState = subtitle,
            transitionSpec = {
                fadeIn(tween(240, delayMillis = 60))
                    .togetherWith(fadeOut(tween(140)))
                    .using(SizeTransform(clip = false) { _, _ -> tween(340) })
            },
            contentKey = { it?.text },
            label = "sheet_header_subtitle",
        ) { sub ->
            if (sub != null) {
                val onSubtitleClick = spec.onSubtitleClick
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
