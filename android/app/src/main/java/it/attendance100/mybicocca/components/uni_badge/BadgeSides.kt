package it.attendance100.mybicocca.components.uni_badge


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.domain.model.*

val usr = User(
  name = "Federico Giarrusso",
  surname = "e' super power ninja turbo neo ultra gay",
  matricola = "909369",
  course = "Informatica",
  year = "3",
  email = "l.lupi3@campus.unimib.it"
)

@Composable
@Preview
fun BadgeFrontPreview() {
  CardFace(
    modifier = Modifier.wrapContentSize().run { this },
    content = { BadgeFront(usr) },
    background = MaterialTheme.colorScheme.primary,
    isChromatic = true,
    touchX = 0f,
    touchY = 0f,
  )
}

@Composable
@Preview
fun BadgeBackPreview() {
  CardFace(
    // modifier = Modifier.wrapContentSize().run { this },
    content = { BadgeBack(usr) },
    background = MaterialTheme.colorScheme.primary,
    isChromatic = true,
    touchX = 0f,
    touchY = 0f,
  )
}

@Composable
fun BadgeFront(user: User?, textColor: Color = MaterialTheme.colorScheme.onBackground) {
  Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(top = 40.dp, start = 40.dp)
  ) {
    val scale = .8
    val height = 52

    // BG Texture
    DitheredTexture(
      modifier = Modifier
          .fillMaxSize()
          .offset(x = (-180).dp)
          .rotate(0f),
      color = Color.Black,
      spacing = 10f,          // Distance between dots
      dotSize = 5f,          // Size of the dot
      globalRotation = 35f,   // Rotate the whole wall of dots
      dotRotation = 80f,       // Rotate the rhombus itself
      fadeStart = 0.2f,       // Fade starts at 10% width
      fadeEnd = 0.8f          // Fade ends at 90% width
    )

    // BG MyBicocca Logo
    Image(
      painter = painterResource(R.drawable.logo_mono),
      contentDescription = null,

      modifier = Modifier
          .wrapContentSize()
          .size(900.dp)

          .scale(2f)
          .rotate(7.5f)
          .absoluteOffset(x = (43).dp, y = (30).dp)

          // .scale(2.25f)
          // .rotate(-7.5f)
          // .absoluteOffset(x = (-60).dp, y = (-5).dp)

          .alpha(0.2f),
      colorFilter = ColorFilter.lighting(Color.White, Color.White)
    )

    // Top MyBicocca logo
    Image(
      painter = painterResource(R.drawable.logo_mono),
      contentDescription = null,
      modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 12.dp)
          .absoluteOffset(y = (-35).dp)
          .size(90.dp),
      colorFilter = ColorFilter.lighting(Color.White, Color.White)
    )

    // Università degli Studi MyBicocca
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier
          .fillMaxWidth()
          .absoluteOffset(x = (-85).dp, y = (-2).dp)
    ) {
      // Università degli Studi
      Text(
        text = stringResource(R.string.badge_university_full),
        fontSize = 17.sp,
      )
      // MyBicocca Logo
      Image(
        painter = painterResource(R.drawable.text),
        contentDescription = null,
        modifier = Modifier
            .absoluteOffset(y = (-40).dp)
            .size(100.dp),
        colorFilter = ColorFilter.lighting(Color.White, Color.White)
      )
    }

    // Chip
    Image(
      painter = painterResource(R.drawable.chip1),
      contentDescription = null,
      modifier = Modifier
          .padding(top = (height + 3).dp)
          .width(scale * 64.dp)
          .height(scale * 52.dp)
          .clip(RoundedCornerShape(corner = CornerSize(scale * 9.5.dp))),
      colorFilter = ColorFilter.tint(Color(0xFFffbc21), blendMode = BlendMode.Multiply)
    )

    // Contactless Chip
    Icon(
      painter = painterResource(R.drawable.contactless),
      contentDescription = null,
      modifier = Modifier
          .padding(top = (height + 8).dp, start = 65.dp)
          .size(30.dp)
    )

    // Name + Surname + Matricola
    Column(
      modifier = Modifier
          .padding(top = (height + 60).dp, end = 28.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Name + Surname
      BasicText(
        text = "${user?.name ?: ""} ${user?.surname ?: ""}".uppercase(),
        style = LocalTextStyle.current.copy(
          color = textColor,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
          stepSize = 0.05.sp,
          maxFontSize = 18.sp,
          minFontSize = 5.sp,
        ),
      )
      // Matricola
      Text(
        text = user?.matricola ?: "",
        color = textColor.copy(alpha = 0.8f),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp
      )
    }
  }
}

@Composable
fun BadgeBack(user: User?, textColor: Color = MaterialTheme.colorScheme.onBackground) {
  Column(modifier = Modifier.fillMaxSize()) {
    Spacer(modifier = Modifier.height(24.dp))
    // Magnetic Stripe
    Box(
      modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .background(Color.Black)
    )

    Spacer(modifier = Modifier.weight(1f))

    Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = user?.course ?: "",
          color = textColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.weight(1f)
        )
        Text(
          text = user?.year ?: "",
          color = textColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Text(
        text = user?.email ?: "",
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
