package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.ui.graphics.GraphicsLayerScope

/**
 * Material "fade through" handoff for chrome that morphs between two end states, driven by a
 * continuous progress [p] (1 = expanded sub-page/search, 0 = collapsed page). Each side fades
 * over its own half of the range and both reach 0 at the midpoint, so the outgoing element is
 * gone before the incoming one appears — they're never composited on top of each other.
 * Symmetric because predictive back can scrub [p] in either direction. Shared by the top bar
 * and the search overlay so both ride the same ramp.
 */
internal fun fadeThroughExpanded(p: Float): Float = ((p - 0.5f) / 0.5f).coerceIn(0f, 1f)

/** The collapsed-state half of the fade-through ramp described on [fadeThroughExpanded]. */
internal fun fadeThroughCollapsed(p: Float): Float = ((0.5f - p) / 0.5f).coerceIn(0f, 1f)

/**
 * The scale half of a fade-through: an element grows from [minScale] to 1 as it fades in (and
 * shrinks back as it fades out), so the swap reads as a transformation rather than a blink.
 * 0.92 is the Material spec default for text/large surfaces; icons pass a smaller value for a
 * punchier morph since they don't also travel in position.
 */
internal fun GraphicsLayerScope.fadeThroughLayer(alpha: Float, minScale: Float = 0.92f) {
    this.alpha = alpha
    val scale = minScale + (1f - minScale) * alpha
    scaleX = scale
    scaleY = scale
}
