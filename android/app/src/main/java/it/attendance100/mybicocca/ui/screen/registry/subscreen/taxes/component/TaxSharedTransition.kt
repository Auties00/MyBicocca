package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import it.attendance100.mybicocca.ui.navigation.LocalAnimatedContentScope
import it.attendance100.mybicocca.ui.navigation.LocalSharedTransitionScope
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxesSharedElement
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxesSharedKey

// Drive the morph with the SAME spatial spring the NavHost slide / app-bar use
// (NavTransitions also reads MotionScheme.defaultSpatialSpec). A fixed tween here ran on a
// different clock than the page transition, so the morph trailed and looked like it snapped
// at the end; sharing the spring makes the two track each other.
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun rememberTaxBoundsTransform(): BoundsTransform {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
    return remember(spatialSpec) { BoundsTransform { _, _ -> spatialSpec } }
}

// sharedBounds (not sharedElement) + scaleToBounds: the list and detail labels are the same
// concept at very different type sizes (e.g. the status grows from ~11sp to 40sp). sharedElement
// only animates the bounding box, so the glyphs stay one size and visibly snap to the target size
// at the end of the flight. scaleToBounds scales the rendered text to the animating bounds, so the
// glyphs grow/shrink smoothly across the morph — the same approach the MyBicocca wordmark uses.

// List side: the registry tab pager is hosted inside the NavDisplay TabRoot entry, so the card
// sits within that entry's AnimatedContent (LocalAnimatedContentScope, bridged from
// LocalNavAnimatedContentScope). That makes this a true shared element that seeks with the
// predictive-back gesture into the matching element in the detail entry — not caller-managed.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.taxSharedListElement(
    taxId: Long,
    element: TaxesSharedElement,
): Modifier {
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalAnimatedContentScope.current ?: return this
    val bounds = rememberTaxBoundsTransform()
    return with(scope) {
        this@taxSharedListElement.sharedBounds(
            sharedContentState = rememberSharedContentState(TaxesSharedKey(taxId, element)),
            animatedVisibilityScope = animatedScope,
            boundsTransform = bounds,
            resizeMode = ResizeMode.scaleToBounds(contentScale = ContentScale.Fit),
        )
    }
}

// Detail side: lives inside the NavDisplay destination's AnimatedContent, so the scope manages it.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.taxSharedDetailElement(
    taxId: Long?,
    element: TaxesSharedElement,
): Modifier {
    if (taxId == null) return this
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalAnimatedContentScope.current ?: return this
    val bounds = rememberTaxBoundsTransform()
    return with(scope) {
        this@taxSharedDetailElement.sharedBounds(
            sharedContentState = rememberSharedContentState(TaxesSharedKey(taxId, element)),
            animatedVisibilityScope = animatedScope,
            boundsTransform = bounds,
            resizeMode = ResizeMode.scaleToBounds(contentScale = ContentScale.Fit),
        )
    }
}
