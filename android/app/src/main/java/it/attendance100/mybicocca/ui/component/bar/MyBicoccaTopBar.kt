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
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
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
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private enum class BarMode { PAGE, SUB_PAGE, SEARCH }

private const val MaxBackProgress = 0.9f

/**
 * Collapsed pill height, identical on every page and sub-page (Profilo included). Exposed
 * together with [PillTopGap] so shell code can compute where the pill's bottom edge lands —
 * floating elements like Profilo's student card hang just below it.
 */
internal val PillCollapsedHeight = 56.dp

/** The constant gap between the status bar and the collapsed pill. */
internal val PillTopGap = 8.dp

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

/**
 * The single morphing top bar for the whole shell. Three modes share one surface — the
 * collapsed search pill on a tab page, the full-width sub-page header, and the open search
 * field. The bar height stays constant; only the chrome (corners, padding, title) morphs along
 * a 0..1 progress, taken as whichever of [navProgress] and [searchProgress] is further along
 * (the two never overlap in practice). Sub-page mode outranks search, because a search result
 * can push a sub-page while search stays alive underneath — popping back then lands on the
 * still-open search view. For the same reason the bar's own predictive-back handler (which
 * scrubs the search collapse) is disabled while a sub-page sits on top: back must pop the page,
 * not collapse the search underneath it.
 *
 * Title handoff: the wordmark and the sub-page title share one alignment anchor whose
 * horizontal bias lerps from center (collapsed) to start (expanded), so the sub-page title
 * travels to exactly where the wordmark sits as the bar collapses. Combined with the
 * fade-through ramp (the outgoing text is gone before the incoming one appears), the swap reads
 * as one title transforming into the other rather than two independent labels.
 *
 * Search follows the M3 search view: the IME search action submits the query but keeps the
 * results visible (only the keyboard dismisses), and clearing the field restores focus and
 * brings the keyboard back if it was dismissed by scrolling or submitting — clearing implies
 * retyping (both calls are no-ops when the field is already focused with the IME up).
 *
 * The bar's status-bar spacer is the raw inset minus whatever ancestors consumed: while the
 * offline band covers the status bar (its host consumes the covered share), the spacer shrinks
 * in lockstep so the band pushes the pill down cleanly instead of stacking on a stale inset.
 *
 * @param navProgress sub-page coverage, driven by the NavDisplay transition in MainShell (seeks
 *   with the predictive-back gesture); never animated by the bar itself, so it stays in sync
 *   with the page slide.
 * @param searchProgress search open/close progress — the one driver this bar animates itself
 *   and scrubs with its own predictive-back handler.
 * @param trailingActions the active sub-page's trailing action (e.g. a favourite star), hoisted
 *   up so the global bar can render it during the morph — the per-screen local bar that owns it
 *   is hard-hidden mid-morph, so without this the action would vanish instantly instead of
 *   morphing.
 * @param globalAlpha render opacity for the whole bar. At 0 the layer is additionally shifted
 *   off-screen: alpha alone keeps the bar hit-testable above the page (swallowing taps meant
 *   for content such as immersive player chrome), while translating it away removes it from hit
 *   testing and keeps the measured height — and thus the scaffold top inset other sub-pages
 *   rely on mid-transition — stable.
 * @param transparentBackground true while the active sub-page draws its content behind the bar
 *   and hasn't published a runtime title yet: the expanded background melts away (an alpha-only
 *   fade of the same hue, so it dissolves in place and fades back in over scrolled content for
 *   the runtime-title handoff), leaving only the back/action buttons floating on top. The pill
 *   background always returns as the progress collapses, so the morph back to a tab page is
 *   unaffected.
 */
@Composable
fun MyBicoccaTopBar(
    navProgress: FloatState,
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
    trailingActions: (@Composable () -> Unit)? = null,
    globalAlpha: Float = 1f,
    transparentBackground: Boolean = false,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isSubPage = canNavigateBack && subPageTitle != null
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

    LaunchedEffect(searchState.active) {
        val target = if (searchState.active) 1f else 0f
        if (searchProgress.value != target) {
            searchProgress.animateTo(target, barSpec)
        }
    }

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

    val p = maxOf(navProgress.floatValue, searchProgress.value)
    val cornerRadius = lerp(32.dp, 0.dp, p)
    val pillHeight = PillCollapsedHeight

    val outerHorizontalPadding = lerp(20.dp, 0.dp, p)
    val outerTopPadding = lerp(8.dp, 0.dp, p)
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

    val density = LocalDensity.current
    var consumedStatusBarPx by remember { mutableIntStateOf(0) }
    val statusBarHeight = with(density) {
        (WindowInsets.statusBars.getTop(this) - consumedStatusBarPx).coerceAtLeast(0).toDp()
    }
    val outerStatusBarHeight = lerp(statusBarHeight, 0.dp, p)
    val innerStatusBarHeight = lerp(0.dp, statusBarHeight + PillTopGap, p)

    Column(
        modifier = modifier
            .onConsumedWindowInsetsChanged { consumedStatusBarPx = it.getTop(density) }
            .fillMaxWidth()
            .graphicsLayer {
                alpha = globalAlpha
                if (globalAlpha == 0f) translationY = -size.height
            },
    ) {
        Spacer(Modifier.height(outerStatusBarHeight))
        Spacer(Modifier.height(outerTopPadding))

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pillHeight)
                        .then(
                            if (mode == BarMode.PAGE && p < 0.1f) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    haptic.tap()
                                    searchState.onActiveChange(true)
                                }
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
                        contentAlignment = BiasAlignment(horizontalBias = -p, verticalBias = -1f),
                    ) {
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

/**
 * Single click target with two glyphs handed off by fade-through + scale: the search icon owns
 * the collapsed (PAGE) state, the back arrow the expanded (SUB_PAGE / SEARCH) state. Only one
 * is visible at a time (they swap at the midpoint) and each shrinks as it leaves / grows as it
 * arrives, so the change reads as a morph instead of a stacked cross-fade. Keeping one
 * IconButton as the hit target also avoids overlapping-touch ambiguity mid-morph.
 */
@Composable
private fun LeadingSlot(
    mode: BarMode,
    p: Float,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    val onClick = when (mode) {
        BarMode.PAGE -> onSearchClick
        BarMode.SUB_PAGE -> onBackClick
        BarMode.SEARCH -> onCloseSearch
    }
    IconButton(onClick = { haptic.tap(); onClick() }, modifier = modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            MorphIcon(
                imageVector = Icons.Outlined.Search,
                contentDescription = if (mode == BarMode.PAGE) stringResource(R.string.topbar_search) else null,
                tint = scheme.onSurface,
                alpha = fadeThroughCollapsed(p),
            )
            MorphIcon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = if (mode == BarMode.PAGE) null else stringResource(R.string.common_back),
                tint = scheme.onSurface,
                alpha = fadeThroughExpanded(p),
            )
        }
    }
}


/**
 * Horizontal position comes from the parent's shared alignment anchor; the passed modifier owns
 * the collapse/expand fade-through alpha + scale. sharedElement = true makes this the landing
 * target for the splash wordmark's flight (the shared-bounds modifier is applied inside the
 * wordmark component itself) and is inert once that startup transition has settled.
 */
@Composable
private fun WordmarkContent(alpha: Float, modifier: Modifier = Modifier) {
    MyBicoccaWordmark(
        fontSize = 21.sp,
        sharedElement = true,
        modifier = modifier.graphicsLayer { fadeThroughLayer(alpha) },
    )
}

/**
 * The morphing sub-page title. Its start inset matches the local sub-page top bar so the alpha
 * handoff at full expansion doesn't shift the title; position otherwise comes from the parent's
 * shared alignment anchor.
 */
@Composable
private fun SubPageTitleContent(title: String, alpha: Float, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
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

/**
 * The bare search field. It keeps a local TextFieldValue mirror of the hoisted query string:
 * programmatic writes (history insert, dictation transcripts) land with the cursor at the end,
 * ready for typing, while user edits pass through with their own selection untouched.
 */
@Composable
private fun SearchFieldContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onImeSearch: () -> Unit,
    focusRequester: FocusRequester,
    alpha: Float,
) {
    val scheme = MaterialTheme.colorScheme
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
                text = stringResource(R.string.topbar_search_placeholder),
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

/**
 * Same fade-through + scale handoff as [LeadingSlot]: the avatar (collapsed/PAGE) is always
 * composed — it reserves the slot's 40dp width and morphs into the expanded action. On a
 * sub-page that is the screen's own [trailingActions] if any (a composable that captures its
 * entry-scoped ViewModel, wrapped so it fades/scales through like the icons), else the filter
 * toggle; on search, per the M3 search view, a mic while the field is empty (the dictation
 * entry point) and clear once there's text, sharing one hit target. Only one variant is visible
 * at a time, and only the one matching the current [mode] is clickable so the faded-out one
 * can't intercept taps.
 */
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
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
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
                trailingActions != null -> Box(
                    modifier = Modifier.graphicsLayer {
                        fadeThroughLayer(fadeThroughExpanded(p), minScale = 0.6f)
                    },
                ) {
                    trailingActions()
                }

                onFilterToggle != null -> IconButton(
                    onClick = { haptic.tap(); onFilterToggle() },
                    modifier = Modifier.size(40.dp)
                ) {
                    MorphIcon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = stringResource(R.string.topbar_filters),
                        tint = if (filterActive) scheme.primary else scheme.onSurface,
                        alpha = fadeThroughExpanded(p),
                    )
                }

                else -> Unit
            }

            BarMode.SEARCH -> {
                val micAlpha by animateFloatAsState(
                    targetValue = if (searchQueryEmpty) 1f else 0f,
                    label = "search-mic-clear",
                )
                IconButton(
                    onClick = {
                        haptic.tap()
                        if (searchQueryEmpty) onMicClick() else onClearText()
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MorphIcon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (searchQueryEmpty) stringResource(R.string.search_voice_title) else null,
                            tint = if (dictating) scheme.primary else scheme.onSurface,
                            alpha = fadeThroughExpanded(p) * micAlpha,
                        )
                        MorphIcon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = if (searchQueryEmpty) null else stringResource(R.string.topbar_clear_text),
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
    val haptic = rememberHapticManager()
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { fadeThroughLayer(alpha, minScale = 0.6f) }
            .clip(CircleShape)
            .background(scheme.surfaceContainerHigh)
            .clickable(enabled = enabled, onClick = { haptic.tap(); onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = stringResource(R.string.screen_title_profile),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = stringResource(R.string.screen_title_profile),
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
            subPageTitle = stringResource(R.string.topbar_exam_detail),
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
