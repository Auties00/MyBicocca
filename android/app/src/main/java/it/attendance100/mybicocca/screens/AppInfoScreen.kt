package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*

private val versionText: String by lazy {
  buildString {
    append("Version ${BuildConfig.VERSION_NAME}")
    // Get Flavor using reflection to avoid compile errors when no flavors are configured
    try {
      val flavorField = BuildConfig::class.java.getDeclaredField("FLAVOR")
      val flavorName = flavorField.get(null) as? String
      if (!flavorName.isNullOrEmpty()) {
        val capitalizedFlavor = flavorName.replaceFirstChar {
          if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        append(" ($capitalizedFlavor)")
      }
    } catch (_: Exception) {
      // FLAVOR field doesn't exist or error accessing it, no flavors configured
    }
    if (BuildConfig.DEBUG) append(" [Debug]")
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppInfoScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight
  val isDarkMode = rememberPreferencesManager().isDarkMode

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
            text = stringResource(R.string.app_info),
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(24.dp)
      ) {
        // App Name
        AppTitle()

        // Version Information
        Text(
          text = versionText,
          color = grayColor,
          fontSize = 16.sp
        )

        // App Logo (monochrome launcher)
        with(sharedTransitionScope) {
          Icon(
            painter = painterResource(if (isDarkMode) R.drawable.logo_text_dark else R.drawable.logo_text),
            contentDescription = stringResource(R.string.app_logo),
            tint = Color.Unspecified,
            modifier = Modifier
                .size(190.dp)
                .sharedElement(
                  sharedContentState = rememberSharedContentState(key = "appinfo_icon"),
                  animatedVisibilityScope = animatedContentScope,
                  boundsTransform = { _, _ ->
                    tween(durationMillis = 400)
                  }
                )
          )
        }

        // Copyright Text
        Text(
          text = stringResource(R.string.copyright),
          color = grayColor,
          fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Licenses Button
        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
          onClick = {
            val intent = android.content.Intent(context, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
            context.startActivity(intent)
          },
          modifier = Modifier
              .fillMaxWidth(0.6f)
              .height(48.dp)
        ) {
          Text(
            text = stringResource(R.string.licenses),
            fontSize = 16.sp
          )
        }
      }
    }
  }
}

