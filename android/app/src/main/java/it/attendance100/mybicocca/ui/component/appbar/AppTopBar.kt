package it.attendance100.mybicocca.ui.component.appbar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import it.attendance100.mybicocca.R
import kotlin.coroutines.cancellation.CancellationException

private enum class BarMode { PAGE, SUB_PAGE, SEARCH }
private const val MAX_BACK_PROGRESS = 0.9f

@Composable
fun AppTopBar(
    profilePic: ByteArray?,
    canNavigateBack: Boolean,
    subPageTitle: String?,
    externalProgress: Animatable<Float, *>? = null,
    onNavigateBack: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val isSubPage = canNavigateBack && subPageTitle != null
    val mode = when {
        searchActive -> BarMode.SEARCH
        isSubPage -> BarMode.SUB_PAGE
        else -> BarMode.PAGE
    }
    val expanded = mode != BarMode.PAGE

    fun closeSearch() {
        searchActive = false
        query = ""
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    val internalProgress = remember { Animatable(if (expanded) 1f else 0f) }
    val progress = externalProgress ?: internalProgress

    LaunchedEffect(expanded) {
        val target = if (expanded) 1f else 0f
        if (progress.value != target) {
            progress.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    PredictiveBackHandler(enabled = searchActive) { backProgress ->
        try {
            keyboardController?.hide()
            backProgress.collect { event ->
                progress.snapTo(1f - (event.progress / MAX_BACK_PROGRESS).coerceIn(0f, 1f))
            }
            progress.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            closeSearch()
        } catch (_: CancellationException) {
            progress.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    LaunchedEffect(searchActive) {
        if (searchActive) focusRequester.requestFocus()
    }

    val p = progress.value
    val cornerRadius = lerp(28.dp, 0.dp, p)
    val horizontalPadding = lerp(16.dp, 0.dp, p)
    val topPadding = lerp(8.dp, 0.dp, p)
    val avatarAlpha = 1f - p

    val statusBarHeight = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    val outerStatusBarHeight = lerp(statusBarHeight, 0.dp, p)
    val innerStatusBarHeight = lerp(0.dp, statusBarHeight + 8.dp, p)

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(outerStatusBarHeight))
        Spacer(Modifier.height(topPadding))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            shape = RoundedCornerShape(cornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = lerp(6.dp, 0.dp, p),
        ) {
            Column {
                Spacer(Modifier.height(innerStatusBarHeight))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .then(
                            if (mode == BarMode.PAGE && p < 0.1f) Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { searchActive = true }
                            else Modifier
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Leading icon — crossfade driven by p
                    IconButton(
                        onClick = {
                            when (mode) {
                                BarMode.PAGE -> searchActive = true
                                BarMode.SUB_PAGE -> onNavigateBack()
                                BarMode.SEARCH -> closeSearch()
                            }
                        },
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                                modifier = Modifier.graphicsLayer { alpha = avatarAlpha },
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.arrow_back),
                                modifier = Modifier.graphicsLayer { alpha = p },
                            )
                        }
                    }

                    // Center content — both rendered, crossfade + slide driven by p
                    Box(
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        // PAGE: centered logo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = avatarAlpha
                                    translationX = p * size.width * 0.15f
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            AppTitle()
                        }

                        // EXPANDED: left-aligned title or search field
                        if (expanded || p > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = p
                                        translationX = avatarAlpha * -size.width * 0.25f
                                    },
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (isSubPage) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = subPageTitle ?: "",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                } else if (searchActive) {
                                    BasicTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .padding(start = 4.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { closeSearch() }),
                                        decorationBox = { innerTextField ->
                                            Box {
                                                if (query.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.calendar_global_search_hint),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Trailing icon — avatar fades with p, clear button for search
                    Box {
                        IconButton(
                            onClick = onProfileClick,
                            enabled = mode == BarMode.PAGE,
                            modifier = Modifier.graphicsLayer { alpha = avatarAlpha },
                        ) {
                            ProfileAvatar(
                                profilePic = profilePic,
                                contentDescription = stringResource(R.string.homescreen_profile),
                                size = 32.dp,
                            )
                        }
                        if (mode == BarMode.SEARCH && query.isNotEmpty()) {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.graphicsLayer { alpha = p },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close),
                                )
                            }
                        }
                    }
                }

                if (searchActive) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = p),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(p.coerceIn(0.01f, 1f))
                            .graphicsLayer { alpha = p }
                            .imePadding(),
                    ) {
                        // TODO: search results
                    }
                }
            }
        }
    }

    if (searchActive && p > 0.5f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (p - 0.5f) * 2f }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { closeSearch() },
        )
    }
}
