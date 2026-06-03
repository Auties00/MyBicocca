package it.attendance100.mybicocca.ui.component.bar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private enum class BarMode { PAGE, SUB_PAGE, SEARCH }

private const val MaxBackProgress = 0.9f

// Pill heights, shared so the shell can compute where the pill's bottom edge lands (the floating
// student card on Profilo floats just below it). Every page/sub-page — Profilo included — uses the
// same collapsed bar height. [PillTopGap] is the constant gap between the status bar and the pill.
internal val PillCollapsedHeight = 56.dp
internal val PillTopGap = 8.dp

// Fade-through morph helpers live in BarMorph.kt (same package) — shared with the search overlay.

@Composable
private fun MorphIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color,
    alpha: Float,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.graphicsLayer { fadeThroughLayer(alpha, minScale = 0.6f) },
    )
}

@Composable
fun MyBicoccaTopBar(
    // Sub-page coverage, driven by the NavDisplay transition in MainShell (seeks with the gesture).
    navProgress: FloatState,
    // Search field open/close, scrubbed below by this bar's own predictive-back handler.
    searchProgress: Animatable<Float, *>,
    canNavigateBack: Boolean,
    subPageTitle: String?,
    searchState: TopBarSearchState,
    onProfileClick: () -> Unit,
    onNavigateBack: () -> Unit,
    photo: File?,
    modifier: Modifier = Modifier,
    onFilterToggle: (() -> Unit)? = null,
    filterActive: Boolean = false,
    // The active sub-page's trailing action (e.g. a favourite star), hoisted up so the global
    // bar can render it during the morph — the per-screen local bar that owns it is hard-hidden
    // mid-morph, so without this the action would vanish instantly instead of morphing.
    trailingActions: (@Composable () -> Unit)? = null,
    globalAlpha: Float = 1f,
    // True while the active sub-page draws its content behind the bar and hasn't published a
    // runtime title yet: the expanded background melts away so the page shows through, with
    // only the back/action buttons floating on top. The pill background always returns as p
    // collapses, so the morph back to a tab page is unaffected.
    transparentBackground: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isSubPage = canNavigateBack && subPageTitle != null
    // Sub-page outranks search: a result can push a sub-page while search stays alive
    // underneath, so popping back lands on the still-open search view.
    val mode = when {
        isSubPage -> BarMode.SUB_PAGE
        searchState.active -> BarMode.SEARCH
        else -> BarMode.PAGE
    }

    fun closeSearch() {
        searchState.onActiveChange(false)
        searchState.onQueryChange("")
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    val motion = MaterialTheme.motionScheme
    val barSpec = remember(motion) { motion.defaultSpatialSpec<Float>() }

    // Only the search half is animated here; sub-page coverage rides navProgress (the NavDisplay
    // transition) so it stays in sync with the page slide and the predictive-back gesture.
    LaunchedEffect(searchState.active) {
        val target = if (searchState.active) 1f else 0f
        if (searchProgress.value != target) {
            searchProgress.animateTo(target, barSpec)
        }
    }

    // Disabled while a sub-page sits on top of search — back must pop the page (NavDisplay's
    // own predictive back), not scrub the search collapse underneath it.
    PredictiveBackHandler(enabled = searchState.active && !isSubPage) { backProgress ->
        try {
            keyboardController?.hide()
            backProgress.collect { event ->
                searchProgress.snapTo(1f - (event.progress / MaxBackProgress).coerceIn(0f, 1f))
            }
            searchProgress.animateTo(0f, barSpec)
            closeSearch()
        } catch (_: CancellationException) {
            searchProgress.animateTo(1f, barSpec)
        }
    }

    LaunchedEffect(searchState.active) {
        if (searchState.active) focusRequester.requestFocus()
    }

    // The chrome morphs on whichever driver is further along; the two never overlap in practice.
    val p = maxOf(navProgress.floatValue, searchProgress.value)
    val cornerRadius = lerp(32.dp, 0.dp, p)
    // The bar height is constant; only the chrome (corners, padding, title) morphs with p.
    val pillHeight = PillCollapsedHeight

    val outerHorizontalPadding = lerp(20.dp, 0.dp, p)
    val outerTopPadding = lerp(8.dp, 0.dp, p)
    // Alpha-only fade (same hue) so the bg dissolves in place when the page scrolls back to
    // the top, and the runtime-title handoff fades it back in over the scrolled content.
    val expandedColor by animateColorAsState(
        targetValue = if (transparentBackground) {
            scheme.surfaceContainer.copy(alpha = 0f)
        } else {
            scheme.surfaceContainer
        },
        label = "bar-expanded-color",
    )
    val containerColor = androidx.compose.ui.graphics.lerp(
        scheme.surfaceContainerHigh,
        expandedColor,
        p,
    )

    val statusBarHeight = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val outerStatusBarHeight = lerp(statusBarHeight, 0.dp, p)
    val innerStatusBarHeight = lerp(0.dp, statusBarHeight + PillTopGap, p)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = globalAlpha
                // Alpha 0 alone keeps the bar hit-testable, and Scaffold layers it above the
                // page — on the immersive video route the invisible bar swallowed the taps
                // meant for the player chrome. Shifting the layer off-screen removes it from
                // hit testing while the measured height (and thus the scaffold top inset
                // other sub-pages rely on mid-transition) stays stable.
                if (globalAlpha == 0f) translationY = -size.height
            },
    ) {
        Spacer(Modifier.height(outerStatusBarHeight)) // System bar padding
        Spacer(Modifier.height(outerTopPadding)) // Extra top padding

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = outerHorizontalPadding),
            shape = RoundedCornerShape(cornerRadius),
            color = containerColor,
            tonalElevation = lerp(6.dp, 0.dp, p),
        ) {
            Column {
                Spacer(Modifier.height(innerStatusBarHeight))

                // Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pillHeight)
                        .then(
                            if (mode == BarMode.PAGE && p < 0.1f) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { searchState.onActiveChange(true) }
                            } else Modifier,
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LeadingSlot(
                        mode = mode,
                        p = p,
                        onSearchClick = { searchState.onActiveChange(true) },
                        onBackClick = onNavigateBack,
                        onCloseSearch = ::closeSearch,
                        Modifier.padding(top = 7.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        // One shared anchor lerps the title's horizontal position from start
                        // (p=1, sub-page) to center (p=0, page), so the sub-page title travels
                        // to exactly where the wordmark sits as the bar collapses. Both children
                        // inherit this alignment, which is what makes the swap read as one title
                        // transforming into the other rather than two independent labels.
                        contentAlignment = BiasAlignment(horizontalBias = -p, verticalBias = -1f),
                    ) {
                        // Material "fade through": the outgoing text fades out before the
                        // incoming one fades in (handoff at the midpoint), so they are never
                        // composited on top of each other — no smeared text-on-text.
                        WordmarkContent(
                            alpha = fadeThroughCollapsed(p),
                            Modifier.padding(top = 17.dp)
                        )
                        when (mode) {
                            BarMode.PAGE -> Unit
                            BarMode.SUB_PAGE -> SubPageTitleContent(
                                title = subPageTitle.orEmpty(),
                                alpha = fadeThroughExpanded(p),
                                Modifier.padding(top = 17.dp)
                            )

                            BarMode.SEARCH -> SearchFieldContent(
                                query = searchState.query,
                                onQueryChange = searchState.onQueryChange,
                                // M3 search view: submitting commits the query and dismisses
                                // the keyboard but keeps the results visible.
                                onImeSearch = {
                                    searchState.onSubmit()
                                    keyboardController?.hide()
                                },
                                focusRequester = focusRequester,
                                alpha = fadeThroughExpanded(p),
                            )
                        }
                    }

                    TrailingSlot(
                        mode = mode,
                        p = p,
                        photo = photo,
                        onProfileClick = onProfileClick,
                        onFilterToggle = onFilterToggle,
                        filterActive = filterActive,
                        trailingActions = trailingActions,
                        searchQueryEmpty = searchState.query.isEmpty(),
                        dictating = searchState.dictating,
                        onMicClick = searchState.onMicClick,
                        onClearText = {
                            searchState.onQueryChange("")
                            // Clearing implies retyping: restore focus and bring the keyboard
                            // back if it was dismissed by scrolling or submitting. Both calls
                            // are no-ops when the field is already focused with the IME up.
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadingSlot(
    mode: BarMode,
    p: Float,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val onClick = when (mode) {
        BarMode.PAGE -> onSearchClick
        BarMode.SUB_PAGE -> onBackClick
        BarMode.SEARCH -> onCloseSearch
    }
    // Single click target, two glyphs handed off by fade-through + scale: the search icon owns
    // the collapsed (PAGE) state, the back arrow the expanded (SUB_PAGE / SEARCH) state. Only
    // one is visible at a time (they swap at the midpoint) and each shrinks as it leaves /
    // grows as it arrives, so the change reads as a morph instead of a stacked cross-fade.
    // Keeping one IconButton as the hit target also avoids overlapping-touch ambiguity mid-morph.
    IconButton(onClick = onClick, modifier = modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            MorphIcon(
                imageVector = Icons.Outlined.Search,
                contentDescription = if (mode == BarMode.PAGE) "Cerca" else null,
                tint = scheme.onSurface,
                alpha = fadeThroughCollapsed(p),
            )
            MorphIcon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = if (mode == BarMode.PAGE) null else "Indietro",
                tint = scheme.onSurface,
                alpha = fadeThroughExpanded(p),
            )
        }
    }
}


@Composable
private fun WordmarkContent(alpha: Float, modifier: Modifier = Modifier) {
    // Horizontal position comes from the parent's shared BiasAlignment; the passed modifier owns
    // the collapse/expand fade-through alpha + scale. sharedElement = true makes this the landing
    // target for the splash wordmark's flight (the shared-bounds modifier is applied inside the
    // component, outside this one) and is inert once that startup transition has settled.
    MyBicoccaWordmark(
        fontSize = 21.sp,
        sharedElement = true,
        modifier = modifier.graphicsLayer { fadeThroughLayer(alpha) },
    )
}

@Composable
private fun SubPageTitleContent(title: String, alpha: Float, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    // start=4dp inset matches the local sub-page top bar so the alpha handoff at p=1 doesn't
    // shift the title. Position otherwise comes from the parent's shared BiasAlignment.
    Crossfade(
        targetState = title,
        label = "sub-page-title",
        modifier = modifier
            .padding(start = 4.dp)
            .graphicsLayer { fadeThroughLayer(alpha) },
    ) { t ->
        Text(
            text = t,
            color = scheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchFieldContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onImeSearch: () -> Unit,
    focusRequester: FocusRequester,
    alpha: Float,
) {
    val scheme = MaterialTheme.colorScheme
    // Local TextFieldValue mirror of the hoisted query string: programmatic writes (history
    // insert, dictation transcripts) land with the cursor at the end, ready for typing;
    // user edits pass through with their own selection untouched.
    var fieldValue by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
    if (fieldValue.text != query) {
        fieldValue = TextFieldValue(query, TextRange(query.length))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { fadeThroughLayer(alpha) },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (query.isEmpty()) {
            Text(
                text = SearchPlaceholder,
                color = scheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        }
        BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                if (it.text != query) onQueryChange(it.text)
            },
            singleLine = true,
            cursorBrush = SolidColor(scheme.primary),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = scheme.onSurface,
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onImeSearch() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

// One app-wide placeholder — search is unified, not scoped to the visible tab.
private const val SearchPlaceholder = "Cerca in MyBicocca"

@Composable
private fun TrailingSlot(
    mode: BarMode,
    p: Float,
    photo: File?,
    onProfileClick: () -> Unit,
    onFilterToggle: (() -> Unit)?,
    filterActive: Boolean,
    trailingActions: (@Composable () -> Unit)?,
    searchQueryEmpty: Boolean,
    dictating: Boolean,
    onMicClick: () -> Unit,
    onClearText: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    // Same fade-through + scale handoff as the leading slot: the avatar (collapsed/PAGE) is
    // always composed — it reserves the slot's 40dp width and morphs into the expanded action
    // (the screen's own action if any, else filter on a sub-page, clear on search). Only one is
    // visible at a time, and only the variant matching the current `mode` is clickable so the
    // faded-out one can't intercept taps.
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        AvatarSlot(
            photo = photo,
            alpha = fadeThroughCollapsed(p),
            enabled = mode == BarMode.PAGE,
            onClick = onProfileClick,
        )
        when (mode) {
            BarMode.PAGE -> Unit
            BarMode.SUB_PAGE -> when {
                // The screen's own action (provided as a composable that captures its
                // entry-scoped ViewModel). Wrapped so it fades/scales through like the icons.
                trailingActions != null -> Box(
                    modifier = Modifier.graphicsLayer {
                        fadeThroughLayer(fadeThroughExpanded(p), minScale = 0.6f)
                    },
                ) {
                    trailingActions()
                }

                onFilterToggle != null -> IconButton(
                    onClick = onFilterToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    MorphIcon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filtri",
                        tint = if (filterActive) scheme.primary else scheme.onSurface,
                        alpha = fadeThroughExpanded(p),
                    )
                }

                else -> Unit
            }

            // M3 search view: mic while the field is empty (dictation entry point), clear once
            // there's text. Same single-hit-target + dual-glyph handoff as the leading slot.
            BarMode.SEARCH -> {
                val micAlpha by animateFloatAsState(
                    targetValue = if (searchQueryEmpty) 1f else 0f,
                    label = "search-mic-clear",
                )
                IconButton(
                    onClick = { if (searchQueryEmpty) onMicClick() else onClearText() },
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MorphIcon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (searchQueryEmpty) "Ricerca vocale" else null,
                            tint = if (dictating) scheme.primary else scheme.onSurface,
                            alpha = fadeThroughExpanded(p) * micAlpha,
                        )
                        MorphIcon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = if (searchQueryEmpty) null else "Cancella testo",
                            tint = scheme.onSurface,
                            alpha = fadeThroughExpanded(p) * (1f - micAlpha),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarSlot(
    photo: File?,
    alpha: Float,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { fadeThroughLayer(alpha, minScale = 0.6f) }
            .clip(CircleShape)
            .background(scheme.surfaceContainerHigh)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = "Profilo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profilo",
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun MyBicoccaTopBarPreview() {
    BicoccaTheme(dark = true) {
        MyBicoccaTopBar(
            navProgress = remember { mutableFloatStateOf(0f) },
            searchProgress = remember { Animatable(0f) },
            canNavigateBack = false,
            subPageTitle = null,
            searchState = TopBarSearchState(
                query = "",
                active = false,
                dictating = false,
                onQueryChange = {},
                onActiveChange = {},
                onMicClick = {},
                onSubmit = {},
            ),
            onProfileClick = {},
            onNavigateBack = {},
            photo = null,
        )
    }
}

@Preview
@Composable
private fun MyBicoccaTopBarSubPagePreview() {
    BicoccaTheme(dark = true) {
        MyBicoccaTopBar(
            navProgress = remember { mutableFloatStateOf(1f) },
            searchProgress = remember { Animatable(0f) },
            canNavigateBack = true,
            subPageTitle = "Dettaglio Esame",
            searchState = TopBarSearchState(
                query = "",
                active = false,
                dictating = false,
                onQueryChange = {},
                onActiveChange = {},
                onMicClick = {},
                onSubmit = {},
            ),
            onProfileClick = {},
            onNavigateBack = {},
            photo = null,
        )
    }
}

@Preview
@Composable
private fun MyBicoccaTopBarSearchPreview() {
    BicoccaTheme(dark = true) {
        MyBicoccaTopBar(
            navProgress = remember { mutableFloatStateOf(0f) },
            searchProgress = remember { Animatable(1f) },
            canNavigateBack = false,
            subPageTitle = null,
            searchState = TopBarSearchState(
                query = "Sistemi",
                active = true,
                dictating = false,
                onQueryChange = {},
                onActiveChange = {},
                onMicClick = {},
                onSubmit = {},
            ),
            onProfileClick = {},
            onNavigateBack = {},
            photo = null,
        )
    }
}
