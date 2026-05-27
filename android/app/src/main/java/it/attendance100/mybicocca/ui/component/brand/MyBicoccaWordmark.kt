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
import it.attendance100.mybicocca.ui.navigation.LocalRootSharedTransitionScope
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent

// The two-colour "My"+"Bicocca" brand wordmark, shared by the splash, the login header and the
// app bar. Rendering it through one composable is what lets the splash mark morph into whichever
// destination follows: the same key ("mybicocca-wordmark") is used in all three, and they share
// AppRoot's top-level NavDisplay scope (LocalRootSharedTransitionScope + LocalNavAnimatedContentScope),
// so the morph rides Nav3's native entry transition — and scaleToBounds scales the glyphs.
private const val WordmarkSharedKey = "mybicocca-wordmark"

// Fixed-duration bounds animation, same rationale as TaxBoundsTransform: a default spring can
// settle before a heavy destination (MainShell) has laid out its app-bar target, so the morph
// reads as a snap. A fixed tween runs the morph the full way once the target resolves.
private val WordmarkBoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = 500, easing = FastOutSlowInEasing)
}

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
    // The top-level NavDisplay's AnimatedContentScope. In the app bar this resolves to the ROOT
    // NavDisplay (the app bar sits outside MainShell's inner NavDisplay), matching the splash/login
    // entries, so the wordmark morphs across the root entry transition. Read it only when the morph
    // is actually active so the wordmark still renders as plain Text without a NavDisplay (@Preview).
    val avScope = if (sharedElement && sharedScope != null) LocalNavAnimatedContentScope.current else null

    // Degrades to a plain Text when the scopes are absent (e.g. @Preview, or any caller that
    // doesn't opt into the shared element), so the wordmark is never coupled to AppRoot.
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
