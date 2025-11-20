package it.attendance100.mybicocca.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import coil.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*

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
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  with(sharedTransitionScope) {
    SubcomposeAsyncImage(
      model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
      contentDescription = stringResource(R.string.homescreen_profile),
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(Modifier.size(23.dp), strokeWidth = 2.dp, color = primaryColor)
            }
          }

          is AsyncImagePainter.State.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Person, stringResource(R.string.error_loading_image), tint = grayColor)
            }
          }

          else -> SubcomposeAsyncImageContent()
        }
      }
    }
  }
}