package it.attendance100.mybicocca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
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
  val destination by viewModel.startDestination

  LaunchedEffect(destination) {
    destination?.let { dest ->
      navController.navigate(dest.route) {
        // Clear Splash from backstack so user can't go back to it
        popUpTo(Screen.Splash.route) { inclusive = true }
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color.Red
  ) {
    Box(contentAlignment = Alignment.Center) {
      // Show App Title or Logo while checking auth
      AppTitle(modifier = Modifier.size(200.dp))
    }
  }
}