package it.attendance100.mybicocca.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }

// App-root-level shared-transition scope from AppRoot's SharedTransitionLayout (which wraps the
// top-level NavDisplay). Kept distinct from LocalSharedTransitionScope above (MainShell's inner
// scope for list -> detail morphs) so the two never match each other's shared-element keys — they
// are independent SharedTransitionLayouts. The matching AnimatedVisibilityScope for the wordmark
// flight is the top-level NavDisplay's LocalNavAnimatedContentScope, not a separate local.
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalRootSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
