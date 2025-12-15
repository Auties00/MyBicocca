package it.attendance100.mybicocca.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.viewmodel.*
import it.attendance100.mybicocca.viewmodel.login.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LoginManagerScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  viewModel: LoginViewModel = hiltViewModel(),
  mainViewModel: MainViewModel = hiltViewModel(),
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = GrayColor()
  val uriHandler = LocalUriHandler.current
  val isOffline by mainViewModel.isOffline.collectAsState()
  val isSessionExpired by mainViewModel.isSessionExpired.collectAsState()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
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
    Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(MaterialTheme.colorScheme.background)
    ) {
      StatusIndicator(
        isOffline = isOffline,
        isSessionExpired = isSessionExpired
      )
      Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = 24.dp, bottom = 32.dp)
        ) {
          SharedAvatar(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            size = 120.dp
          )
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
              .clickable { uriHandler.openUri("https://elearning.unimib.it") },
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
              .clickable { uriHandler.openUri("https://s3w.si.unimib.it") },
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
              text = stringResource(R.string.bottom_navbar_mappa),
              color = textColor,
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }

        // BicoccApp
        Surface(
          modifier = Modifier
              .fillMaxWidth()
              .clickable {
                // Navigate to the WebView
                navController.navigate(Screen.Login.route)
              },
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
              imageVector = ImageVector.vectorResource(R.drawable.logo),
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier
                  .scale(1.5f)
                  .size(24.dp)
            )
            Text(
              text = stringResource(R.string.bicoccapp),
              color = textColor,
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    // If we have a token, try to fetch data to prove it works
    viewModel.fetchAndLogProfile()
  }
}

