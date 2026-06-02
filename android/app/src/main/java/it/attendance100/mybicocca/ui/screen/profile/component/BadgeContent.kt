package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.ui.component.card.DitheredTexture
import it.attendance100.mybicocca.ui.theme.BadgeSignatureBoxColorRed
import it.attendance100.mybicocca.ui.theme.BadgeSignatureBoxColorRed2
import it.attendance100.mybicocca.ui.theme.BadgeSignatureBoxColorWhite
import it.attendance100.mybicocca.ui.theme.BadgeSignatureBoxColorWhite2
import it.attendance100.mybicocca.ui.theme.BadgeWhiteDrawableColor
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.OnBackgroundColor
import java.io.File
import java.time.Instant
import java.util.Locale

private fun String.titleCase(): String {
    if (isEmpty()) return ""
    return split("\\s+".toRegex()).joinToString(" ") { word ->
        if (word.isEmpty()) "" else word.take(1).uppercase(Locale.ROOT) + word.substring(1).lowercase(Locale.ROOT)
    }
}

// Front face: layered MyBicocca logo art + EMV-style chip / contactless glyph, with the
// holder name and matricola. Layers shift by (touch - 0.5) * movementCoeff for parallax.
@Composable
fun BadgeFront(
    account: Account?,
    career: Career? = null,
    textColor: Color = OnBackgroundColor,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    whiteBadge: Boolean,
    badgeParallax: Boolean = true,
) {
    val scale = .8
    val height = 52
    val drawableColor = if (whiteBadge) BadgeWhiteDrawableColor else textColor
    val movementCoeff = if (badgeParallax) 30 else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DitheredTexture()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 40.dp),
        ) {


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
                colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
            )

            Image(
                painter = painterResource(R.drawable.logo_mono),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 12.dp)
                    .absoluteOffset(y = (-35).dp)
                    .size(90.dp),
                colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .absoluteOffset(x = (-85).dp, y = (-2).dp),
            ) {
                Text(
                    text = "Università degli Studi",
                    fontSize = 17.sp,
                    color = drawableColor,
                )
                Image(
                    painter = painterResource(R.drawable.text),
                    contentDescription = null,
                    modifier = Modifier
                        .absoluteOffset(y = (-40).dp)
                        .size(100.dp),
                    colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
                )
            }

            val chip = getChipDrawable(account)

            Image(
                painter = painterResource(chip),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = (height + 3).dp)
                    .width((scale * 64).dp)
                    .height((scale * 52).dp)
                    .clip(RoundedCornerShape(corner = CornerSize((scale * 9.5).dp))),
                colorFilter = ColorFilter.tint(
                    if (whiteBadge) Color.Red.copy(alpha = 0.1f) else Color(0xFFFFAD42),
                    blendMode = BlendMode.Multiply,
                ),
            )

            Icon(
                painter = painterResource(R.drawable.contactless),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = (height + 8).dp, start = 65.dp)
                    .size(30.dp),
                tint = drawableColor,
            )

            Column(
                modifier = Modifier.padding(top = (height + 70).dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText(
                    text = (account?.displayName ?: "Studente").uppercase(),
                    style = LocalTextStyle.current.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        stepSize = 0.05.sp,
                        maxFontSize = 18.sp,
                        minFontSize = 5.sp,
                    ),
                )
                if (career?.matricola != null) Text(
                    text = career.matricola,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                )
            }
        }
    }
}

// Back face: magnetic stripe, signature box (cursive over ruled lines), grayscale photo,
// and institutional email. Haze blur is sampled from the card's hazeSource.
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BadgeBack(
    account: Account?,
    photoFile: File? = null,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    touchX: Float = 0.5f,
    touchY: Float = 0.5f,
    whiteBadge: Boolean,
    hazeState: HazeState,
    badgeParallax: Boolean = true,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val fontFamily = FontFamily(
            Font(R.font.mrs_saint_delafield_regular, FontWeight.Medium),
        )
        val drawableColor = if (whiteBadge) BadgeWhiteDrawableColor else textColor
        val movementCoeff = if (badgeParallax) 30 else 0

        val signatureBgCol =
            if (whiteBadge) BadgeSignatureBoxColorWhite else BadgeSignatureBoxColorRed
        val signatureBgCol2 =
            if (whiteBadge) BadgeSignatureBoxColorWhite2 else BadgeSignatureBoxColorRed2

        DitheredTexture()

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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
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
            colorFilter = ColorFilter.lighting(drawableColor, drawableColor),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            // Magnetic stripe
            Box(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.regular())
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(
                        (if (whiteBadge) BadgeSignatureBoxColorWhite else Color(0xFF000000)).copy(
                            alpha = 0.95f
                        )
                    ),
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .padding(end = 12.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = "Signature",
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )

                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp)
                                .height(40.dp)
                                .hazeEffect(state = hazeState, style = HazeMaterials.regular())
                                .background(signatureBgCol),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 5.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
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
                            BasicText(
                                modifier = Modifier
                                    .offset(y = 1.75.dp)
                                    .padding(horizontal = 10.dp),
                                text = account?.displayName?.titleCase() ?: "",
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

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(3f / 4f)
                            .padding(top = 12.dp),
                    ) {
                        val matrix = remember { ColorMatrix().apply { setToSaturation(0f) } }
                        if (photoFile != null) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = "Foto profilo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                colorFilter = ColorFilter.colorMatrix(matrix),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    Text(
                        modifier = Modifier.offset(y = 5.dp),
                        text = "Email",
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        // username may already be the full institutional email (the sign-in field),
                        // so only append the domain when it's a bare local part.
                        text = account?.username?.let { if (it.contains('@')) it else "$it@campus.unimib.it" }
                            ?: "",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

fun getChipDrawable(account: Account?): Int {
    val chips = listOf(
        R.drawable.chip1,
        R.drawable.chip2,
        R.drawable.chip3,
        R.drawable.chip4,
        R.drawable.chip5,
        R.drawable.chip6,
        R.drawable.chip7,
        R.drawable.chip8,
        R.drawable.chip9,
        R.drawable.chip10
    )

    // Use the hash of the account ID to select a chip drawable, ensuring that the same user gets the same chip each time
    val index = account?.id?.value?.hashCode()?.rem(chips.size)?.let {
        if (it < 0) it + chips.size else it
    } ?: 0
    return chips[index]
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0606)
@Composable
private fun BadgeFrontDarkModePreview() {
    val careerId = CareerId(123456L)
    val account = Account(
        id = AccountId("user123"),
        username = "m.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "record123",
            personId = 1L,
            fiscalCode = "RSSMRA80A01H501A",
            careers = emptyList(),
            selectedCareerId = careerId
        ),
        learning = LearningIdentity(
            lmsUserId = 1,
            lmsUsername = "mrossi",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 1024L,
            storageQuotaBytes = 1024L
        ),
        createdAt = Instant.now(),
        lastUsedAt = Instant.now(),
        lastSyncedAt = Instant.now()
    )
    val career = Career(
        id = careerId,
        enrollmentTraitId = 1L,
        programId = 1L,
        easyStaffProgramCode = null,
        academicYearEnrollmentId = 1L,
        matricola = "123456",
        description = "Informatica",
        academicYear = 2023,
        status = CareerStatus.ACTIVE
    )
    BicoccaTheme(dark = true) {
        Box(
            modifier = Modifier
                .size(width = 400.dp, height = 250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xff600528))
        ) {
            BadgeFront(
                account = account,
                career = career,
                whiteBadge = false,
                badgeParallax = false
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0606)
@Composable
private fun BadgeBackDarkModePreview() {
    val careerId = CareerId(123456L)
    val account = Account(
        id = AccountId("user123"),
        username = "m.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "record123",
            personId = 1L,
            fiscalCode = "RSSMRA80A01H501A",
            careers = emptyList(),
            selectedCareerId = careerId
        ),
        learning = LearningIdentity(
            lmsUserId = 1,
            lmsUsername = "mrossi",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 1024L,
            storageQuotaBytes = 1024L
        ),
        createdAt = Instant.now(),
        lastUsedAt = Instant.now(),
        lastSyncedAt = Instant.now()
    )
    BicoccaTheme(dark = true) {
        Box(
            modifier = Modifier
                .size(width = 400.dp, height = 250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xff600528))
        ) {
            BadgeBack(
                account = account,
                photoFile = null,
                whiteBadge = false,
                hazeState = remember { HazeState() },
                badgeParallax = false
            )
        }
    }
}