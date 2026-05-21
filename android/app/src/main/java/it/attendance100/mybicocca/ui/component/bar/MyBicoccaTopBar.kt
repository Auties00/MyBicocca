package it.attendance100.mybicocca.ui.component.bar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private enum class BarMode { PAGE, SUB_PAGE, SEARCH }

private const val MaxBackProgress = 0.9f

@Composable
fun MyBicoccaTopBar(
    progress: Animatable<Float, *>,
    canNavigateBack: Boolean,
    subPageTitle: String?,
    searchState: TopBarSearchState,
    onProfileClick: () -> Unit,
    onNavigateBack: () -> Unit,
    photo: File?,
    modifier: Modifier = Modifier,
    onFilterToggle: (() -> Unit)? = null,
    filterActive: Boolean = false,
    globalAlpha: Float = 1f,
) {
    val scheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isSubPage = canNavigateBack && subPageTitle != null
    val mode = when {
        searchState.active -> BarMode.SEARCH
        isSubPage -> BarMode.SUB_PAGE
        else -> BarMode.PAGE
    }
    val expanded = mode != BarMode.PAGE

    fun closeSearch() {
        searchState.onActiveChange(false)
        searchState.onQueryChange("")
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    val motion = MaterialTheme.motionScheme
    val barSpec = remember(motion) { motion.defaultSpatialSpec<Float>() }

    LaunchedEffect(expanded) {
        val target = if (expanded) 1f else 0f
        if (progress.value != target) {
            progress.animateTo(target, barSpec)
        }
    }

    PredictiveBackHandler(enabled = searchState.active) { backProgress ->
        try {
            keyboardController?.hide()
            backProgress.collect { event ->
                progress.snapTo(1f - (event.progress / MaxBackProgress).coerceIn(0f, 1f))
            }
            progress.animateTo(0f, barSpec)
            closeSearch()
        } catch (_: CancellationException) {
            progress.animateTo(1f, barSpec)
        }
    }

    LaunchedEffect(searchState.active) {
        if (searchState.active) focusRequester.requestFocus()
    }

    val p = progress.value
    val cornerRadius = lerp(32.dp, 0.dp, p)
    val outerHorizontalPadding = lerp(20.dp, 0.dp, p)
    val outerTopPadding = lerp(8.dp, 0.dp, p)
    val containerColor = androidx.compose.ui.graphics.lerp(
        scheme.surfaceContainerHigh,
        scheme.surfaceContainer,
        p,
    )

    val statusBarHeight = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val outerStatusBarHeight = lerp(statusBarHeight, 0.dp, p)
    val innerStatusBarHeight = lerp(0.dp, statusBarHeight + 8.dp, p)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = globalAlpha },
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
                        .height(56.dp)
                        .then(
                            if (mode == BarMode.PAGE && p < 0.1f) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { searchState.onActiveChange(true) }
                            } else Modifier,
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LeadingSlot(
                        mode = mode,
                        onSearchClick = { searchState.onActiveChange(true) },
                        onBackClick = onNavigateBack,
                        onCloseSearch = ::closeSearch,
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        when (mode) {
                            BarMode.PAGE -> WordmarkContent(p = p)
                            BarMode.SUB_PAGE -> SubPageTitleContent(
                                title = subPageTitle.orEmpty(),
                                p = p,
                            )
                            BarMode.SEARCH -> SearchFieldContent(
                                query = searchState.query,
                                placeholder = searchState.placeholder,
                                onQueryChange = searchState.onQueryChange,
                                onImeDone = ::closeSearch,
                                focusRequester = focusRequester,
                            )
                        }
                    }

                    TrailingSlot(
                        mode = mode,
                        photo = photo,
                        avatarAlpha = 1f - p,
                        onProfileClick = onProfileClick,
                        onFilterToggle = onFilterToggle,
                        filterActive = filterActive,
                        onClearSearch = {
                            if (searchState.query.isEmpty()) closeSearch()
                            else searchState.onQueryChange("")
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadingSlot(
    mode: BarMode,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onCloseSearch: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val onClick = when (mode) {
        BarMode.PAGE -> onSearchClick
        BarMode.SUB_PAGE -> onBackClick
        BarMode.SEARCH -> onCloseSearch
    }
    val icon = when (mode) {
        BarMode.PAGE -> Icons.Outlined.Search
        BarMode.SUB_PAGE -> Icons.AutoMirrored.Outlined.ArrowBack
        BarMode.SEARCH -> Icons.AutoMirrored.Outlined.ArrowBack
    }
    val description = when (mode) {
        BarMode.PAGE -> "Cerca"
        else -> "Indietro"
    }
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(imageVector = icon, contentDescription = description, tint = scheme.onSurface)
    }
}

@Composable
private fun WordmarkContent(p: Float) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - p
                translationX = p * 30.dp.toPx()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = wordmark(scheme.onSurface, BicoccaWordmarkAccent),
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SubPageTitleContent(title: String, p: Float) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp)
            .graphicsLayer {
                alpha = p
                translationX = (1f - p) * -30.dp.toPx()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Crossfade(targetState = title, label = "sub-page-title") { t ->
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
}

@Composable
private fun SearchFieldContent(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onImeDone: () -> Unit,
    focusRequester: FocusRequester,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
            Text(
                text = placeholder,
                color = scheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(scheme.primary),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = scheme.onSurface,
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onImeDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

@Composable
private fun TrailingSlot(
    mode: BarMode,
    photo: File?,
    avatarAlpha: Float,
    onProfileClick: () -> Unit,
    onFilterToggle: (() -> Unit)?,
    filterActive: Boolean,
    onClearSearch: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    when (mode) {
        BarMode.PAGE -> AvatarSlot(
            photo = photo,
            alpha = avatarAlpha,
            onClick = onProfileClick,
        )
        BarMode.SUB_PAGE -> {
            if (onFilterToggle != null) {
                IconButton(onClick = onFilterToggle, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filtri",
                        tint = if (filterActive) scheme.primary else scheme.onSurface,
                    )
                }
            } else {
                Spacer(Modifier.size(40.dp))
            }
        }
        BarMode.SEARCH -> {
            IconButton(onClick = onClearSearch, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Chiudi ricerca",
                    tint = scheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AvatarSlot(
    photo: File?,
    alpha: Float,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(scheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = "Profilo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
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

private fun wordmark(myColor: Color, bicoccaColor: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = myColor)) { append("My") }
        withStyle(SpanStyle(color = bicoccaColor)) { append("Bicocca") }
    }
