package it.attendance100.mybicocca.screens

import androidx.biometric.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.composables.*
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

