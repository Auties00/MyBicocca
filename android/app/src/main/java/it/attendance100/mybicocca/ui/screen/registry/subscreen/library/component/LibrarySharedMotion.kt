package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import it.attendance100.mybicocca.ui.component.modal.SheetMotion

/**
 * Shared-element transition context provided by the Biblioteca [SheetPager] host. Null in
 * previews and isolated UI tests so shared-element modifiers degrade to no-ops.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Immutable
internal data class LibrarySharedTransition(
    val sharedScope: SharedTransitionScope,
    val animatedContentScope: AnimatedContentScope,
    val isOverlayShowing: Boolean = false,
)

internal val LocalLibrarySharedTransition =
    compositionLocalOf<LibrarySharedTransition?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
private val LibraryBoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = SheetMotion.PAGE_MS)
}

internal fun libraryPhotoSharedKey(libraryId: String): String = "library_photo_$libraryId"

internal fun libraryZoneArtSharedKey(zoneId: Int): String = "library_zone_art_$zoneId"

internal fun libraryBookingArtSharedKey(reservationId: Int): String =
    "library_booking_art_$reservationId"

/**
 * Attaches a shared element synced to [SheetMotion.PAGE_MS] so bounds follow both programmatic
 * page changes and predictive-back seeking (commit and cancel). Keeps the modifier node intact
 * while suppressing overlay rendering whenever a sheet-local confirmation or outcome is showing.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.librarySharedElement(
    key: String,
    clipShape: Shape? = null,
): Modifier {
    val transition = LocalLibrarySharedTransition.current ?: return this
    return with(transition.sharedScope) {
        val state = rememberSharedContentState(key = key)
        if (clipShape != null) {
            sharedElement(
                sharedContentState = state,
                animatedVisibilityScope = transition.animatedContentScope,
                boundsTransform = LibraryBoundsTransform,
                renderInOverlayDuringTransition = !transition.isOverlayShowing,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
            )
        } else {
            sharedElement(
                sharedContentState = state,
                animatedVisibilityScope = transition.animatedContentScope,
                boundsTransform = LibraryBoundsTransform,
                renderInOverlayDuringTransition = !transition.isOverlayShowing,
            )
        }
    }
}
