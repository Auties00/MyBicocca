package it.attendance100.mybicocca.screens

import androidx.biometric.BiometricManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import coil.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LoginManagerScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 13.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.arrow_back),
              tint = textColor
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.login_manager),
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  ) { paddingValues ->
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)
    val canUseBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    var fingerprintLoginEnabled by remember { mutableStateOf(false) }

    Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(MaterialTheme.colorScheme.background)
          .verticalScroll(rememberScrollState())
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 32.dp)
      ) {
        with(sharedTransitionScope) {
          SubcomposeAsyncImage(
            model = "https://lh3.googleusercontent.com/a/ACg8ocLz6eMAklEzeodysm38Y18Ult6bw96hlhQ_DCheY_eEnuoLeno=s298-c-no",
            contentDescription = stringResource(R.string.homescreen_profile),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .sharedElement(
                  state = rememberSharedContentState(key = "avatar"),
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
                    modifier = Modifier.size(40.dp),
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
                    contentDescription = null,
                    tint = grayColor,
                    modifier = Modifier.size(60.dp)
                  )
                }
              }

              else -> SubcomposeAsyncImageContent()
            }
          }
        }
        Text(
          text = stringResource(R.string.login_manager),
          color = textColor,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = stringResource(R.string.login_manager_desc),
          color = grayColor,
          fontSize = 16.sp
        )
      }

      // Security section
      Text(
        text = stringResource(R.string.logins_list),
        color = primaryColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
      )

      // Elearning
      Surface(
        modifier = Modifier
            .fillMaxWidth()
        //  .clickable() {} TODO add elearning login
        ,
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
          Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = stringResource(R.string.bottom_navbar_elearning),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      // Segreterie
      Surface(
        modifier = Modifier
            .fillMaxWidth()
        //  .clickable() {} TODO add segreterie login
        ,
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
          Icon(
            imageVector = Icons.Default.ContactPage,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = stringResource(R.string.bottom_navbar_segreterie),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }

      // Security section
      Text(
        text = stringResource(R.string.security),
        color = primaryColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
      )

      // Fingerprint login setting
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canUseBiometric) {
              fingerprintLoginEnabled = !fingerprintLoginEnabled
            },
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Fingerprint,
              contentDescription = null,
              tint = if (canUseBiometric) primaryColor else grayColor,
              modifier = Modifier.size(24.dp)
            )
            Column(
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = stringResource(R.string.fingerprint_login),
                color = if (canUseBiometric) textColor else grayColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
              )
              Text(
                text = stringResource(R.string.fingerprint_login_desc),
                color = grayColor,
                fontSize = 13.sp,
                lineHeight = 16.sp
              )
            }
          }
          Switch(
            checked = fingerprintLoginEnabled,
            onCheckedChange = { fingerprintLoginEnabled = it },
            enabled = canUseBiometric,
            colors = SwitchDefaults.colors(
              checkedThumbColor = primaryColor,
              checkedTrackColor = primaryColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(start = 15.dp)
          )
        }
      }
    }
  }
}

