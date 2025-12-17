package it.attendance100.mybicocca.ui.component.appbar

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.GrayColor


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    grayColor: Color,
) {
    SubcomposeAsyncImage(
        model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
        contentDescription = stringResource(R.string.profile_screen),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.scale(.33f),
                        strokeWidth = 3.dp,
                        color = primaryColor
                    )
                }
            }

            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.error_loading_image),
                        tint = grayColor,
                        modifier = Modifier.scale(.5f)
                    )
                }
            }

            else -> SubcomposeAsyncImageContent()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedAvatar(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = GrayColor()

    with(sharedTransitionScope) {
        SubcomposeAsyncImage(
            model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
            contentDescription = stringResource(R.string.profile_screen),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "avatar"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 400)
                    },
                    clipInOverlayDuringTransition = OverlayClip(CircleShape)
                )
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.33f),
                            strokeWidth = 3.dp,
                            color = primaryColor
                        )
                    }
                }

                is AsyncImagePainter.State.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.error_loading_image),
                            tint = grayColor,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                }

                else -> SubcomposeAsyncImageContent()
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HoistedAvatar(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    animatedX: Dp,
    animatedY: Dp,
    avatarSize: Dp,
    onClick: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = GrayColor()

    with(sharedTransitionScope) {
        SubcomposeAsyncImage(
            model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
            contentDescription = stringResource(R.string.profile_screen),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .offset(x = animatedX, y = animatedY)
                .size(avatarSize)
                .clip(CircleShape)
                .clickable { onClick() }
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "avatar"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ -> tween(durationMillis = 400) },
                    clipInOverlayDuringTransition = OverlayClip(CircleShape)
                )
        ) {
            val state = painter.state
            Crossfade(targetState = state) { currentState ->
                when (currentState) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(23.dp),
                                strokeWidth = 2.dp,
                                color = primaryColor
                            )
                        }
                    }

                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                stringResource(R.string.error_loading_image),
                                tint = grayColor
                            )
                        }
                    }

                    else -> SubcomposeAsyncImageContent()
                }
            }
        }
    }
}