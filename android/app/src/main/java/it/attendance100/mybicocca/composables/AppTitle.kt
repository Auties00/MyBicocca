package it.attendance100.mybicocca.composables

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R


@Composable
fun AppTitle() {
  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground

  Text(
    text = buildAnnotatedString {
      withStyle(
        style = SpanStyle(
          color = textColor,
          fontWeight = FontWeight.Normal,
          fontSize = 20.sp
        )
      ) {
        append(stringResource(R.string.homescreen_app_prefix))
      }
      withStyle(
        style = SpanStyle(
          color = primaryColor,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp
        )
      ) {
        append(stringResource(R.string.homescreen_app_suffix))
      }
    }
  )
}