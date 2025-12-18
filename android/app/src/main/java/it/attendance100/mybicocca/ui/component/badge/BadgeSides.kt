package it.attendance100.mybicocca.ui.component.badge


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import coil.compose.*
import dev.chrisbanes.haze.*
import dev.chrisbanes.haze.materials.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.extension.*
import it.attendance100.mybicocca.manager.*
import it.attendance100.mybicocca.ui.component.*
import it.attendance100.mybicocca.ui.theme.*

val usr = User(
    name = "Federico Giarrusso",
    surname = "e' super power",
    matricola = "909369",
    course = "Informatica",
    year = "3",
    email = "l.lupi3@campus.unimib.it"
)

@Composable
@Preview(device = "spec:width=1080px,height=2340px,dpi=480")
fun BadgeFrontPreview() {
    val hazeState = remember { HazeState() }
    CardFace(
        modifier = Modifier.wrapContentSize().run { this },
        content = { x, y, whiteBadge, _ ->
            BadgeFront(
                usr,
                touchX = x,
                touchY = y,
                whiteBadge = whiteBadge
            )
        },
        background = PrimaryColor,
        isChromatic = true,
        touchX = 0f,
        touchY = 0f,
        hazeState = hazeState,
    )
}

@Composable
@Preview(device = "spec:width=1080px,height=2340px,dpi=480")
fun BadgeBackPreview() {
    val hazeState = remember { HazeState() }
    CardFace(
        content = { x, y, whiteBadge, hazeState ->
            BadgeBack(
                usr,
                touchX = x,
                touchY = y,
                whiteBadge = whiteBadge,
                hazeState = hazeState
            )
        },
        background = PrimaryColor,
        isChromatic = true,
        touchX = 0f,
        touchY = 0f,
        hazeState = hazeState,
    )
}

@Composable
fun BadgeFront(
    user: User?,
    textColor: Color = OnBackgroundColor,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    whiteBadge: Boolean
) {
    val preferencesManager = rememberPreferencesManager()
    val scale = .8
    val height = 52
    val drawableColor = if (whiteBadge) BadgeWhiteDrawableColor else textColor
    var movementCoeff = 30
    if (!preferencesManager.badgeParallax) movementCoeff = 0

    Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 40.dp, start = 40.dp)
    ) {
        // BG Texture
        DitheredTexture(
            modifier = Modifier
              .fillMaxSize()
              .offset(
	              x = (-190 + (-touchX + 0.5f) * (movementCoeff * 1.5)).dp,
	              y = ((-touchY + 0.5f) * (movementCoeff * 1.5)).dp
              )
              .rotate(10f),
            color = Color.Black,
            spacing = 10f,          // Distance between dots
            dotSize = 5f,          // Size of the dot
            globalRotation = 35f,   // Rotate the whole wall of dots
            dotRotation = 80f,       // Rotate the rhombus itself
            fadeStart = 0.2f,       // Fade starts at 10% width
            fadeEnd = 1f          // Fade ends at 90% width
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
              .absoluteOffset(
	              x = (43 + (-touchX + 0.5f) * (movementCoeff)).dp,
	              y = (30 + (-touchY + 0.5f) * (movementCoeff / 2.5)).dp
              )

              .alpha(0.2f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
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
                color = drawableColor,
            )
            // MyBicocca Logo
            Image(
                painter = painterResource(R.drawable.text),
                contentDescription = null,
                modifier = Modifier
                    .absoluteOffset(y = (-40).dp)
                    .size(100.dp),
                colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
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
            colorFilter = ColorFilter.tint(
                if (whiteBadge) Color.Red.copy(alpha = 0.1f) else Color(
                    0xFFffad42
                ), blendMode = BlendMode.Multiply
            )
        )

        // Contactless Chip
        Icon(
            painter = painterResource(R.drawable.contactless),
            contentDescription = null,
            modifier = Modifier
                .padding(top = (height + 8).dp, start = 65.dp)
                .size(30.dp),
            tint = drawableColor
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BadgeBack(
    user: User?,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    whiteBadge: Boolean,
    hazeState: HazeState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val preferencesManager = rememberPreferencesManager()
        val fontFamily = FontFamily(
            Font(R.font.mrs_saint_delafield_regular, FontWeight.Medium),
        )
        val drawableColor = if (whiteBadge) BadgeWhiteDrawableColor else textColor

        var movementCoeff = 30
        if (!preferencesManager.badgeParallax) movementCoeff = 0

        val signatureBgCol =
            if (whiteBadge) BadgeSignatureBoxColorWhite else BadgeSignatureBoxColorRed
        val signatureBgCol2 =
            if (whiteBadge) BadgeSignatureBoxColorWhite2 else BadgeSignatureBoxColorRed2

        // BG Texture
        DitheredTexture(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (100 + (-touchX + 0.5f) * (movementCoeff * 1.5)).dp,
                    y = (-50 + (-touchY + 0.5f) * (movementCoeff * 1.5)).dp
                )
                .rotate(90f),
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
                .size(200.dp)
                .rotate(-7.5f)
                .absoluteOffset(
                    x = (-50 + (-touchX + 0.5f) * (movementCoeff * 1.5)).dp,
                    y = (80 + (-touchY + 0.5f) * (movementCoeff * 0.5)).dp
                )

                .alpha(0.2f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )
        Image(
            painter = painterResource(R.drawable.logo_mono),
            contentDescription = null,

            modifier = Modifier
                .wrapContentSize()
                .size(70.dp)
                .absoluteOffset(
                    x = (-10 + (-touchX + 0.5f) * (movementCoeff * 1.9)).dp,
                    y = (65 + (-touchY + 0.5f) * (movementCoeff * 1)).dp
                )
                .rotate(20f)

                .alpha(0.17f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )
        Image(
            painter = painterResource(R.drawable.logo_mono),
            contentDescription = null,

            modifier = Modifier
                .wrapContentSize()
                .size(80.dp)
                .absoluteOffset(
                    x = (103 + (-touchX + 0.5f) * (movementCoeff * 2.5)).dp,
                    y = (135 + (-touchY + 0.5f) * (movementCoeff * 1)).dp
                )
                .rotate(20f)

                .alpha(0.135f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )
        Image(
            painter = painterResource(R.drawable.logo_mono),
            contentDescription = null,

            modifier = Modifier
                .wrapContentSize()
                .size(40.dp)
                .absoluteOffset(
                    x = (60 + (-touchX + 0.5f) * (movementCoeff * 3)).dp,
                    y = (95 + (-touchY + 0.5f) * (movementCoeff * 1.6)).dp
                )
                .rotate(-20f)

                .alpha(0.125f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )
        Image(
            painter = painterResource(R.drawable.logo_mono),
            contentDescription = null,

            modifier = Modifier
                .wrapContentSize()
                .size(30.dp)
                .absoluteOffset(
                    x = (160 + (-touchX + 0.5f) * (movementCoeff * 3)).dp,
                    y = (200 + (-touchY + 0.5f) * (movementCoeff * 1.5)).dp
                )
                .rotate(-20f)

                .alpha(0.1f),
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor)
        )
        Image(
            painter = painterResource(R.drawable.logo_mono),
            contentDescription = null,

            modifier = Modifier
                .wrapContentSize()
                .size(15.dp)
                .absoluteOffset(
                    x = (120 + (-touchX + 0.5f) * (movementCoeff * 5)).dp,
                    y = (115 + (-touchY + 0.5f) * (movementCoeff * 2)).dp
                )
                .rotate(40f)

                .alpha(0.05f),
            colorFilter = ColorFilter.lighting(
                drawableColor, drawableColor

            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Magnetic Stripe
            Box(
                modifier = Modifier
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular(),
                    )
                    .fillMaxWidth()
                    .height((55).dp)
                    .background(
                        (if (whiteBadge) BadgeSignatureBoxColorWhite else Color(0xFF000000)).copy(
                            alpha = 0.95f
                        )
                    )
            ) {}

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .padding(end = 12.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Signature Row
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Signature
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = "Signature",
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Signature Box
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp)
                                .height(40.dp)
                                .hazeEffect(state = hazeState, style = HazeMaterials.regular())
                                .background(signatureBgCol)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 5.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .background(signatureBgCol2)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .background(signatureBgCol2)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .background(signatureBgCol2)
                                )
                            }
                            // Signature Text
                            BasicText(
                                modifier = Modifier
                                    .offset(y = 1.75.dp)
                                    .padding(horizontal = 10.dp),
                                text = "${user?.name?.titleCase() ?: ""} ${user?.surname?.titleCase() ?: ""}",
                                style = LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    color = if (whiteBadge) textColor else Color.Black,
                                    lineHeight = 15.sp,
                                    fontFamily = fontFamily,
                                ),
                                softWrap = false,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(
                                    stepSize = 0.05.sp,
                                    maxFontSize = 38.sp,
                                    minFontSize = 15.sp,
                                ),
                            )
                        }
                    }

                    // Segreterie pfp
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(3f / 4f)
                            .padding(top = 12.dp)
                    ) {
                        val matrix = remember { ColorMatrix().apply { setToSaturation(0f) } }
                        AsyncImage(
                            model = "https://elearning.unimib.it/pluginfile.php/1568485/user/icon/bicocca/f1?rev=16628697",
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.colorMatrix(matrix),
                            onError = { error ->
                                error.result.throwable.printStackTrace()
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    // Email Label
                    Text(
                        modifier = Modifier.offset(y = 5.dp),
                        text = "Email",
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // Email
                    Text(
                        text = user?.email ?: "",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

            }
        }
    }
}
