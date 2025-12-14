package it.attendance100.mybicocca.screens.career

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.*


@Composable
fun PianoTab(
  user: User?,
  stats: CareerStats?,
) {
  Text(
    text = stringResource(R.string.career_tab_piano),
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold
  )
}
