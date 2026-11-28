package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoFood

/** The icon that stands for allergens everywhere in the Mensa screens. */
internal val MensaAllergenIcon: ImageVector get() = Icons.Outlined.NoFood

/** The scopes a dish plate needs to fly between a card and the dish carousel. */
@OptIn(ExperimentalSharedTransitionApi::class)
internal class MensaSharedScopes(
    val transition: SharedTransitionScope,
    val visibility: AnimatedVisibilityScope,
)

internal val LocalMensaShared = compositionLocalOf<MensaSharedScopes?> { null }

/** Makes this dish's plate a shared element, so it morphs between its card and the carousel. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.mensaPlateElement(dishId: Int, enabled: Boolean = true): Modifier {
    val scopes = LocalMensaShared.current
    if (scopes == null || !enabled) return this
    return with(scopes.transition) {
        this@mensaPlateElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "mensa_plate_$dishId"),
            animatedVisibilityScope = scopes.visibility,
        )
    }
}
