package it.attendance100.mybicocca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.viewmodel.*

@Composable
fun SplashScreen(
  navController: NavController,
  viewModel: SplashViewModel = hiltViewModel(),
) {
  val state by viewModel.splashState

  LaunchedEffect(state) {
    when (state) {
      is SplashViewModel.SplashState.NavigateHome -> {
        navController.navigate(Screen.Home.route) {
          popUpTo(Screen.Splash.route) { inclusive = true }
        }
      }

      is SplashViewModel.SplashState.NavigateLogin -> {
        navController.navigate(Screen.Login.route) {
          popUpTo(Screen.Splash.route) { inclusive = true }
        }
      }

      else -> {}
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(contentAlignment = Alignment.Center) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        AppTitle(modifier = Modifier.size(200.dp))

        if (state is SplashViewModel.SplashState.ShowLoginButton) {
          Spacer(modifier = Modifier.height(32.dp))
          Button(
            onClick = { navController.navigate(Screen.Login.route) }
          ) {
            Text("Login")
          }
        } else if (state is SplashViewModel.SplashState.Loading) {
          Spacer(modifier = Modifier.height(32.dp))
          CircularProgressIndicator()
        }
      }
    }
  }
}