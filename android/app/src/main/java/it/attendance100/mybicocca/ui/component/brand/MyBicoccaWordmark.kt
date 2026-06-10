package it.attendance100.mybicocca.ui.component.brand

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import it.attendance100.mybicocca.ui.navigation.transitions.LocalRootSharedTransitionScope
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent

/** One key for every wordmark host, so any pair of them can hand the mark off as a shared element. */
private const val WordmarkSharedKey = "mybicocca-wordmark"

/**
 * Fixed-duration bounds animation for the wordmark flight: a default spring can settle before a
 * heavy destination (MainShell) has laid out its app-bar target, making the morph read as a
 * snap, whereas a fixed tween runs the morph the full way once the target resolves.
 */
private val WordmarkBoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = 500, easing = FastOutSlowInEasing)
}

/**
 * The two-colour "My"+"Bicocca" brand wordmark, shared by the splash, the login header and the
 * app bar.
 *
 * With [sharedElement] = true the text registers a shared-bounds element under one app-wide key
 * inside AppRoot's top-level NavDisplay scope, which is what lets the splash mark morph into
 * whichever destination follows, riding Nav3's native entry transition with the glyphs scaling
 * to the target bounds. In the app bar the animated scope resolves to the root NavDisplay (the
 * app bar sits outside MainShell's inner NavDisplay), matching the splash/login entries. The
 * scopes are read only while the morph is opted into, and when either is absent — previews, or
 * any caller that doesn't opt in — the wordmark degrades to plain text, so it is never coupled
 * to AppRoot.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MyBicoccaWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 21.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    sharedElement: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val sharedScope = LocalRootSharedTransitionScope.current
    val avScope = if (sharedElement && sharedScope != null) LocalNavAnimatedContentScope.current else null

    val sharedModifier = if (sharedScope != null && avScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(WordmarkSharedKey),
                animatedVisibilityScope = avScope,
                boundsTransform = WordmarkBoundsTransform,
                resizeMode = ResizeMode.scaleToBounds(contentScale = ContentScale.Fit),
            )
        }
    } else {
        Modifier
    }

    Text(
        text = wordmark(scheme.onSurface, BicoccaWordmarkAccent),
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        modifier = sharedModifier.then(modifier),
    )
}

private fun wordmark(myColor: Color, bicoccaColor: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = myColor)) { append("My") }
        withStyle(SpanStyle(color = bicoccaColor)) { append("Bicocca") }
    }
