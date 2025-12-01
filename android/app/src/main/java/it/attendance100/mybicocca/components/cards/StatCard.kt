package it.attendance100.mybicocca.components.cards

import android.content.res.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*


@Composable
fun StatCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  textColor: Color,
  secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  val haptic = rememberHapticManager()
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceDim
    ),
    onClick = {
      haptic.tap()
    },
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = title,
        color = secondaryColor,
        fontSize = 12.sp,
        maxLines = 2
      )
      Text(
        text = value,
        color = textColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun StatCardDarkPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        StatCard(
          title = "Media aritmetica",
          value = "8.5",
          textColor = textColor,
          secondaryColor = GrayColor(),
        )
      }
    }
  }
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun StatCardLightPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        StatCard(
          title = "Media aritmetica",
          value = "8.5",
          textColor = textColor,
          secondaryColor = GrayColor(),
        )
      }
    }
  }
}