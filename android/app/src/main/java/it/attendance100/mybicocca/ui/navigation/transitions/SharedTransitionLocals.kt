package it.attendance100.mybicocca.ui.navigation.transitions

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * MainShell's inner shared-transition scope, from the SharedTransitionLayout wrapping its
 * NavDisplay, so list items can morph into detail entries. Null outside the shell (e.g. previews),
 * which turns the bicocca shared modifiers into no-ops.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * The enclosing nav entry's [AnimatedContentScope], bridged from the NavDisplay by the hosting
 * entry so shared elements deeper in the page tree can seek with the page transition.
 */
val LocalAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }

/**
 * App-root-level shared-transition scope from AppRoot's SharedTransitionLayout (which wraps the
 * top-level NavDisplay). Kept distinct from [LocalSharedTransitionScope] (MainShell's inner scope
 * for list-to-detail morphs) so the two never match each other's shared-element keys — they are
 * independent SharedTransitionLayouts. The matching AnimatedVisibilityScope for the wordmark
 * flight is the top-level NavDisplay's LocalNavAnimatedContentScope, not a separate local.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalRootSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
