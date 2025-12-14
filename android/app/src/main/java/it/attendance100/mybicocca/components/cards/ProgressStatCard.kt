package it.attendance100.mybicocca.components.cards

import android.content.res.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*


@Composable
fun ProgressStatCard(
  modifier: Modifier = Modifier,
  title: String,
  current: Int,
  total: Int,
  primaryColor: Color? = null,
  textColor: Color,
  secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  progressbar: Boolean = false,
  backgroundProgressBar: Boolean = false,
) {
  var primaryColor by remember { mutableStateOf(primaryColor) }
  if (primaryColor == null) {
    val preferencesManager = rememberPreferencesManager()
    primaryColor = if (preferencesManager.isDarkMode ?: isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
  }

  val progress = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
  val haptic = rememberHapticManager()

  val outerRadius = 16

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceDim
    ),
    onClick = {
      haptic.spring()
    },
    shape = RoundedCornerShape(outerRadius.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val padding = 6
      val innerRadius = outerRadius - padding
      if (backgroundProgressBar) {
        Box(
          modifier = Modifier
              .matchParentSize()
              .padding(padding.dp)
              .clip(RoundedCornerShape(innerRadius.dp))
        ) {
          // Box(
          //   modifier = Modifier
          //       .fillMaxHeight()
          //       .fillMaxWidth()
          //       .background(secondaryColor.copy(alpha = 0.05f))
          // )
          Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(primaryColor!!)
          )
        }
      }

      Column(
        modifier = Modifier
            .padding(outerRadius.dp)
            .padding(horizontal = 8.dp)
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
          text = "$current/$total",
          color = textColor,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )

        // Progress bar
        if (progressbar) Box(
          modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(secondaryColor.copy(alpha = 0.2f))
        ) {
          Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(primaryColor!!)
          )
        }
      }
    }
  }
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardDarkPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 8,
          total = 10,
          textColor = textColor,
        )
      }
    }
  }
}

@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBarDarkPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 125.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 8,
          total = 10,
          textColor = textColor,
          progressbar = true,
        )
      }
    }
  }
}

@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBGBarDarkPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaDarkColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 7,
          total = 10,
          textColor = textColor,
          backgroundProgressBar = true,
        )
      }
    }
  }
}


@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardLightPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 8,
          total = 10,
          textColor = textColor,
        )
      }
    }
  }
}

@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBarLightPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 125.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 8,
          total = 10,
          textColor = textColor,
          progressbar = true,
        )
      }
    }
  }
}

@Preview(showSystemUi = false, uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
fun ProgressStatCardBGBarLightPreview() {
  ProvideHapticManager {
    MaterialTheme(colorScheme = MyBicoccaLightColorScheme) {
      val textColor = MaterialTheme.colorScheme.onBackground
      Box(
        modifier = Modifier
            .size(200.dp, 110.dp)
            .padding(8.dp)
      ) {
        ProgressStatCard(
          title = "Media aritmetica",
          current = 8,
          total = 10,
          textColor = textColor,
          backgroundProgressBar = true,
        )
      }
    }
  }
}

